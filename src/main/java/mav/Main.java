package mav;

import org.apache.log4j.BasicConfigurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

public class Main {



    public Main() throws IOException {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File workingDir = new File(FileSystems.getDefault().getPath(".").toString());
        // because log4j wants it
        BasicConfigurator.configure();

//        FireVideoManager fireVideoManager = new FireVideoManager();
//        fireVideoManager.justListen();

        PoseEstimation doSomething = new PoseEstimation();
        doSomething.ranIgorScript(workingDir + "/script.py", "orig.mp4", "user.mp4");
    }

}
