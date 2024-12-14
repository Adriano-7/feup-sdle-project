package org.project.client;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.project.data_structures.LWWSet;
import org.project.data_structures.LWWSetSerializer;
import org.project.data_structures.ShoppingListDeserializer;
import org.project.model.ShoppingList;
import org.zeromq.ZThread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommunicationHandler implements ZThread.IDetachedRunnable {
    private final BlockingQueue<String> commandQueue;
    private final BlockingQueue<String> responseQueue;
    private final Gson gson;
    private final String username;

    public static final String READ_COMMAND = "read";
    public static final String WRITE_COMMAND = "write";

    public CommunicationHandler(String username) {
        this.username = username;
        this.commandQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LWWSet.class, new LWWSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .create();
    }
    @Override
    public void run(Object[] args) {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket client = context.createSocket(SocketType.REQ);
            client.setIdentity(("C" + username).getBytes());
            client.connect("ipc://frontend.ipc");

            // Send request, get reply
            while (true) {
                String command = commandQueue.take();
                client.send(command);

                String reply = client.recvStr();
                responseQueue.put(reply);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server communication thread interrupted");
        }
    }
    public void readShoppingList(String listId) throws InterruptedException {
        commandQueue.put(READ_COMMAND + "/" + listId);
    }
    public void writeShoppingList(ShoppingList shoppingList) throws InterruptedException {
        String shoppingListJson = gson.toJson(shoppingList);
        commandQueue.put(WRITE_COMMAND + "/" + shoppingList.getID()  + "/" + shoppingListJson);
    }
    public void deleteShoppingList(String listId) throws InterruptedException {
        commandQueue.put("delete/" + listId);
    }
    public String getResponse() throws InterruptedException {
        return responseQueue.take();
    }

    public ShoppingList parseShoppingListResponse(String response) {
        try {
            return gson.fromJson(response, ShoppingList.class);
        } catch (Exception e) {
            System.err.println("Error parsing shopping list: " + e.getMessage());
            return null;
        }
    }

    public boolean isServerRunning() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket client = context.createSocket(SocketType.REQ);
            client.setIdentity(("C_health_check_" + username).getBytes());
            client.connect("ipc://frontend.ipc");

            client.setReceiveTimeOut(1000);  // 1 seconds timeout

            try {
                client.send("ping");

                String response = client.recvStr();

                return response != null;
            } catch (Exception e) {
                return false;
            }
        }
    }
}