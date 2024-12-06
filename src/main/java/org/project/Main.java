package org.project;

import org.project.server.Message;
import org.project.server.Server;
import org.project.server.LoadBalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Main {
    public static void main(String[] args) {
        try {
            LoadBalancer loadBalancer = new LoadBalancer(8080);
            new Thread(loadBalancer::run).start();

            Server server1 = new Server("Server1", 9001);
            Server server2 = new Server("Server2", 9002);
            Server server3 = new Server("Server3", 9003);

            loadBalancer.addNode(server1);
            loadBalancer.addNode(server2);
            loadBalancer.addNode(server3);

            new Thread(server1::run).start();
            new Thread(server2::run).start();
            new Thread(server3::run).start();

            sendRequest("CREATE", "list1", "Supermercado");
            sendRequest("ADD", "list1", "Banana,5");
            sendRequest("CREATE", "list2", "Mercado Local");
            sendRequest("ADD", "list2", "Tomate,10");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendRequest(String operation, String key, String value) {
        try (SocketChannel clientChannel = SocketChannel.open(new InetSocketAddress("localhost", 8080))) {
            Message message = new Message(operation, key, value);
            message.send(clientChannel);

            Message response = Message.read(clientChannel);
            if (response != null) {
                System.out.println("Answer from Load Balancer: " + response.getValue());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
