package org.project.server;

import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import org.project.model.ShoppingList;
import java.util.UUID;


public class Server {
    private final String address;
    private final Map<String, ShoppingList> lists;

    public Server(String address) {
        this.address = address;
        this.lists = new HashMap<>();
    }

    public String getAddress() {
        return address;
    }

}