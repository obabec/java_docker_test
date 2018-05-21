package image;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.command.BuildImageResultCallback;

import java.io.File;

public class DockerImage {
    public void buildImage(DockerClient dockerClient, String tag, File dockerDir) {
        dockerClient.buildImageCmd(dockerDir).withTag(tag).exec(new BuildImageResultCallback()).awaitImageId();
    }
}
