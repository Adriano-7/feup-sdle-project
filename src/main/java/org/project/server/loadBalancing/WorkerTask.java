package org.project.server.loadBalancing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.project.data_structures.serializers.ShoppingListDeserializer;
import org.project.data_structures.crdts.AWORSet;
import org.project.data_structures.serializers.AWORSetSerializer;
import org.project.data_structures.model.ShoppingList;
import org.project.server.database.ServerDB;
import org.zeromq.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorkerTask implements ZThread.IDetachedRunnable {
    private final int workerNbr;
    private static final byte[] WORKER_READY = { '\001' };
    private final Map<String, ShoppingList> shoppingLists;
    private final Gson gson;

    public WorkerTask(int workerNbr) {
        this.workerNbr = workerNbr;
        this.shoppingLists = new ConcurrentHashMap<>(ServerDB.loadShoppingLists(workerNbr));
        this.gson = new GsonBuilder()
                .registerTypeAdapter(AWORSet.class, new AWORSetSerializer())
                .registerTypeAdapter(ShoppingList.class, new ShoppingListDeserializer())
                .setPrettyPrinting()
                .create();
    }
    @Override
    public void run(Object[] args) {
        System.out.println("Starting Worker: " + workerNbr);

        try (ZContext context = new ZContext()) {
            ZMQ.Socket worker = context.createSocket(SocketType.REQ);
            worker.setIdentity(("W" + Math.random()).getBytes());
            worker.connect("ipc://backend.ipc");

            // Notify backend of readiness
            ZFrame frame = new ZFrame(WORKER_READY);
            frame.send(worker, 0);

            while (true) {
                ZMsg msg = ZMsg.recvMsg(worker);
                if (msg == null)
                    break;

                String response = processRequest(new String(msg.getLast().getData(), ZMQ.CHARSET));

                msg.getLast().reset(response);
                msg.send(worker);
            }
        }
    }

    private String processRequest(String message) {
        try {
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
        System.out.println("Worker " + workerNbr + " is reading shopping list: " + id);

        ShoppingList shoppingList = shoppingLists.get(id);
        if (shoppingList == null) {
            System.out.println("Worker " + workerNbr + " list not found: " + id);
            throw new IllegalArgumentException("List Not Found");
        }
        if (shoppingList.isDeleted()) {
            System.out.println("Worker " + workerNbr + " List is deleted");
            return "error/list_deleted";
        }

        return gson.toJson(shoppingList);
    }

    private String handleWriteCommand(String message) {
        //command: listID/shoppingListJson
        String[] parts = message.split("/", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid Write Command");
        }
        String id = parts[0];
        String shoppingList = parts[1];

        System.out.println("Worker " + workerNbr + " syncing shopping list: " + id);

        try {
            ShoppingList incomingList = gson.fromJson(shoppingList, ShoppingList.class);

            ShoppingList existingList = shoppingLists.get(id);
            if (existingList == null){
                shoppingLists.put(id, incomingList);
                ServerDB.saveShoppingList(incomingList, workerNbr);
                return gson.toJson(incomingList);
            }
            ShoppingList updatedList = existingList.merge(incomingList);
            ServerDB.saveShoppingList(updatedList, workerNbr);
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
        System.out.println("Worker " + workerNbr + " is deleting shopping list: " + id);

        ShoppingList shoppingList = shoppingLists.get(id);
        if (shoppingList == null) {
            throw new IllegalArgumentException("List Not Found");
        }
        shoppingList.setDeleted();
        shoppingLists.put(id, shoppingList);
        ServerDB.saveShoppingList(shoppingList, workerNbr);
        return "success/deleted";
    }
}