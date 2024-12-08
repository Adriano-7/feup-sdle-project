package org.project.client;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.project.data_structures.LWWSet;
import org.project.data_structures.LWWSetSerializer;
import org.project.data_structures.ShoppingListDeserializer;
import org.project.model.ShoppingList;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommunicationHandler implements Runnable {
    private final String serverAddress;
    private final BlockingQueue<String> commandQueue;
    private final BlockingQueue<String> responseQueue;
    private final AtomicBoolean isRunning;
    private final Gson gson;

    public static final String READ_COMMAND = "read";
    public static final String WRITE_COMMAND = "write";

    public CommunicationHandler(String serverAddress) {
        this.serverAddress = serverAddress;
        this.commandQueue = new LinkedBlockingQueue<>();
        this.responseQueue = new LinkedBlockingQueue<>();
        this.isRunning = new AtomicBoolean(false);

        this.gson = new GsonBuilder()
                .registerTypeAdapter(LWWSet.class, new LWWSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .create();
    }
    @Override
    public void run() {
        try (ZContext context = new ZContext()) {
            ZMQ.Socket socket = context.createSocket(ZMQ.REQ);
            socket.connect(serverAddress);

            isRunning.set(true);

            while (isRunning.get()) {
                String command = commandQueue.take();

                socket.send(command.getBytes(ZMQ.CHARSET), 0);

                byte[] responseBytes = socket.recv(0);
                String response = new String(responseBytes, ZMQ.CHARSET);

                responseQueue.put(response);
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
        commandQueue.put(WRITE_COMMAND + "/" + shoppingListJson);
    }
    public String getResponse() throws InterruptedException {
        return responseQueue.take();
    }

    public void stop() {
        isRunning.set(false);
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
        return isRunning.get();
    }
}