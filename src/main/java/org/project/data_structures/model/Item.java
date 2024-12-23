package org.project.data_structures.model;

import org.project.data_structures.crdts.BGCounter;

public class Item {
    private String name;
    BGCounter counter;
    public Item(String name, long quantity) {
        this.name = name;
        this.counter = new BGCounter(quantity);
    }
    public void increaseMax(long quantity) {
        counter.setMaxValue(counter.getMaxValue() + quantity);
    }

    public long consume(String user, long quantity) {
        for (int i = 0; i < quantity; i++) {
            if (!counter.increment(user)) {
                return i;
            }
        }
        return quantity;
    }

    public Item merge(Item other) {
        if (!this.name.equals(other.name)) {
            throw new IllegalArgumentException("Items must have the same name to be merged");
        }
        Item mergedItem = new Item(this.name, 0);
        mergedItem.counter = this.counter.merge(other.counter);
        return mergedItem;
    }

    public String toString() {
        return "NAME: " + name + " | QUANTITY: " + counter.query() + " / " + counter.getMaxValue();
    }
}
