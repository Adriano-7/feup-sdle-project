package org.project.model;
import org.project.data_structures.BGCounter;

import java.util.List;
import java.util.UUID;

public class Item {
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

    public boolean consume(String user){
        return counter.increment(user);
    }

    public String toString() {
        return "NAME: " + name + " | QUANTITY: " + counter.query() + " / " + counter.getMaxValue();
    }
}
