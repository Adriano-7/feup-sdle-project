package org.project.server;

import java.security.MessageDigest;
import java.util.*;

public class HashRing {
    private final SortedMap<Integer, String> ring = new TreeMap<>();
    private final int virtualNodes;

    public HashRing(int virtualNodes) {
        this.virtualNodes = virtualNodes;
    }

    public void addServer(String server) {
        for (int i = 0; i < virtualNodes; i++) {
            int hash = hash(server + "#" + i);
            ring.put(hash, server);
        }
    }

    public void removeServer(String server) {
        for (int i = 0; i < virtualNodes; i++) {
            int hash = hash(server + "#" + i);
            ring.remove(hash);
        }
    }

    public String getServer(String key) {
        if (ring.isEmpty()) return null;
        int hash = hash(key);
        if (!ring.containsKey(hash)) {
            return ring.get(ring.tailMap(hash).firstKey());
        }
        return ring.get(hash);
    }

    private int hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(key.getBytes());
            return Arrays.hashCode(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
