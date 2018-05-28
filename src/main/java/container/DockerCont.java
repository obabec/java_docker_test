package container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;

public class DockerCont {
    private DockerClient dockerClient;

    public DockerCont(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public CreateContainerResponse createContainer(String tag, String name) {
        return dockerClient.createContainerCmd(tag)
                .withName(name)
                .exec();
    }
    public void connectContToNetwork(CreateContainerResponse cont, String networkId) {
        dockerClient.connectToNetworkCmd().withNetworkId(networkId).withContainerId(cont.getId()).exec();

    }

}
