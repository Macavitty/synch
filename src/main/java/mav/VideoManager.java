package mav;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.database.DataSnapshot;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystems;

public class VideoManager {
    private File workingDir = new File(FileSystems.getDefault().getPath(".").toString());
    private File videosPosted, videosMerged;
    private final String VIDEOS_POSTED_DIR = workingDir + "/videos_posted";
    private final String VIDEOS_MERGED_DIR = workingDir + "/videos_merged";
    private final String ORIGIN_VIDEO = workingDir + "/repeat_me.mp4";

    public VideoManager(){
        videosMerged = new File(VIDEOS_MERGED_DIR);
        videosPosted = new File(VIDEOS_POSTED_DIR);
        boolean createdP = videosPosted.mkdir();
        boolean createdM = videosMerged.mkdir();

    }
    String downloadVideo(URL videoUrl) {
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

    String getOriginVideo() {
        return ORIGIN_VIDEO;
    }

    URL getPostVideoUrl(DataSnapshot dataSnapshot) throws MalformedURLException, URISyntaxException {
        return new URL(dataSnapshot.child("postedvideo").getValue().toString());
    }

    String getVideoName(URL videoUrl) {
        String str = videoUrl.toString();
        return str.substring(str.lastIndexOf("/"));
    }


    String createMergedVideo(String origin, String posted) throws IOException {
        System.out.println("MERGING .... ");
        String mergedPath = videosMerged + "/123" + "_merged.mp4";
        // run script, return result

        Process p = Runtime.getRuntime().exec(new String[]{"./merge.sh", origin, posted, mergedPath});
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((in.readLine()) != null) {
            // wait till read
        }
        in.close();
        return mergedPath;
    }

    String loadMergedVideo(String path, Bucket bucket) throws FileNotFoundException {
        InputStream testFile = new FileInputStream(path);
        System.out.println(path);
        String blobString = "folder/" + path;
        Blob created = bucket.create(blobString, testFile, Bucket.BlobWriteOption.userProject("goalchallenge-8de36"));
        String s = created.getSelfLink();
//        String s = created.getMediaLink();
        return s;
    }
}