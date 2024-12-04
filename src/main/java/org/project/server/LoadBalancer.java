package org.project.server;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class LoadBalancer {
    private final List<String> servers;
    private final SortedMap<Integer, String> hashRing;

    public LoadBalancer() {
        servers = new ArrayList<>();
        hashRing = new TreeMap<>();
    }

    public void addServer(String serverAddress) {
        int hash = serverAddress.hashCode();
        hashRing.put(hash, serverAddress);
        servers.add(serverAddress);
        System.out.println("Servidor adicionado: " + serverAddress);
    }

    public void removeServer(String serverAddress) {
        int hash = serverAddress.hashCode();
        hashRing.remove(hash);
        servers.remove(serverAddress);
        System.out.println("Servidor removido: " + serverAddress);
    }

    public String getServer(String key) {
        if (hashRing.isEmpty()) {
            throw new IllegalStateException("Nenhum servidor disponível no Hash Ring!");
        }

        int hash = key.hashCode();
        // Search the next bigger hash in hash ring
        SortedMap<Integer, String> tailMap = hashRing.tailMap(hash);
        Integer serverHash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        return hashRing.get(serverHash);
    }
    
    public String processRequest(String key, String request) {
        String serverAddress = getServer(key);
        return "Requisição '" + request + "' processada por: " + serverAddress;
    }

    public List<String> getServers() {
        return servers;
    }
}
