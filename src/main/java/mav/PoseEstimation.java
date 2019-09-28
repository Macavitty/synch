package mav;

import java.io.*;

public class PoseEstimation {

    public PoseEstimation() {
    }

    public Integer ranIgorScript(String absolutePath, String origVideo, String userVideo) throws IOException, InterruptedException {
        System.out.println("IN");
        Integer score;
        Process p = Runtime.getRuntime().exec("python3 " + absolutePath + " " + origVideo + " " + userVideo);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        int exitCode = p.waitFor();
        score = Integer.valueOf(buffer.toString());
        in.close();
        System.out.println("OUT");
        return score;
    }
}