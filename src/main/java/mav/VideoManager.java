package mav;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.database.DataSnapshot;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class VideoManager {
    private File workingDir = new File("/home/ubuntu");
    private File videosPosted, videosMerged;
    private final String VIDEOS_POSTED_DIR = workingDir + "/videos_posted";
    private final String VIDEOS_MERGED_DIR = workingDir + "/videos_merged";
    private final String ORIGIN_VIDEO = workingDir + "/video_cutted.mp4";

    public VideoManager() {
        videosMerged = new File(VIDEOS_MERGED_DIR);
        if (videosMerged.exists()) {
            new File(VIDEOS_MERGED_DIR + "/123_merged.mp4").deleteOnExit();
            videosMerged.delete();
        }

        videosPosted = new File(VIDEOS_POSTED_DIR);
        if (videosPosted.exists()) {
            new File(VIDEOS_POSTED_DIR + "/123.mp4").deleteOnExit();
            videosPosted.delete();
        }

        boolean createdP = videosPosted.mkdir();
        boolean createdM = videosMerged.mkdir();

    }

    String downloadVideo(URL videoUrl) {
        System.out.println("Lets download * * *");
        new File(VIDEOS_POSTED_DIR + "/123.mp4").deleteOnExit();
        InputStream is = null;
        BufferedOutputStream outStream = null;
        try {
            byte[] buf;
            int byteRead, byteWritten = 0;
            outStream = new BufferedOutputStream(new FileOutputStream(videosPosted  + "/123.mp4"));

            URLConnection conn = videoUrl.openConnection();
            is = conn.getInputStream();
            buf = new byte[100];
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


    String createMergedVideo(String origin, String posted, Integer score) throws IOException, InterruptedException {
        System.out.println("MERGING .... ");
        String mergedPath = videosMerged + "/123" + "_merged.mp4";
        String tmpPath = videosMerged + "/tmp" + "_merged.mp4";
        new File(mergedPath).deleteOnExit();
        new File(tmpPath).deleteOnExit();

        // run script, return result
        Thread.sleep(2000);


        Process p = Runtime.getRuntime().exec(new String[]{"./merge.sh", origin, posted, score.toString() ,mergedPath, tmpPath});

        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((in.readLine()) != null) {
            // wait till read
        }
        in.close();
        return mergedPath;
    }

    String loadMergedVideo(String path, Bucket bucket) throws FileNotFoundException {
        InputStream testFile = new FileInputStream(path);
        String blobString = "folder/123_merged.mp4";

        Blob created = bucket.create(blobString, testFile, Bucket.BlobWriteOption.userProject("thelats-ef16e"));
        String s = created.getSelfLink();
//        String s = created.getMediaLink();
        return s;
    }

    public void uploadVideoViaCurl(String url) throws IOException {
        System.out.println("Curl " + VIDEOS_MERGED_DIR + "/123_merged.mp4" + url);
        Process p = Runtime.getRuntime().exec(new String[]{"./curl.sh", VIDEOS_MERGED_DIR + "/123_merged.mp4", url});
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        while ((in.readLine()) != null) {
            // wait till read
        }
        in.close();
    }

    public void rmfolds(){

    }
}
