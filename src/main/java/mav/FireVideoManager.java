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
import java.nio.file.Path;

public class FireVideoManager {

    private FirebaseOptions options;
    PoseEstimation script;

    private File workingDir = new File(FileSystems.getDefault().getPath(".").toString());

    FileInputStream serviceKeysJson = new FileInputStream(workingDir + "/expensivevkchat-firebase-adminsdk-cqssj-b84457cad2.json");
    private final String VIDEOS_FOLDER = workingDir + "/videos_folder";
    File videosFolder;

    public FireVideoManager() throws IOException {
        videosFolder = new File(VIDEOS_FOLDER);
        boolean created = videosFolder.mkdir();
        script = new PoseEstimation();
        initFire();
    }

    public void initFire() throws IOException {
        options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setDatabaseUrl("https://expensivevkchat.firebaseio.com/")
                .build();
        FirebaseApp.initializeApp(options);
        initDataBase();
    }

    private void initDataBase() throws FileNotFoundException {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("videos");
        ref.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                System.out.println("Getting video  . . . . . . . . . . . ");
                try {
                    String downPath = downloadVideo(getVideoUrl(dataSnapshot));
                    // todo call script
                    script.ranIgorScript("path", "orig", downPath);

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

    void justListen() {
        while (true) {
        }
    }

    private URL getVideoUrl(DataSnapshot dataSnapshot) throws MalformedURLException, URISyntaxException {
        return new URL(dataSnapshot.child("video").getValue().toString());

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
            outStream = new BufferedOutputStream(new FileOutputStream(videosFolder + "/" + getVideoName(videoUrl)));

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
        return videosFolder + "/" + getVideoName(videoUrl);
    }


}