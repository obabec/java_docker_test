import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import container.DockerCont;
import image.DockerImage;
import network.DockerNetwork;

import java.io.File;



public class Main {

    public static void main(String[] args) {
        DockerClient dockerClient = DockerClientBuilder.getInstance(DefaultDockerClientConfig.createDefaultConfigBuilder().build()).build();

        Info info = dockerClient.infoCmd().exec();
        String tag = "docker_test:01";
        DockerImage dockerImage = new DockerImage();
        dockerImage.buildImage(dockerClient, "docker_test:01", new File("/home/obabec/Desktop/images"));

        DockerNetwork dockerNetwork = new DockerNetwork();
        String serverNetwork =  dockerNetwork.createNetworkWithSubnet("172.22.0.0/16", "server_network", dockerClient, "172.22.0.1");
        String clientNetwork = dockerNetwork.createNetworkWithSubnet("172.23.0.0/16", "client_network", dockerClient, "172.23.0.1");

        DockerCont dockerCont = new DockerCont(dockerClient);
        CreateContainerResponse router = dockerCont.createContainer(tag, "router");


        dockerCont.connectContToNetwork(router, clientNetwork);
        dockerCont.connectContToNetwork(router, serverNetwork);
        dockerClient.startContainerCmd(router.getId()).exec();


        CreateContainerResponse client = dockerCont.createContainer(tag, "comm_client");
        CreateContainerResponse server = dockerCont.createContainer(tag, "comm_server");

        dockerCont.connectContToNetwork(client, clientNetwork);
        dockerCont.connectContToNetwork(server, serverNetwork);

        dockerClient.startContainerCmd(client.getId()).exec();
        dockerClient.startContainerCmd(server.getId()).exec();


    }
}
