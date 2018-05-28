import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecCreateCmdImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.jaxrs.ExecCreateCmdExec;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import container.DockerCont;
import image.DockerImage;
import network.DockerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.TimeUnit;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        DockerClient dockerClient = DockerClientBuilder.getInstance(DefaultDockerClientConfig.createDefaultConfigBuilder().build()).build();

        String tag = "docker_test:01";
        DockerImage dockerImage = new DockerImage();
        dockerImage.buildImage(dockerClient, "docker_test:01", new File("/home/obabec/Desktop/images"));

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
        NetworkSettings containerNetworkSettings = containerResponse.getNetworkSettings();

        String[] commands = new String[2];
        commands[0] = "./setGW";
        commands[1] = containerNetworkSettings.getNetworks().get(clientNetwork.getName()).getIpAddress();
        dockerCont.runCommand(client,  commands);

        commands[1] = containerNetworkSettings.getNetworks().get(serverNetwork.getName()).getIpAddress();
        dockerCont.runCommand(server, commands);

        logger.info("Server response" + containerNetworkSettings.getNetworks().get(serverNetwork.getName()).getIpAddress());
        logger.info("Client response" + containerNetworkSettings.getNetworks().get(clientNetwork.getName()).getIpAddress());


    }
}
