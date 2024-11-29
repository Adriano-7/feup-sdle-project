package org.project.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShoppingList {
    private UUID listID;
    private String name;
    private List<Item> items;
    private LocalDateTime lastModified;

    public ShoppingList(String name) {
        this.listID = UUID.randomUUID();
        this.name = name;
        this.items = new ArrayList<>();
        this.lastModified = LocalDateTime.now();
    }

    public ShoppingList(UUID id, String name) {
        this.listID = id;
        this.name = name;
        this.items = new ArrayList<>();
        this.lastModified = LocalDateTime.now();
    }

    public ShoppingList(UUID id, String name, List<Item> items) {
        this.listID = id;
        this.name = name;
        this.items = new ArrayList<>(items);
        this.lastModified = LocalDateTime.now();
    }

    public String getID() {
        return listID.toString();
    }

    public String getName() {
        return name;
    }

    public List<Item> getItems() {
        return items;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void addItem(String name, int quantity) {
        this.items.add(new Item(name, quantity));
        this.lastModified = LocalDateTime.now();
    }

    public void removeItem(int index) {
        this.items.remove(index);
        this.lastModified = LocalDateTime.now();
    }

    public long consumeItem(int index, String user, int quantity) {
        Item item = this.items.get(index);
        return item.consume(user, quantity);
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("\n>> LIST: " + name + "\n>> ID: " + listID + "\n");
        if (!items.isEmpty()) {
            s.append(">> Items:\n");
            for (Item item : items) {
                s.append("  >> ").append(item).append("\n");
            }
        }
        else s.append(">> No items in the shopping list.");
        return s.append("\n").toString();
    }
}
