package org.project.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;
import org.project.data_structures.LWW;

public class ShoppingList {
    private UUID listID;
    private String name;
    private LWW items;
    private LocalDateTime lastModified;

    public ShoppingList(String name) {
        this.listID = UUID.randomUUID();
        this.name = name;
        this.items = new LWW();
        this.lastModified = LocalDateTime.now();
    }

    public ShoppingList(UUID id, String name) {
        this.listID = id;
        this.name = name;
        this.items = new LWW();
        this.lastModified = LocalDateTime.now();
    }

    public ShoppingList(UUID id, String name, LWW items) {
        this.listID = id;
        this.name = name;
        this.items = items;
        this.lastModified = LocalDateTime.now();
    }

    public String getID() {
        return listID.toString();
    }

    public String getName() {
        return name;
    }

    public LWW getItems() {
        return items;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void addItem(String name, int quantity) {
        this.items.add(new Item(name, quantity));
        this.lastModified = LocalDateTime.now();
    }

    public void removeItem(String id) {
        Item item = this.items.get(id);
        this.items.remove(item);
        this.lastModified = LocalDateTime.now();
    }

    public long consumeItem(String id, String user, int quantity) {
        Item item = this.items.get(id);
        return item.consume(user, quantity);
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
