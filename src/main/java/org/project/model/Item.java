package org.project.model;
import java.util.List;
import java.util.UUID;

public class Item {
    private int id;
    private String name;
    private int quantity;

    public Item(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void consume(){
        this.quantity = Math.max(0, this.quantity-1);
    }
}
