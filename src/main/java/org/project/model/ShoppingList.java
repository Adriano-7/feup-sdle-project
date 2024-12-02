package org.project.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.project.data_structures.LWWSet;

public class ShoppingList {
    private UUID listID;
    private String name;
    private LWWSet items;

    public ShoppingList(String name) {
        this.listID = UUID.randomUUID();
        this.name = name;
        this.items = new LWWSet();
    }

    public ShoppingList(UUID id, String name) {
        this.listID = id;
        this.name = name;
        this.items = new LWWSet();
    }

    public ShoppingList(UUID id, String name, LWWSet items) {
        this.listID = id;
        this.name = name;
        this.items = items;
    }

    public String getID() {
        return listID.toString();
    }

    public String getName() {
        return name;
    }

    public LWWSet getItems() {
        return items;
    }

    public void addItem(String name, int quantity) {
        this.items.add(name, quantity);
    }

    public void removeItem(String id) {
        this.items.remove(id);
    }

    public long consumeItem(String id, String user, int quantity) {
        return items.consumeItem(id, user, quantity);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("\n>> LIST: " + name + "\n>> ID: " + listID + "\n");
        if (!items.isEmpty()) {
            s.append(">> Items:\n");
            s.append(items.toString());
        }
        else s.append(">> No items in the shopping list.");
        return s.append("\n").toString();
    }
}
