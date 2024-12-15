package org.project.data_structures.test;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class VClock {

    public enum Ord {
        LT,  // lower
        EQ,  // equal
        GT,  // greater
        CC   // concurrent
    }

    private final GCounter counter;

    public VClock() {
        this.counter = new GCounter();
    }

    public VClock(GCounter counter) {
        this.counter = counter;
    }

    public void increment(String nodeId) {
        counter.increment(nodeId);
    }

    public VClock merge(VClock other) {
        return new VClock(this.counter.merge(other.counter));
    }

    public Ord compare(VClock other) {
        Map<String, Long> thisPayload = toMap(this.counter);
        Map<String, Long> otherPayload = toMap(other.counter);

        Set<String> allKeys = new java.util.HashSet<>(thisPayload.keySet());
        allKeys.addAll(otherPayload.keySet());

        Ord result = Ord.EQ;
        for (String key : allKeys) {
            long thisValue = thisPayload.getOrDefault(key, 0L);
            long otherValue = otherPayload.getOrDefault(key, 0L);

            if (thisValue > otherValue) {
                if (result == Ord.LT) return Ord.CC;
                result = Ord.GT;
            } else if (thisValue < otherValue) {
                if (result == Ord.GT) return Ord.CC;
                result = Ord.LT;
            }
        }

        return result;
    }


    private Map<String, Long> toMap(GCounter gCounter) {
        ConcurrentHashMap<String, AtomicLong> payload = gCounter.getPayload();
        Map<String, Long> map = new java.util.HashMap<>();
        payload.forEach((key, value) -> map.put(key, value.get()));
        return map;
    }

    @Override
    public String toString() {
        return "VClock{" + "counter=" + counter + '}';
    }
}