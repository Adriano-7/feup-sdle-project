package org.project.data_structures;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BGCounter<K> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ConcurrentHashMap<K, AtomicLong> payload;
    private long maxValue;

    public BGCounter(long maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("Maximum value must be non-negative");
        }
        this.payload = new ConcurrentHashMap<>();
        this.maxValue = maxValue;
    }

    public BGCounter(long maxValue, ConcurrentHashMap<K, AtomicLong> payload){
        this.maxValue = maxValue;
        this.payload = payload;
    }

    public void addNode(K nodeId) {
        Objects.requireNonNull(nodeId, "Node identifier cannot be null");

        if (payload.containsKey(nodeId)) {
            throw new IllegalArgumentException("Node " + nodeId + " already exists");
        }

        payload.put(nodeId, new AtomicLong(0));
    }

    public boolean increment(K nodeId) {
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

    public BGCounter<K> merge(BGCounter<K> other) {
        Objects.requireNonNull(other, "Cannot merge with null counter");

        // Use the maximum of the two max values to ensure safety
        long mergedMaxValue = Math.max(this.maxValue, other.maxValue);
        BGCounter<K> mergedCounter = new BGCounter<>(mergedMaxValue);

        // Combine nodes from both counters
        payload.keySet().forEach(mergedCounter::addNode);

        // Add any additional nodes from the other counter
        for (K key : other.payload.keySet()) {
            if (!mergedCounter.payload.containsKey(key)) {
                mergedCounter.addNode(key);
            }
        }

        // Perform element-wise max merge
        // All nodes are considered, even if max value is exceeded. This is a design choice.
        for (K nodeId : mergedCounter.payload.keySet()) {
            long thisValue = this.payload.containsKey(nodeId)
                    ? this.payload.get(nodeId).get()
                    : 0;

            long otherValue = other.payload.containsKey(nodeId)
                    ? other.payload.get(nodeId).get()
                    : 0;

            mergedCounter.payload.get(nodeId).set(thisValue + otherValue);
        }

        return mergedCounter;
    }

    private long calculateMaxAllowedForNode(BGCounter<K> counter, K currentNodeId, long maxTotalValue) {
        // Calculate current total of other nodes
        long otherNodesTotal = counter.payload.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(currentNodeId))
                .mapToLong(entry -> entry.getValue().get())
                .sum();

        // Determine max allowed for current node
        return Math.max(0, maxTotalValue - otherNodesTotal);
    }

    public boolean isLessThanOrEqualTo(BGCounter<K> other) {
        Objects.requireNonNull(other, "Cannot compare with null counter");

        for (K nodeId : payload.keySet()) {
            long thisValue = payload.get(nodeId).get();
            long otherValue = other.payload.getOrDefault(nodeId, new AtomicLong(0)).get();

            if (thisValue > otherValue) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "BGCounter{" +
                ", maxValue=" + maxValue +
                ", nodes=" + payload +
                ", totalCount=" + query() +
                '}';
    }

    public BGCounter<K> clone() {
        BGCounter<K> clonedCounter = new BGCounter<>(this.maxValue);

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

    public ConcurrentHashMap<K, AtomicLong> getPayload() {
        return payload;
    }

    public static void main(String[] args) {
        // Demonstrate bounded counter functionality
        BGCounter<String> counter1 = new BGCounter<>(10); // Max total of 10
        BGCounter<String> counter2 = new BGCounter<>(10);

        // Add nodes
        counter1.addNode("user_web");
        counter1.addNode("user_mobile");
        counter2.addNode("user_web");
        counter2.addNode("user_desktop");

        // Increment counters
        System.out.println("Incrementing counter1...");
        for (int i = 0; i < 15; i++) {
            boolean incrementedWeb = counter1.increment("user_web");
            boolean incrementedMobile = counter1.increment("user_mobile");
            System.out.println("Web increment " + i + ": " + incrementedWeb +
                    ", Mobile increment " + i + ": " + incrementedMobile + ", Total: " + counter1.query());
        }

        // Merge counters
        System.out.println("\nBefore merge:");
        System.out.println("Counter 1: " + counter1);

        counter2.increment("user_web");
        counter2.increment("user_desktop");
        counter2.increment("user_desktop");
        System.out.println("Counter 2: " + counter2);

        BGCounter<String> mergedCounter = counter1.merge(counter2);

        System.out.println("\nAfter merge:");
        System.out.println("Merged Counter: " + mergedCounter);
        System.out.println("Total Count: " + mergedCounter.query());
        System.out.println("Max Value: " + mergedCounter.getMaxValue());
    }
}