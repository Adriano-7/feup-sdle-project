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

public class Server {
    private static Map<String, ShoppingList> shoppingLists;
    private static Gson gson;

    public static void main(String[] args) {
        shoppingLists = ServerDB.loadShoppingLists();
        gson = new GsonBuilder()
                .registerTypeAdapter(LWWSet.class, new LWWSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .create();

        System.out.println("Starting server...");

        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(SocketType.REP);
            socket.bind("tcp://*:5555");

            while (!Thread.currentThread().isInterrupted()) {
                byte[] reply = socket.recv(0);
                String response = processCommand(new String(reply, ZMQ.CHARSET));
                socket.send(response.getBytes(ZMQ.CHARSET), 0);
            }
        }
    }

    /*
    Server commands:
    - read/<id>: Read the shopping list with the given ID.
    - write/<shoppingList>: Merge the shopping list received with the server's version, update it, and return the updated shopping list.
    */
    private static String processCommand(String message) {
        if (message.startsWith("read/")) {
            return handleReadCommand(message.substring(5));
        }
        if (message.startsWith("write/")) {
            return handleWriteCommand(message.substring(6));
        }

        return "Unknown command.";
    }

    private static String handleReadCommand(String id) {
        System.out.println("Handling read command for shopping list with ID: " + id);

        ShoppingList shoppingList = shoppingLists.get(id);
        if (shoppingList == null) {
            System.out.println("Shopping list not found.");
            return "Shopping list not found.";
        } else {
            return gson.toJson(shoppingList);
        }
    }

    private static String handleWriteCommand(String message) {
        String[] parts = message.split("/");
        String shoppingListJson = parts[0];
        ShoppingList shoppingList = gson.fromJson(shoppingListJson, ShoppingList.class);
        String id = shoppingList.getID().toString();

        System.out.println("Handling write command for shopping list with ID: " + id);

        ShoppingList serverShoppingList = shoppingLists.get(id);
        if (serverShoppingList == null) {
            shoppingLists.put(id, shoppingList);
        }
        else {
            shoppingList = serverShoppingList.merge(shoppingList);
            shoppingLists.put(id, shoppingList);
        }
        return gson.toJson(shoppingList);
    }
}