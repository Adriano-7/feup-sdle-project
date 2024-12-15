package org.project.data_structures.test;

import org.project.model.Item;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
public class AWORSet {
    public final Map<String, VClockItemPair> addSet; // listId -> VClockItemPair
    public final Map<String, VClockItemPair> removeSet;

    public AWORSet() {
        this.addSet = new ConcurrentHashMap<>();
        this.removeSet = new ConcurrentHashMap<>();
    }

    public Map<String, VClockItemPair> getValue() {
        Map<String, VClockItemPair> result = new ConcurrentHashMap<>(addSet);

        removeSet.forEach((key, remPair) -> {
            if (result.containsKey(key)) {
                VClock addClock = result.get(key).getVClock();
                if (addClock.compare(remPair.getVClock()) == VClock.Ord.LT) {
                    result.remove(key);
                }
            }
        });

        return result;
    }

    /*
        // Item exists in the shopping list?
        // Yes -> Increase the buying quantity & update time
        // No -> Add the item to the shopping list and update time
    */
    public void add(String nodeId, String itemName, int quantity) {
        if(addSet.containsKey(itemName)) {
            VClockItemPair newPair = addSet.get(itemName);
            newPair.getVClock().increment(nodeId);
            newPair.getItem().increaseMax(quantity);
            addSet.put(itemName, newPair);
        } else {
            VClockItemPair newPair = new VClockItemPair(new VClock(), new Item(itemName, quantity));
            newPair.getVClock().increment(nodeId);
            addSet.put(itemName, newPair);
        }
    }

    public void remove(String nodeId, String element) {
        VClockItemPair newPair = addSet.getOrDefault(element, new VClockItemPair(new VClock(), null));
        newPair.getVClock().increment(nodeId);
        removeSet.put(element, newPair);
        addSet.remove(element);
    }

    public AWORSet merge(AWORSet other) {
        other.addSet.forEach((key, otherPair) -> {
            addSet.merge(key, otherPair, (existingPair, newPair) -> {
                existingPair.setVClock(existingPair.getVClock().merge(newPair.getVClock()));
                existingPair.setItem(existingPair.getItem().merge(newPair.getItem()));
                return existingPair;
            });
        });

        other.removeSet.forEach((key, otherPair) -> {
            removeSet.merge(key, otherPair, (existingPair, newPair) -> {
                existingPair.setVClock(existingPair.getVClock().merge(newPair.getVClock()));
                return existingPair;
            });
        });

        removeSet.forEach((key, remPair) -> {
            if (addSet.containsKey(key)) {
                VClock addClock = addSet.get(key).getVClock();
                if (addClock.compare(remPair.getVClock()) == VClock.Ord.LT) {
                    addSet.remove(key);
                }
            }
        });

        addSet.forEach((key, addPair) -> {
            if (removeSet.containsKey(key)) {
                VClock remClock = removeSet.get(key).getVClock();
                if (addPair.getVClock().compare(remClock) == VClock.Ord.LT) {
                    removeSet.remove(key);
                }
            }
        });

        return this;
    }

    public boolean lookup(String element) {
        return addSet.containsKey(element) && !removeSet.containsKey(element);
    }

    public boolean isEmpty() {
        Map<String, VClockItemPair> value = getValue();
        return value.isEmpty();
    }

    public long consumeItem(String element, String user, long quantity) {
        VClockItemPair newPair = addSet.getOrDefault(element, new VClockItemPair(new VClock(), null));
        newPair.getVClock().increment(user);
        addSet.put(element, newPair);
        return addSet.get(element).getItem().consume(user, quantity);
    }

    @Override
    public java.lang.String toString() {
        StringBuilder s = new StringBuilder();
        Map<String, VClockItemPair> value = getValue();
        value.forEach((key, pair) -> {
            s.append(pair.getItem().toString()).append("\n");
        });
        return s.toString();
    }
}
