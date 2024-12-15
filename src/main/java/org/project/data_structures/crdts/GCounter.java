package org.project.data_structures.crdts;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class GCounter implements Serializable {
    private final ConcurrentHashMap<String, AtomicLong> payload;

    public GCounter() {
        this.payload = new ConcurrentHashMap<>();
    }

    public void addNode(String nodeId) {
        Objects.requireNonNull(nodeId, "Node identifier cannot be null");

        if (payload.containsKey(nodeId)) {
            throw new IllegalArgumentException("Node " + nodeId + " already exists");
        }

        payload.put(nodeId, new AtomicLong(0));
    }

    public void increment(String nodeId) {
        if (!payload.containsKey(nodeId)) {
            addNode(nodeId);
        }
        AtomicLong counter = payload.get(nodeId);
        counter.incrementAndGet();

        if (counter == null) {
            throw new IllegalStateException("Node " + nodeId + " does not exist");
        }
    }

    public long query() {
        return payload.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }

    public GCounter merge(GCounter other) {
        Objects.requireNonNull(other, "Cannot merge with null counter");

        GCounter mergedCounter = new GCounter();

        payload.keySet().forEach(mergedCounter::addNode);

        for (String key : other.payload.keySet()) {
            if (!mergedCounter.payload.containsKey(key)) {
                mergedCounter.addNode(key);
            }
        }

        for (String nodeId : mergedCounter.payload.keySet()) {
            long thisValue = this.payload.containsKey(nodeId)
                    ? this.payload.get(nodeId).get()
                    : 0;

            long otherValue = other.payload.containsKey(nodeId)
                    ? other.payload.get(nodeId).get()
                    : 0;

            mergedCounter.payload.get(nodeId).set(Math.max(thisValue, otherValue));
        }

        return mergedCounter;
    }

    @Override
    public String toString() {
        return "GCounter{" +
                ", nodes=" + payload +
                ", totalCount=" + query() +
                '}';
    }

    public GCounter clone() {
        GCounter clonedCounter = new GCounter();

        payload.forEach((nodeId, value) -> {
            clonedCounter.addNode(nodeId);
            clonedCounter.payload.get(nodeId).set(value.get());
        });

        return clonedCounter;
    }

    public ConcurrentHashMap<String, AtomicLong> getPayload() {
        return payload;
    }
}