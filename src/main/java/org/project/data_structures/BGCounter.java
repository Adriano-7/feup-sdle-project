package org.project.data_structures;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BGCounter implements Serializable {
    private final ConcurrentHashMap<String, AtomicLong> payload;
    private long maxValue;

    public BGCounter(long maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("Maximum value must be non-negative");
        }
        this.payload = new ConcurrentHashMap<>();
        this.maxValue = maxValue;
    }

    public BGCounter(long maxValue, ConcurrentHashMap<String, AtomicLong> payload){
        this.maxValue = maxValue;
        this.payload = payload;
    }

    public void addNode(String nodeId) {
        Objects.requireNonNull(nodeId, "Node identifier cannot be null");

        if (payload.containsKey(nodeId)) {
            throw new IllegalArgumentException("Node " + nodeId + " already exists");
        }

        payload.put(nodeId, new AtomicLong(0));
    }

    public boolean increment(String nodeId) {
        if (!payload.containsKey(nodeId)) {
            addNode(nodeId);
        }
        AtomicLong counter = payload.get(nodeId);

        if (counter == null) {
            throw new IllegalStateException("Node " + nodeId + " does not exist");
        }

        // If max value is exceeded, increment is not allowed
        if (query() >= maxValue) {
            return false;
        }
        return counter.incrementAndGet() <= maxValue;
    }

    public long query() {
        return payload.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }

    public BGCounter merge(BGCounter other) {
        Objects.requireNonNull(other, "Cannot merge with null counter");

        long mergedMaxValue = Math.max(this.maxValue, other.maxValue);
        BGCounter mergedCounter = new BGCounter(mergedMaxValue);

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
        return "BGCounter{" +
                ", maxValue=" + maxValue +
                ", nodes=" + payload +
                ", totalCount=" + query() +
                '}';
    }

    public BGCounter clone() {
        BGCounter clonedCounter = new BGCounter(this.maxValue);

        payload.forEach((nodeId, value) -> {
            clonedCounter.addNode(nodeId);
            clonedCounter.payload.get(nodeId).set(value.get());
        });

        return clonedCounter;
    }

    public long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(long maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("Maximum value must be non-negative");
        }
        this.maxValue = maxValue;
    }
}