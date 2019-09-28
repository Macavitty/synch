package mav;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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

    private FirebaseOptions options;
    PoseEstimation poseEstimation;
//    DatabaseReference usersRef;
    DatabaseReference dbRef;

    private File workingDir = new File(FileSystems.getDefault().getPath(".").toString());
    public Map<String, User> users = new HashMap<>();

    FileInputStream serviceKeysJson = new FileInputStream(workingDir + "/goalchallenge-8de36-firebase-adminsdk-cs8im-41908d0450.json");
    private final String VIDEOS_FOLDER = workingDir + "/videos_folder";
    File videosFolder;

    public FireManager() throws IOException {
        videosFolder = new File(VIDEOS_FOLDER);
        boolean created = videosFolder.mkdir();
        poseEstimation = new PoseEstimation();
        initFire();
    }

    public void initFire() throws IOException {
        options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceKeysJson))
                .setDatabaseUrl("https://goalchallenge-8de36.firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(options);
        initDataBase();
    }

    private void initDataBase() throws FileNotFoundException {
        dbRef = FirebaseDatabase.getInstance()
                .getReference("videos");
//        usersRef = dbRef.child("videos");
        dbRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                User usr;
                System.out.println("Getting video  . . . . . . . . . . . ");
                try {
                    /*
                    * MAIN
                    * */
                    String snapKey = dataSnapshot.getKey();
                    String uid = dataSnapshot.child("userid").getValue().toString();
                    System.out.println("UID is " + uid);
                    if (!users.containsKey(snapKey)){
                        System.out.println("TRUE " + dataSnapshot.child("postedvideo").getValue().toString());
                        usr = new User(uid, dataSnapshot.child("postedvideo").getValue().toString(), -10);
                        users.put(snapKey, usr);
                    }
                    else{
                        System.out.println("FALSE");
                        // user participates in new challenge
                        usr = users.get(snapKey);
//                        usr.setPostedvideo("");
                        usr.setScore(0);
                    }
                    System.out.println("HERE");
                    String downPath = downloadVideo(getVideoUrl(dataSnapshot));
                    System.out.println("FOUND video: " + downPath);
//                    usr.setPostedvideo(downPath);
                    Integer score = poseEstimation.ranIgorScript(workingDir + "/script.py", "orig", downPath);
                    System.out.println("SCORE: " + score);
                    usr.setScore(score);
                    // upd in database
                    updUser(snapKey, usr);

                } catch (MalformedURLException | URISyntaxException e) {
                    System.out.println("Soryan, I can`t download this for you, probably the url is not valid");
//                    e.printStackTrace();
                } catch (InterruptedException |IOException  e) {
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

    void justListenForNewVideo() {
        while (true) {
        }
    }

    private URL getVideoUrl(DataSnapshot dataSnapshot) throws MalformedURLException, URISyntaxException {

        return new URL(dataSnapshot.child("postedvideo").getValue().toString());

    }

    private String getVideoName(URL videoUrl) {
        String str = videoUrl.toString();
        return str.substring(str.lastIndexOf("/"));
    }

    private String downloadVideo(URL videoUrl) {
        System.out.println("ONE");

        InputStream is = null;
        BufferedOutputStream outStream = null;
        try {
            byte[] buf;
            int byteRead, byteWritten = 0;
            System.out.println("ONE");
            outStream = new BufferedOutputStream(new FileOutputStream(videosFolder + "/" + getVideoName(videoUrl)));
            System.out.println("ONE");

            URLConnection conn = videoUrl.openConnection();
            System.out.println("ONE");
            is = conn.getInputStream();
            buf = new byte[59];
            while ((byteRead = is.read(buf)) != -1) {
                outStream.write(buf, 0, byteRead);
                byteWritten += byteRead;
                System.out.println("Writet" + byteWritten);
            }

            System.out.println("ONE");
        } catch (Exception e) {
            System.out.println("ONE");
            System.out.println("Oops, IO error " + videoUrl);
            e.printStackTrace();
        } finally {
            try {
                System.out.println("ONE");
                assert is != null;
                is.close();
                outStream.close();
                System.out.println("Success !");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("ONE");

        return videosFolder + "/" + getVideoName(videoUrl);
    }

    private void updUser (String key, User user) {
        System.out.println("Updating user");
        dbRef.child(key).setValueAsync(user);
    }
}