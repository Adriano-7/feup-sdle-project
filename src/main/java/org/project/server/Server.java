package org.project.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.project.data_structures.LWWSet;
import org.project.data_structures.LWWSetSerializer;
import org.project.data_structures.ShoppingListDeserializer;
import org.project.model.ShoppingList;
import org.project.server.database.ServerDB;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 5555;
    private final Map<String, ShoppingList> shoppingLists;
    private final Gson gson;

    public Server() {
        this.shoppingLists = new ConcurrentHashMap<>(ServerDB.loadShoppingLists());
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LWWSet.class, new LWWSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .setPrettyPrinting()
                .create();
    }

    public void start() {
        System.out.println("Starting Server on port " + PORT);

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:" + PORT);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] request = socket.recv(0);
                    String response = processRequest(new String(request, ZMQ.CHARSET));
                    socket.send(response.getBytes(ZMQ.CHARSET), 0);
                } catch (Exception e) {
                    System.out.println("Error processing request: " + e.getMessage());
                    socket.send("error/server_error".getBytes(ZMQ.CHARSET), 0);
                }
            }
        }
    }

    private String processRequest(String message) {
        try {
            if (message.equals("ping")) {
                return "pong";
            }
            if (message.startsWith("read/")) {
                return handleReadCommand(message.substring(5));
            }
            if (message.startsWith("write/")) {
                return handleWriteCommand(message.substring(6));
            }
            if (message.startsWith("delete/")) {
                return handleDeleteCommand(message.substring(7));
            }
            throw new IllegalArgumentException("Unknown command");
        } catch (IllegalArgumentException e) {
            System.out.println("Request processing error: " + e.getMessage());
            return "error/" + e.getMessage().toLowerCase().replace(" ", "_");
        }
    }

    private String handleReadCommand(String id) {
        System.out.println("Reading shopping list: " + id);

        ShoppingList shoppingList = shoppingLists.get(id);
        if (shoppingList == null) {
            throw new IllegalArgumentException("List Not Found");
        }
        if (shoppingList.isDeleted()) {
            System.out.println("List is deleted");
            return "error/list_deleted";
        }

        return gson.toJson(shoppingList);
    }

    private String handleWriteCommand(String message) {
        try {
            ShoppingList incomingList = gson.fromJson(message, ShoppingList.class);
            String id = incomingList.getID().toString();

            ShoppingList existingList = shoppingLists.get(id);
            if (existingList == null){
                shoppingLists.put(id, incomingList);
                ServerDB.saveShoppingList(incomingList);
                return gson.toJson(incomingList);
            }
            ShoppingList updatedList = existingList.merge(incomingList);
            ServerDB.saveShoppingList(updatedList);
            shoppingLists.put(id, updatedList);
            if (updatedList.isDeleted()) {
                return "error/list_deleted";
            }
            return gson.toJson(updatedList);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Shopping List Data");
        }
    }

    private String handleDeleteCommand(String id) {
        System.out.println("Deleting shopping list: " + id);

        ShoppingList shoppingList = shoppingLists.get(id);
        if (shoppingList == null) {
            throw new IllegalArgumentException("List Not Found");
        }
        shoppingList.setDeleted();
        shoppingLists.put(id, shoppingList);
        ServerDB.saveShoppingList(shoppingList);
        return "success/deleted";
    }

    public static void main(String[] args) {
        new Server().start();
    }
}