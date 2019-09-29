package mav;

import java.io.*;

public class PoseEstimation {

    public PoseEstimation() {
    }

    public Integer ranIgorScript(String absolutePath, String origVideo, String userVideo) throws IOException, InterruptedException {
        Integer score = 0;
        System.out.println("VIDEO " + origVideo);
        System.out.println("AN VIDE " + userVideo);
        Process p = Runtime.getRuntime().exec("python3 /home/ubuntu/lightweight-human-pose-estimation.pytorch/demo.py  --video " + origVideo + " --video_current " + userVideo);
//        Process p = Runtime.getRuntime().exec(new String[]{"/usr/bin/python3", "/home/ubuntu/lightweight-human-pose-estimation.pytorch/demo.py ", " --video ", origVideo, " --video_current ", userVideo});

        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        String line = "";
        while ((line = in.readLine()) != null){
            buffer.append(line);
        }
        System.out.println("HERE");
        int exitCode = p.waitFor();
        System.out.println("PY exit code: " + exitCode);
        if (exitCode != 0) {
            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            line = "";
            while ((line = err.readLine()) != null){
                System.out.println(line);
            }
        }
//        System.out.println("PY buffer: " + buffer.toString());
        score = Integer.valueOf(buffer.toString());
        System.out.println("PY score: " + score);
        in.close();
        if (exitCode != 0) return 42;
        return score;
    }
}

// python3 demo.py --video ~/video_cutted.mp4 --video_current ~/IMG_7935.mp4