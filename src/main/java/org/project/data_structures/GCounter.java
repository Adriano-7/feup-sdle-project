package org.project.data_structures;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * GCounter (Grow-Only Counter) Conflict-free Replicated Data Type (CRDT)
 *
 * Implements a distributed, eventually consistent counter with the following properties:
 * - Monotonically increasing counter
 * - Supports concurrent updates across distributed systems
 * - Provides causal consistency guarantees
 *
 * Theoretical Foundations:
 * - Based on the semilattice merge operation
 * - Follows the principles of CRDTs as defined by Marc Shapiro et al.
 *
 * @param <K> Type of node identifier
 */
public class GCounter<K> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Payload representing the distributed counter state
     * Concurrent hash map ensures thread-safe operations
     */
    private final Map<K, AtomicLong> payload;

    /**
     * Unique identifier for this counter instance
     */
    private final UUID instanceId;

    /**
     * Constructs a new GCounter with a randomly generated instance ID
     */
    public GCounter() {
        this.payload = new ConcurrentHashMap<>();
        this.instanceId = UUID.randomUUID();
    }

    /**
     * Constructs a GCounter with a specific instance ID
     *
     * @param instanceId Unique identifier for this counter
     */
    public GCounter(UUID instanceId) {
        this.payload = new ConcurrentHashMap<>();
        this.instanceId = instanceId;
    }

    /**
     * Adds a new node to the counter
     *
     * @param nodeId Identifier for the new node
     * @throws IllegalArgumentException if node already exists
     */
    public void addNode(K nodeId) {
        Objects.requireNonNull(nodeId, "Node identifier cannot be null");

        if (payload.containsKey(nodeId)) {
            throw new IllegalArgumentException("Node " + nodeId + " already exists");
        }

        payload.put(nodeId, new AtomicLong(0));
    }

    /**
     * Increments the counter for a specific node
     *
     * @param nodeId Node to increment
     * @throws IllegalStateException if node doesn't exist
     */
    public void increment(K nodeId) {
        AtomicLong counter = payload.get(nodeId);

        if (counter == null) {
            throw new IllegalStateException("Node " + nodeId + " does not exist");
        }

        counter.incrementAndGet();
    }

    /**
     * Retrieves the total count across all nodes
     *
     * @return Total aggregated count
     */
    public long query() {
        return payload.values().stream()
                .mapToLong(AtomicLong::get)
                .sum();
    }

    /**
     * Merges this counter with another counter
     * Implements the semilattice merge operation
     *
     * @param other Counter to merge with
     * @return Merged counter
     */
    public GCounter<K> merge(GCounter<K> other) {
        Objects.requireNonNull(other, "Cannot merge with null counter");

        GCounter<K> mergedCounter = new GCounter<>(this.instanceId);

        // Combine nodes from both counters
        payload.keySet().forEach(mergedCounter::addNode);

        // for each key of other key set, if merged does not have that key, add that key to merged
        for(K key : other.payload.keySet()){
            if(!mergedCounter.payload.containsKey(key)){
                mergedCounter.addNode(key);
            }
        }

        // Perform element-wise max merge
        for (K nodeId : mergedCounter.payload.keySet()) {
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

    /**
     * Checks if this counter is less than or equal to another counter
     * Implements the partial order comparison
     *
     * @param other Counter to compare against
     * @return true if this counter is less than or equal to the other
     */
    public boolean isLessThanOrEqualTo(GCounter<K> other) {
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

    /**
     * Provides a detailed string representation of the counter
     *
     * @return Detailed counter state
     */
    @Override
    public String toString() {
        return "GCounter{" +
                "instanceId=" + instanceId +
                ", nodes=" + payload +
                ", totalCount=" + query() +
                '}';
    }

    /**
     * Generates a deep copy of the counter
     *
     * @return Exact replica of the current counter
     */
    public GCounter<K> clone() {
        GCounter<K> clonedCounter = new GCounter<>(this.instanceId);

        payload.forEach((nodeId, value) -> {
            clonedCounter.addNode(nodeId);
            clonedCounter.payload.get(nodeId).set(value.get());
        });

        return clonedCounter;
    }

    public static void main(String[] args) {
        // Create counters for different users/systems
        GCounter<String> counter1 = new GCounter<>();
        GCounter<String> counter2 = new GCounter<>();

        // Add nodes (users/sources)
        counter1.addNode("user_web");
        counter1.addNode("user_mobile");
        counter2.addNode("user_web");
        counter2.addNode("user_desktop");

        // Increment counters independently
        counter1.increment("user_web");
        counter1.increment("user_web");
        counter1.increment("user_mobile");

        counter2.increment("user_web");
        counter2.increment("user_desktop");
        counter2.increment("user_desktop");

        // Merge counters
        GCounter<String> mergedCounter = counter1.merge(counter2);

        // Print results
        System.out.println("Counter 1: " + counter1);
        System.out.println("Counter 2: " + counter2);
        System.out.println("Merged Counter: " + mergedCounter);
        System.out.println("Total Count: " + mergedCounter.query());

        // Demonstrate partial order
        System.out.println("Is Counter 1 <= Merged? " + counter1.isLessThanOrEqualTo(mergedCounter));
    }
}