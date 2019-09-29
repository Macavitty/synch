package mav;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class FireManager {

    private File workingDir = new File("/home/ubuntu");
    private File videosPosted, videosMerged;
    private final String VIDEOS_POSTED_DIR = workingDir + "/videos_posted";
    private final String VIDEOS_MERGED_DIR = workingDir + "/videos_merged";
    private final String ORIGIN_VIDEO = workingDir + "/video_cutted.mp4";

    private PoseEstimation poseEstimation;
    private String _url = "";
    private VideoManager videoManager;
    private DatabaseReference dbRef;
    private DatabaseReference urlsRef;
    private Bucket bucket;
    public FirebaseApp firebaseApp;
    String urlFromAlina = "";


    private FileInputStream serviceKeysJson = new FileInputStream(workingDir + "/thelats-ef16e-firebase-adminsdk-uf03f-12c0ed8911.json");
    public Map<String, UserModel> users = new HashMap<>();


    public FireManager() throws IOException {
        videoManager = new VideoManager();
        poseEstimation = new PoseEstimation();
        initFire();
    }

    public void initFire(   ) throws IOException {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceKeysJson))
                .setDatabaseUrl("https://thelats-ef16e.firebaseio.com/")
                .setStorageBucket("thelats-ef16e.appspot.com")
                .build();
        firebaseApp = FirebaseApp.initializeApp(options);
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
                        usr = new UserModel(uid, dataSnapshot.child("postedvideo").getValue().toString(), 10);
                        users.put(snapKey, usr);
                    } else {
                        // user participates in new challenge
                        usr = users.get(snapKey);
                        usr.setScore(0);
                    }
                    String downPath = videoManager.downloadVideo(videoManager.getPostVideoUrl(dataSnapshot));
                    Integer score = poseEstimation.ranIgorScript(workingDir + "/script.py", "/home/ubuntu/video_cutted.mp4", downPath);
                    usr.setScore(score);
                    String mergedPath = videoManager.createMergedVideo(videoManager.getOriginVideo(), downPath, score);
                    System.out.println("MERGED");
                    String mergedLink = videoManager.loadMergedVideo(mergedPath, bucket);
                    usr.setMergedvideo(mergedLink);
                    // upd in database
                    updUser(snapKey, usr);

                    FirebaseDatabase.getInstance()
                            .getReference("urls").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            _url = dataSnapshot.child("post").getValue().toString();
                            System.out.println("URL!!! " + _url);
                            if (!_url.isEmpty() && new File(mergedPath).exists()) {
                                System.out.println("ЗФЕР is not NULL. Calling curl");
                                try {
                                    videoManager.uploadVideoViaCurl(_url);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    if (!_url.isEmpty()) {
                        System.out.println("URL is not NULL. Calling curl");
                        videoManager.uploadVideoViaCurl(_url);

                    }



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

        urlsRef = FirebaseDatabase.getInstance()
                .getReference("urls").child("post");
        urlsRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                urlFromAlina = dataSnapshot.child("post").getValue().toString();
                System.out.println("urlFromAlina" + urlFromAlina);
                if (!urlFromAlina.equals("")){
                    System.out.println("URL " + urlFromAlina);
                    _url = urlFromAlina;
                    String mergedPath = VIDEOS_MERGED_DIR + "/123" + "_merged.mp4";
                    if (new File(mergedPath).exists()) {
                        System.out.println("ЗФЕР is not NULL. Calling curl");
                        try {
                            videoManager.uploadVideoViaCurl(_url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                urlFromAlina = dataSnapshot.child("post").getValue().toString();
                System.out.println("urlFromAlina" + urlFromAlina);
                if (!urlFromAlina.equals("")){
                    System.out.println("URL " + urlFromAlina);
                    _url = urlFromAlina;
                    String mergedPath = VIDEOS_MERGED_DIR + "/123" + "_merged.mp4";
                    if (new File(mergedPath).exists()) {
                        System.out.println("ЗФЕР is not NULL. Calling curl");
                        try {
                            videoManager.uploadVideoViaCurl(_url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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