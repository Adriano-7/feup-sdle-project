package org.project.model;
import org.project.data_structures.BGCounter;

import java.util.List;
import java.util.UUID;
import java.util.Comparator;

public class Item implements Comparable<Item> {
    private UUID itemID;
    private String name;
    BGCounter<String> counter;

    public Item(String name, int quantity) {
        this.itemID = UUID.randomUUID();
        this.name = name;
        this.counter = new BGCounter<>(quantity);
    }

    public String getName() {
        return name;
    }

    public long getQuantity() {
        return counter.query();
    }

    public long consume(String user, long quantity) {
        for (int i = 0; i < quantity; i++) {
            if (!counter.increment(user)) {
                return i;
            }
        }
        return quantity;
    }

    public String toString() {
        return "NAME: " + name + " | QUANTITY: " + counter.query() + " / " + counter.getMaxValue() + " | ID: " + itemID;
    }

    public String getID() {
        return itemID.toString();
    }


    @Override
    public int compareTo(Item o) {
        return Comparator.comparing(Item::getID).compare(this, o);
    }

}
