package org.project.server;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import org.project.model.ShoppingList;
import java.util.UUID;


public class Server {
    private final String address;
    private final Map<String, ShoppingList> lists;

    public Server(String address) {
        this.address = address;
        this.lists = new HashMap<>();
    }

    public String getAddress() {
        return address;
    }

    public String handleRequest(String key, String request) {
        String[] parts = request.split(":");
        String command = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "CREATE":
                return createList(key, payload);
            case "ADD":
                return addItem(key, payload);
            case "REMOVE":
                return removeItem(key, payload);
            default:
                return "invalid!";
        }
    }

    private String createList(String key, String name) {
        if (lists.containsKey(key)) {
            return "List with the ID " + key + " already exists!";
        }
        lists.put(key, new ShoppingList(UUID.randomUUID(), name, Arrays.asList()));
        return "Lista criada com ID: " + key;
    }

    private String addItem(String key, String payload) {
        ShoppingList list = lists.get(key);
        if (list == null) {
            return "Lista não encontrada.";
        }
        String[] parts = payload.split(",");
        list.addItem(parts[0], Integer.parseInt(parts[1]));
        return "Item adicionado à lista " + key;
    }

    private String removeItem(String key, String payload) {
        ShoppingList list = lists.get(key);
        if (list == null) {
            return "Lista não encontrada.";
        }
        list.removeItem(Integer.parseInt(payload));
        return "Item removido da lista " + key;
    }
}