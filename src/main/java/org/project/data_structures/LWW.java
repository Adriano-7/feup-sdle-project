package org.project.data_structures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.project.model.Item;

/**
 * LWW (Last-Write-Wins) Element Set implementation for Conflict-free Replicated Data Types (CRDTs)
 */
public class LWW {
    private Map<Item, Double> addSet;
    private Map<Item, Double> removeSet;

    private final ReadWriteLock addLock;
    private final ReadWriteLock removeLock;

    public LWW() {
        this.addSet = new HashMap<>();
        this.removeSet = new HashMap<>();
        this.addLock = new ReentrantReadWriteLock();
        this.removeLock = new ReentrantReadWriteLock();
    }

    public void add(Item element) {
        addLock.writeLock().lock();
        try {
            double currentTime = System.currentTimeMillis() / 1000.0;
            Double existingTimestamp = addSet.getOrDefault(element, 0.0);

            if (existingTimestamp < currentTime) {
                addSet.put(element, currentTime);
            }
        } catch (Exception e) {
            System.out.println("Error in add");
        } finally {
            addLock.writeLock().unlock();
        }
    }

    public boolean lookup(Item element) {
        addLock.readLock().lock();
        removeLock.readLock().lock();
        try {
            if (!addSet.containsKey(element)) {
                return false;
            }
            if (!removeSet.containsKey(element)) {
                return true;
            }
            return removeSet.get(element) < addSet.get(element);
        } finally {
            removeLock.readLock().unlock();
            addLock.readLock().unlock();
        }
    }

    public void remove(Item element) {
        removeLock.writeLock().lock();
        try {
            double currentTime = System.currentTimeMillis() / 1000.0;
            Double existingTimestamp = removeSet.getOrDefault(element, 0.0);

            if (existingTimestamp < currentTime) {
                removeSet.put(element, currentTime);
            }
        } catch (Exception e) {
            System.out.println("Error in remove");
        } finally {
            removeLock.writeLock().unlock();
        }
    }

    public boolean compare(LWW other) {
        addLock.readLock().lock();
        removeLock.readLock().lock();
        other.addLock.readLock().lock();
        other.removeLock.readLock().lock();
        try {
            boolean addSubset = other.addSet.keySet().containsAll(this.addSet.keySet());
            boolean removeSubset = other.removeSet.keySet().containsAll(this.removeSet.keySet());
            return addSubset && removeSubset;
        } finally {
            other.removeLock.readLock().unlock();
            other.addLock.readLock().unlock();
            removeLock.readLock().unlock();
            addLock.readLock().unlock();
        }
    }

    public LWW merge(LWW other) {
        LWW mergedLWW = new LWW();

        addLock.readLock().lock();
        removeLock.readLock().lock();
        other.addLock.readLock().lock();
        other.removeLock.readLock().lock();
        try {
            mergedLWW.addSet.putAll(this.addSet);
            mergedLWW.addSet.putAll(other.addSet);

            mergedLWW.removeSet.putAll(this.removeSet);
            mergedLWW.removeSet.putAll(other.removeSet);

            for (Map.Entry<Item, Double> entry : this.addSet.entrySet()) {
                Item element = entry.getKey();
                double timestamp = entry.getValue();
                mergedLWW.addSet.put(element,
                        Math.max(mergedLWW.addSet.getOrDefault(element, 0.0), timestamp)
                );
            }

            // Update timestamps for remove set
            for (Map.Entry<Item, Double> entry : this.removeSet.entrySet()) {
                Item element = entry.getKey();
                double timestamp = entry.getValue();
                mergedLWW.removeSet.put(element,
                        Math.max(mergedLWW.removeSet.getOrDefault(element, 0.0), timestamp)
                );
            }

            return mergedLWW;
        } finally {
            other.removeLock.readLock().unlock();
            other.addLock.readLock().unlock();
            removeLock.readLock().unlock();
            addLock.readLock().unlock();
        }
    }


    public Map<Item, Double> getAddSet() {
        addLock.readLock().lock();
        try {
            return new HashMap<>(addSet);
        } finally {
            addLock.readLock().unlock();
        }
    }

    public Map<Item, Double> getRemoveSet() {
        removeLock.readLock().lock();
        try {
            return new HashMap<>(removeSet);
        } finally {
            removeLock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        for (Item item : addSet.keySet()) {
            if (lookup(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Item item : addSet.keySet()) {
            if (lookup(item)) {
                sb.append(item).append("\n");
            }
        }
        return sb.toString();
    }

    public Item get(String ID) {
        for (Item item : addSet.keySet()) {
            if (item.getID().equals(ID)) {
                return item;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        LWW lww = new LWW();
        Item item1 = new Item("apple", 10);
        Item item2 = new Item("banana", 5);
        Item item3 = new Item("cherry", 20);
        lww.add(item1);
        lww.add(item2);
        lww.add(item3);
        lww.remove(item2);

        System.out.println(lww);
    }
}