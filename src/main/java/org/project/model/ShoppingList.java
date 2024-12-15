package org.project.model;

import java.time.LocalDateTime;
import java.util.UUID;
import org.project.data_structures.test.AWORSet;

public class ShoppingList {
    private UUID listID;
    private String name;
    private AWORSet items;
    private boolean isDeleted;

    public ShoppingList(String name) {
        this.listID = UUID.randomUUID();
        this.name = name;
        this.items = new AWORSet();
        this.isDeleted = false;
    }

    public ShoppingList(UUID id, String name) {
        this.listID = id;
        this.name = name;
        this.items = new AWORSet();
        this.isDeleted = false;
    }

    public ShoppingList(UUID id, String name, AWORSet items) {
        this.listID = id;
        this.name = name;
        this.items = items;
        this.isDeleted = false;
    }

    public ShoppingList(UUID id, String name, AWORSet items, boolean isDeleted) {
        this.listID = id;
        this.name = name;
        this.items = items;
        this.isDeleted = isDeleted;
    }

    public UUID getID() {return listID;}

    public String getName() {
        return name;
    }

    public AWORSet getItems() {
        return items;
    }

    public void addItem(String nodeId, String name, int quantity) {
        this.items.add(nodeId, name, quantity);
    }

    public void removeItem(String nodeId, String id) {
        this.items.remove(nodeId, id);
    }

    public long consumeItem(String id, String user, int quantity) {
        return items.consumeItem(id, user, quantity);
    }

    public boolean hasItem(String id) {
        return items.lookup(id);
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted() {
        isDeleted = true;
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

    public ShoppingList merge(ShoppingList other) {
        AWORSet mergedItems = items.merge(other.getItems());
        return new ShoppingList(listID, name, mergedItems, this.isDeleted || other.isDeleted);
    }
}
