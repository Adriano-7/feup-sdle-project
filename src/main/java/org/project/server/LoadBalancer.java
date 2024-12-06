package org.project.server;

import org.project.model.ShoppingList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;

public class LoadBalancer {
    private final Selector selector;
    private final ServerSocketChannel serverSocket;
    private final Map<String, Server> nodes;
    private final HashRing hashRing;

    public LoadBalancer(int port) throws IOException {
        this.selector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress(port));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        this.nodes = new HashMap<>();
        this.hashRing = new HashRing(3);
    }

    public void run() {
        System.out.println("Starting Load Balancer...");
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isAcceptable()) {
                        acceptConnection();
                    } else if (key.isReadable()) {
                        handleRequest(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void acceptConnection() throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        System.out.println("Client connected: " + client.getRemoteAddress());
    }

    private void handleRequest(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        try {
            Message message = Message.read(client);
            if (message == null) return;

            processMessage(client, message);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(SocketChannel client, Message message) throws IOException {
        String operation = message.getOperation();
        String key = message.getKey();
        String value = message.getValue();

        String serverId = hashRing.getServer(key);
        Server targetServer = nodes.get(serverId);

        if ("CREATE".equals(operation)) {
            ShoppingList newList = new ShoppingList(value);
            targetServer.store(key, newList);
            replicateData(key, newList, serverId);
            sendResponse(client, "List created: " + value);
        } else if ("ADD".equals(operation)) {
            ShoppingList list = targetServer.retrieve(key);
            if (list == null) {
                sendResponse(client, "Error: List not found.");
            } else {
                String[] parts = value.split(",", 2);
                list.addItem(parts[0], Integer.parseInt(parts[1]));
                sendResponse(client, "Item added: " + parts[0]);
            }
        }
    }

    private void replicateData(String key, ShoppingList data, String primaryServerId) {
        int replicationFactor = 2;
        List<String> predecessors = hashRing.getPredecessors(primaryServerId, replicationFactor);

        for (String predId : predecessors) {
            Server predServer = nodes.get(predId);
            if (predServer != null) {
                predServer.store(key, data);
                System.out.println("Data replicated to server: " + predServer.getName());
            }
        }
    }

    private void sendResponse(SocketChannel client, String response) throws IOException {
        Message responseMessage = new Message("RESPONSE", null, response);
        responseMessage.send(client);
    }

    public void addNode(Server server) {
        nodes.put(server.getName(), server);
        hashRing.addServer(server.getName());
        System.out.println("Server added: " + server.getName());
    }
}