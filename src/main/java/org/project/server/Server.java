package org.project.server;

import org.project.model.ShoppingList;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final String name;
    private final ConcurrentHashMap<String, ShoppingList> database = new ConcurrentHashMap<>();

    public Server(String name) {
        this.name = name;
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

    public Map<String, ShoppingList> getData() {
        return new ConcurrentHashMap<>(database);
    }
}