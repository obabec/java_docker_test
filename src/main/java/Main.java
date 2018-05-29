import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.NetworkSettings;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import container.DockerCont;
import image.DockerImage;
import network.DockerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        DockerClient dockerClient = DockerClientBuilder.getInstance(DefaultDockerClientConfig.createDefaultConfigBuilder().build()).build();

        String tag = "docker_test:01";
        DockerImage dockerImage = new DockerImage();
        dockerImage.buildImage(dockerClient, "docker_test:01", new File("src/dockerfiles"));

        DockerNetwork dockerNetwork = new DockerNetwork();
        Network serverNetwork =  dockerNetwork.createNetworkWithSubnet("172.22.0.0/16", "server_network", dockerClient, "172.22.0.1");
        Network clientNetwork = dockerNetwork.createNetworkWithSubnet("172.23.0.0/16", "client_network", dockerClient, "172.23.0.1");

        DockerCont dockerCont = new DockerCont(dockerClient);
        CreateContainerResponse router = dockerCont.createContainer(tag, "router");


        dockerCont.connectContToNetwork(router, clientNetwork.getId());
        dockerCont.connectContToNetwork(router, serverNetwork.getId());
        dockerClient.startContainerCmd(router.getId()).exec();


        CreateContainerResponse client = dockerCont.createContainer(tag, "comm_client");
        CreateContainerResponse server = dockerCont.createContainer(tag, "comm_server");

        dockerCont.connectContToNetwork(client, clientNetwork.getId());
        dockerCont.connectContToNetwork(server, serverNetwork.getId());

        dockerClient.startContainerCmd(client.getId()).exec();
        dockerClient.startContainerCmd(server.getId()).exec();
        InspectContainerResponse containerResponse = dockerClient.inspectContainerCmd("router").exec();
        NetworkSettings serverResponse = dockerClient.inspectContainerCmd("comm_server").exec().getNetworkSettings();
        NetworkSettings containerNetworkSettings = containerResponse.getNetworkSettings();

        logger.debug("Starting GW setup");

        dockerCont.runCommand(server, "java -jar /root/DataServer.jar");
        dockerCont.runCommand(server, "./setGW " + containerNetworkSettings.getNetworks().get(serverNetwork.getName()).getIpAddress());

        dockerCont.runCommand(client,  "./setGW " + containerNetworkSettings.getNetworks().get(clientNetwork.getName()).getIpAddress());

        dockerCont.runCommand(client, "java -jar /root/DataClient.jar " + serverResponse.getNetworks().get(serverNetwork.getName()).getIpAddress());

        logger.info("Server response" + containerNetworkSettings.getNetworks().get(serverNetwork.getName()).getIpAddress());
        logger.info("Client response" + containerNetworkSettings.getNetworks().get(clientNetwork.getName()).getIpAddress());


    }
}
