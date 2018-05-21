package network;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateNetworkResponse;
import com.github.dockerjava.api.model.Network;

public class DockerNetwork {
    public String createNetworkWithSubnet(String subnet, String name, DockerClient dockerClient, String gateway) {
        Network.Ipam ipam = new Network.Ipam().withConfig(new Network.Ipam.Config().withSubnet(subnet).withGateway(gateway));

        CreateNetworkResponse networkResponse = dockerClient.createNetworkCmd().withName(name)
                .withDriver("bridge")
                .withIpam(ipam)
                .exec();
        return networkResponse.getId();
    }

}