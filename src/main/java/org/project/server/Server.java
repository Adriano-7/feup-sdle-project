package org.project.server;

import org.project.model.ShoppingList;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final String name;
    private final int port;
    private final ConcurrentHashMap<String, ShoppingList> database = new ConcurrentHashMap<>();

    public Server(String name, int port) {
        this.name = name;
        this.port = port;
    }

    public void store(String key, ShoppingList list) {
        database.put(key, list);
    }

    public ShoppingList retrieve(String key) {
        return database.get(key);
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public Map<String, ShoppingList> getData() {
        return new ConcurrentHashMap<>(database);
    }

    public void run() {
        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.bind(new InetSocketAddress(port));
            serverSocket.configureBlocking(false);

            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server " + name + " started on port " + port);

            while (true) {
                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        acceptConnection(serverSocket, selector);
                    } else if (key.isReadable()) {
                        handleRequest(key);
                    }
                }
                selector.selectedKeys().clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptConnection(ServerSocketChannel serverSocket, Selector selector) throws IOException {
        SocketChannel clientChannel = serverSocket.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Client connected to server " + name);
    }

    private void handleRequest(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        try {
            Message message = Message.read(clientChannel);
            if (message == null) return;

            processMessage(clientChannel, message);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processMessage(SocketChannel clientChannel, Message message) throws IOException {
        String operation = message.getOperation();
        String key = message.getKey();
        String value = message.getValue();

        if ("CREATE".equals(operation)) {
            ShoppingList newList = new ShoppingList(value);
            store(key, newList);
            sendResponse(clientChannel, "List created: " + value);
        } else if ("ADD".equals(operation)) {
            ShoppingList list = retrieve(key);
            if (list == null) {
                sendResponse(clientChannel, "Error: List not found.");
            } else {
                String[] parts = value.split(",", 2);
                list.addItem(parts[0], Integer.parseInt(parts[1]));
                sendResponse(clientChannel, "Item added: " + parts[0]);
            }
        }
    }

    private void sendResponse(SocketChannel clientChannel, String response) throws IOException {
        Message responseMessage = new Message("RESPONSE", null, response);
        responseMessage.send(clientChannel);
    }
}