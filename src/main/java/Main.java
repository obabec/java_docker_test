import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import network.DockerNetwork;

import java.io.File;
import java.util.HashSet;
import java.util.Set;


public class Main {

    public static void main(String[] args) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(DefaultDockerClientConfig.createDefaultConfigBuilder().build()).build();

        Info info = dockerClient.infoCmd().exec();
        File dockerDir = new File("DOCKER DIR PATH");
        String tag = "docker_test:01";
        dockerClient.buildImageCmd(dockerDir).withTag(tag).exec(new BuildImageResultCallback()).awaitImageId();

        String subnet =  "172.22.0.0/16";
        String subnet2 = "172.23.0.0/16";
        DockerNetwork dockerNetwork = new DockerNetwork();
        String serverNetwork =  dockerNetwork.createNetworkWithSubnet(subnet, "server_network", dockerClient, "172.22.0.1");
        String clientNetwork = dockerNetwork.createNetworkWithSubnet(subnet2, "client_network", dockerClient, "172.23.0.1");


        CreateContainerResponse router = dockerClient.createContainerCmd(tag)
                .withName("router")
                .exec();


        dockerClient.connectToNetworkCmd().withNetworkId(clientNetwork).withContainerId(router.getId()).exec();
        dockerClient.connectToNetworkCmd().withContainerId(router.getId()).withNetworkId(serverNetwork).exec();
        dockerClient.startContainerCmd(router.getId()).exec();


        CreateContainerResponse client = dockerClient.createContainerCmd(tag)
                .withName("comm_client")
                .exec();

        CreateContainerResponse server = dockerClient.createContainerCmd(tag)
                .withName("comm_server")
                .exec();



        dockerClient.connectToNetworkCmd().withNetworkId(clientNetwork).withContainerId(client.getId()).exec();

        dockerClient.connectToNetworkCmd().withNetworkId(serverNetwork).withContainerId(server.getId()).exec();


        dockerClient.startContainerCmd(client.getId()).exec();
        dockerClient.startContainerCmd(server.getId()).exec();

        System.out.println(info);


    }
}
