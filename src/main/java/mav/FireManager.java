package mav;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

public class FireManager {

    private PoseEstimation poseEstimation;
    private DatabaseReference dbRef;
    private Bucket bucket;
    private VideoManager videoManager;

    private File workingDir = new File(FileSystems.getDefault().getPath(".").toString());
    public Map<String, UserModel> users = new HashMap<>();

    private FileInputStream serviceKeysJson = new FileInputStream(workingDir + "/goalchallenge-8de36-firebase-adminsdk-cs8im-41908d0450.json");


    public FireManager() throws IOException {
        videoManager = new VideoManager();
        poseEstimation = new PoseEstimation();
        initFire();
    }

    public void initFire() throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceKeysJson))
                .setDatabaseUrl("https://goalchallenge-8de36.firebaseio.com/")
                .setStorageBucket("goalchallenge-8de36.appspot.com")
                .build();
        FirebaseApp.initializeApp(options);
        initDataBase();
        initStorage();
    }

    private void initDataBase() {
        dbRef = FirebaseDatabase.getInstance()
                .getReference("videos");
        dbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                UserModel usr;
                try {
                    String snapKey = dataSnapshot.getKey();
                    String uid = dataSnapshot.child("userid").getValue().toString();
                    if (!users.containsKey(snapKey)) {
                        usr = new UserModel(uid, dataSnapshot.child("postedvideo").getValue().toString(), -10);
                        users.put(snapKey, usr);
                    } else {
                        // user participates in new challenge
                        usr = users.get(snapKey);
                        usr.setScore(0);
                    }
                    String downPath = videoManager.downloadVideo(videoManager.getPostVideoUrl(dataSnapshot));
                    Integer score = poseEstimation.ranIgorScript(workingDir + "/script.py", "orig", downPath);
                    usr.setScore(score);
                    String mergedPath = videoManager.createMergedVideo(videoManager.getOriginVideo(), downPath);
                    System.out.println("MERGED");
                    String mergedLink = videoManager.loadMergedVideo(mergedPath, bucket);
                    usr.setMergedvideo(mergedLink);
                    // upd in database
                    updUser(snapKey, usr);

                } catch (MalformedURLException | URISyntaxException e) {
                    System.out.println("Soryan, I can`t download this for you, probably the url is not valid");
//                    e.printStackTrace();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void initStorage() {
        bucket = StorageClient.getInstance().bucket();
    }

    // the main loop
    void justListenForNewVideo() {
        while (true) {
        }
    }

    private void updUser(String key, UserModel userModel) {
        dbRef.child(key).setValueAsync(userModel);
    }


}