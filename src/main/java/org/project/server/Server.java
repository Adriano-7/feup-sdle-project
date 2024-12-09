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
    private final int port;
    private final Map<String, ShoppingList> shoppingLists;
    private final Gson gson;

    public Server(int port) {
        this.port = port;
        this.shoppingLists = new ConcurrentHashMap<>(ServerDB.loadShoppingLists());
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LWWSet.class, new LWWSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .setPrettyPrinting()
                .create();
    }

    public void start() {
        System.out.println("Starting Server on port " + port);

        try (ZContext context = new ZContext()) {
            ZMQ.Socket repSocket = context.createSocket(SocketType.REP);
            repSocket.bind("tcp://*:" + port);

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    byte[] request = repSocket.recv(0);
                    String response = processRequest(new String(request, ZMQ.CHARSET));
                    repSocket.send(response.getBytes(ZMQ.CHARSET), 0);
                } catch (Exception e) {
                    System.out.println("Error processing request: " + e.getMessage());
                    repSocket.send("error/server_error".getBytes(ZMQ.CHARSET), 0);
                }
            }
        }
    }

    private String processRequest(String message) {
        try {
            if (message.equals("ping")) {
                return "pong"; // Respond to ping for status check
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

        // Returns the list as JSON
        return gson.toJson(shoppingList);
    }

    private String handleWriteCommand(String message) {
        try {
            // Deserializes the shopping list received
            ShoppingList incomingList = gson.fromJson(message, ShoppingList.class);
            String id = incomingList.getID().toString();

            // Update or create a new shopping list
            ShoppingList existingList = shoppingLists.get(id);
            if (existingList == null) {
                shoppingLists.put(id, incomingList);
                ServerDB.saveShoppingList(incomingList);
                return gson.toJson(incomingList);
            }

            // Merge lists if there is already a list with the same ID
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
        if (args.length < 1) {
            System.out.println("Please specify the port for the server.");
            return;
        }

        try {
            int port = Integer.parseInt(args[0]);
            new Server(port).start();
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[0]);
        }
    }
}
