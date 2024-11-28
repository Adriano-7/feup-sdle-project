package org.project.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ShoppingList {
    private UUID id;
    private String name;
    private List<Item> items;
    private LocalDateTime lastModified;

    public ShoppingList(UUID id, String name, List<Item> items) {
        this.id = id;
        this.name = name;
        this.items = new ArrayList<>(items);
        this.lastModified = LocalDateTime.now();
    }

    public String getId() {
        return id.toString();
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


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(">> LIST\n>> ID: " + id + "\n>> Name: " + name + "\n");
        if (items != null){
            s.append(">> Items: (name | quantity)\n");
            for (Item item : items) {
                s.append(item.getName()).append(" | ").append(item.getQuantity()).append("\n");
            }
        }
        else s.append(">> No items in the shopping list.");
        return s.append("\n\n").toString();
    }
}
