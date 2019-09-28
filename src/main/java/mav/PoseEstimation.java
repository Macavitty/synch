package mav;

import java.io.*;

public class PoseEstimation {

    public PoseEstimation() {
    }

    public void ranIgorScript(String absolutePath, String origVideo, String userVideo) throws IOException, InterruptedException {

        Process p = Runtime.getRuntime().exec("python3 " + absolutePath + " " + origVideo + " " + userVideo);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        String line = "null";
        while ((line = in.readLine()) != null){
            buffer.append(line);
            buffer.append("\n");
        }
        int exitCode = p.waitFor();
        System.out.println("Value is: " + buffer.toString());
        System.out.println("Process exit value:"+exitCode);
        in.close();

    }
}