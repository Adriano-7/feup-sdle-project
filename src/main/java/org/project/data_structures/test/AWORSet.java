package org.project.data_structures.test;

import org.project.model.Item;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

import java.util.HashMap;

public class AWORSet {
    private final Map<String, VClock> addSet;//listId -> VClock
    private final Map<String, VClock> removeSet;//listId -> VClock

    public AWORSet() {
        this.addSet = new ConcurrentHashMap<>();
        this.removeSet = new ConcurrentHashMap<>();
    }

    public Map<String, VClock> getValue() {
        Map<String, VClock> result = new HashMap<>(addSet);

        removeSet.forEach((key, remClock) -> {
            if (result.containsKey(key)) {
                VClock addClock = result.get(key);
                if (addClock.compare(remClock) == VClock.Ord.LT) {
                    result.remove(key);
                }
            }
        });

        return result;
    }

    public void add(String nodeId, String element) {
        VClock newClock = addSet.getOrDefault(element, new VClock());
        newClock.increment(nodeId);
        addSet.put(element, newClock);
        removeSet.remove(element);
    }

    public void remove(java.lang.String nodeId, String element) {
        VClock newClock = addSet.getOrDefault(element, new VClock());
        newClock.increment(nodeId);
        removeSet.put(element, newClock);
        addSet.remove(element);
    }

    public void merge(AWORSet other) {
        other.addSet.forEach((key, otherClock) -> {
            addSet.merge(key, otherClock, VClock::merge);
        });

        other.removeSet.forEach((key, otherClock) -> {
            removeSet.merge(key, otherClock, VClock::merge);
        });

        // Cleanup invalid entries
        removeSet.forEach((key, remClock) -> {
            if (addSet.containsKey(key)) {
                VClock addClock = addSet.get(key);
                if (addClock.compare(remClock) == VClock.Ord.LT) {
                    addSet.remove(key);
                }
            }
        });

        addSet.forEach((key, addClock) -> {
            if (removeSet.containsKey(key)) {
                VClock remClock = removeSet.get(key);
                if (addClock.compare(remClock) == VClock.Ord.LT) {
                    removeSet.remove(key);
                }
            }
        });
    }

    @Override
    public java.lang.String toString() {
        return "AWORSet{" +
                "addSet=" + addSet +
                ", removeSet=" + removeSet +
                '}';
    }

    public static void main(java.lang.String[] args) {

        System.out.println("Merged AWORSet:");
    }
}
