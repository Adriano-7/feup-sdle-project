package org.project.server;

import org.project.model.ShoppingList;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancer {
    private final HashRing hashRing;
    private final List<Server> servers;

    public LoadBalancer(int virtualNodes) {
        hashRing = new HashRing(virtualNodes);
        servers = new ArrayList<>();
    }

    public void addServer(Server server) {
        servers.add(server);
        hashRing.addServer(server.getName());
    }

    public void removeServer(Server server) {
        servers.remove(server);
        hashRing.removeServer(server.getName());
    }

    public String handleRequest(String key, String request) {
        String serverName = hashRing.getServer(key);
        Server targetServer = servers.stream()
                .filter(server -> server.getName().equals(serverName))
                .findFirst()
                .orElse(null);

        if (targetServer == null) {
            return "Error: Server not found.";
        }

        ShoppingList shoppingList = targetServer.retrieve(key);

        if (request.startsWith("CREATE:")) {
            String name = request.substring(7);
            ShoppingList newList = new ShoppingList(name);
            targetServer.store(key, newList);
            return "List created in server: " + serverName;
        }

        if (request.startsWith("ADD:")) {
            if (shoppingList == null) {
                return "Error: List not found.";
            }

            String[] parts = request.substring(4).split(",", 2);
            String itemName = parts[0];
            int quantity = Integer.parseInt(parts[1]);
            shoppingList.addItem(itemName, quantity);
            return "Item added to the list in server: " + serverName;
        }

        return "Operation unknown.";
    }

}