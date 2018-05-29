package com.redhat.PatrIoT.network_demo.image;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.redhat.PatrIoT.network_demo.files.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DockerImage {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerImage.class);

    private DockerClient dockerClient;

    public DockerImage(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void buildImage(String tag, String path) {

        FileUtils fileUtils = new FileUtils();

        if (new File(DockerImage.class.getClassLoader().getResource(path).getFile()).exists()) {

            LOGGER.info("Looking in resources");
            dockerClient.buildImageCmd(new File(DockerImage.class.getClassLoader().getResource(path).getFile()))
                    .withTag(tag).exec(new BuildImageResultCallback()).awaitImageId();


        } else if (DockerImage.class.getClassLoader().getResourceAsStream(path) != null){
            LOGGER.info("Looking in root");

            File docker = new File(fileUtils.convertToFile(DockerImage.class.getClassLoader()
                    .getResourceAsStream(path), "Dockerfile"));

            File script = new File(fileUtils.convertToFile(DockerImage.class.getClassLoader()
                    .getResourceAsStream("app/setGW"), "setGW"));

            script.setExecutable(true);

            if (path.contains("app")) {
                File javaClient = new File(fileUtils.convertToFile(DockerImage.class.getClassLoader()
                        .getResourceAsStream("app/DataClient-1.0-SNAPSHOT.jar"), "DataClient-1.0-SNAPSHOT.jar"));

                File javaServer = new File(fileUtils.convertToFile(DockerImage.class.getClassLoader()
                        .getResourceAsStream("app/DataServer-1.0-SNAPSHOT.jar"), "DataServer-1.0-SNAPSHOT.jar"));
                javaClient.deleteOnExit();
                javaServer.deleteOnExit();
            }

            dockerClient.buildImageCmd(docker).withTag(tag).exec(new BuildImageResultCallback()).awaitImageId();
            script.delete();
            docker.delete();

        } else {
            LOGGER.warn("DOCKERFILES does not exists");
            System.exit(0);
        }

    }



}
