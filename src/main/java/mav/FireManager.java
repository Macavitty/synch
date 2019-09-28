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
    Bucket bucket;

    private File workingDir = new File(FileSystems.getDefault().getPath(".").toString());
    public Map<String, User> users = new HashMap<>();

    private FileInputStream serviceKeysJson = new FileInputStream(workingDir + "/goalchallenge-8de36-firebase-adminsdk-cs8im-41908d0450.json");
    private final String VIDEOS_POSTED_DIR = workingDir + "/videos_posted";
    private final String VIDEOS_MERGED_DIR = workingDir + "/videos_merged";
    private final String ORIGIN_VIDEO = workingDir + "/repeat_me.mp4";
    private File videosPosted, videosMerged;

    public FireManager() throws IOException {
        videosMerged = new File(VIDEOS_MERGED_DIR);
        videosPosted = new File(VIDEOS_POSTED_DIR);
        boolean createdP = videosPosted.mkdir();
        boolean createdM = videosMerged.mkdir();
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
                    if (!users.containsKey(snapKey)){
                        usr = new User(uid, dataSnapshot.child("postedvideo").getValue().toString(), -10);
                        users.put(snapKey, usr);
                    }
                    else{
                        // user participates in new challenge
                        usr = users.get(snapKey);
                        usr.setScore(0);
                    }
                    String downPath = downloadVideo(getVideoUrl(dataSnapshot));
                    Integer score = poseEstimation.ranIgorScript(workingDir + "/script.py", "orig", downPath);
                    usr.setScore(score);
                    String mergedPath = createMergedVideo(ORIGIN_VIDEO, downPath);
                    System.out.println("MERGED");
                    String mergedLink = loadMergedVideo(mergedPath);
                    usr.setMergedvideo(mergedLink);
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

    private void initStorage(){
        bucket = StorageClient.getInstance().bucket();
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
        InputStream is = null;
        BufferedOutputStream outStream = null;
        try {
            byte[] buf;
            int byteRead, byteWritten = 0;
            outStream = new BufferedOutputStream(new FileOutputStream(videosPosted +  "/123.mp4"));

            URLConnection conn = videoUrl.openConnection();
            is = conn.getInputStream();
            buf = new byte[59];
            while ((byteRead = is.read(buf)) != -1) {
                outStream.write(buf, 0, byteRead);
                byteWritten += byteRead;
            }

        } catch (Exception e) {
            System.out.println("Oops, IO error " + videoUrl);
            e.printStackTrace();
        } finally {
            try {
                assert is != null;
                is.close();
                outStream.close();
                System.out.println("Success !");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return videosPosted + "/123.mp4";
    }

    private void updUser (String key, User user) {
        dbRef.child(key).setValueAsync(user);
    }

    private String createMergedVideo(String origin, String posted) throws IOException {
        System.out.println("MERGING .... ");
        String mergedPath = videosMerged + "/123" + "_merged.mp4";
        // run script, return result

        Process p = Runtime.getRuntime().exec(new String[]{"./merge.sh", origin, posted, mergedPath} );
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((in.readLine()) != null){
            // wait till read
        }
        in.close();
        return mergedPath;
    }

    private String loadMergedVideo(String path) throws FileNotFoundException {
        InputStream testFile = new FileInputStream(path);
        System.out.println(path);
        String blobString = "folder/" + path;
        Blob created =  bucket.create(blobString, testFile , Bucket.BlobWriteOption.userProject("goalchallenge-8de36"));
        String s = created.getSelfLink();
//        String s = created.getMediaLink();
        return s;
    }
}