package org.project.server.loadBalancing;

import org.zeromq.ZFrame;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.TreeMap;

public class HashRing {
    private final TreeMap<Integer, ZFrame> ring = new TreeMap<>();

    public void addWorker(ZFrame worker) {
        int hash = hash(worker.toString());
        ring.put(hash, worker);
    }

    public void removeWorker(ZFrame worker) {
        int hash = hash(worker.toString());
        ring.remove(hash);
    }

    public ZFrame getWorker(String key) {
        if (ring.isEmpty()) {
            return null;
        }
        int hash = hash(key);
        if (!ring.containsKey(hash)) {
            // If the exact hash is not found, return the worker associated with the smallest entry greater than or equal to the hash.
            return ring.ceilingEntry(hash) != null ? ring.ceilingEntry(hash).getValue() : ring.firstEntry().getValue();
        }
        return ring.get(hash);
    }

    private int hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return ((digest[0] & 0xFF) << 24) | ((digest[1] & 0xFF) << 16) | ((digest[2] & 0xFF) << 8) | (digest[3] & 0xFF);
        } catch (Exception e) {
            throw new RuntimeException("Hashing error", e);
        }
    }
}
