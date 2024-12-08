package org.project.server;

import org.project.server.Message;
import org.project.model.ShoppingList;
import org.zeromq.ZMQ;

import java.util.*;

public class LoadBalancer {
    private final Map<String, String> nodes; // ID do servidor -> Endereço ZeroMQ
    private final HashRing hashRing;

    public LoadBalancer() {
        this.nodes = new HashMap<>();
        this.hashRing = new HashRing(3);
    }

    public void run() {
        try (ZMQ.Context context = ZMQ.context(1);
             ZMQ.Socket repSocket = context.socket(ZMQ.REP)) {
            repSocket.bind("tcp://*:5555");
            System.out.println("Load Balancer is running...");

            while (!Thread.currentThread().isInterrupted()) {
                String request = repSocket.recvStr();
                Message message = Message.deserialize(request);

                String response = processMessage(message);
                repSocket.send(response);
            }
        }
    }

    private String processMessage(Message message) {
        String operation = message.getOperation();
        String key = message.getKey();
        String value = message.getValue();

        String serverId = hashRing.getServer(key);
        String serverAddress = nodes.get(serverId);

        if (serverAddress == null) {
            return "error/server_not_found";
        }

        if ("CREATE".equals(operation)) {
            boolean success = forwardToServer(serverAddress, new Message("write", key, value));
            return success ? "List created: " + value : "error/create_failed";
        } else if ("ADD".equals(operation)) {
            boolean success = forwardToServer(serverAddress, new Message("update", key, value));
            return success ? "Item added: " + value.split(",")[0] : "error/add_failed";
        }
        return "error/unknown_operation";
    }

    private boolean forwardToServer(String serverAddress, Message message) {
        try (ZMQ.Context context = ZMQ.context(1); ZMQ.Socket reqSocket = context.socket(ZMQ.REQ)) {
            reqSocket.connect(serverAddress);
            reqSocket.send(message.serialize());
            String response = reqSocket.recvStr();
            return response != null && !response.startsWith("error");
        }
    }

    public void addNode(String serverId, String serverAddress) {
        nodes.put(serverId, serverAddress);
        hashRing.addServer(serverId);
        System.out.println("Server added: " + serverId + " at " + serverAddress);
    }
}
