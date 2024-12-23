package org.project.data_structures.crdts;

import org.project.data_structures.model.Item;
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

        //Remove from add set all entries whose add clock is less than the remove clock (the item has been removed)
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

    public void add(String nodeId, String itemName, int quantity) {
        if(addSet.containsKey(itemName)){
            VClockItemPair newPair = addSet.get(itemName);
            newPair.getVClock().increment(nodeId);
            newPair.getItem().increaseMax(quantity);
            addSet.put(itemName, newPair);
        }
        else if (removeSet.containsKey(itemName)){
            VClockItemPair newPair = removeSet.get(itemName);
            newPair.getVClock().increment(nodeId);
            newPair.setItem(new Item(itemName, quantity));
            addSet.put(itemName, newPair);
            removeSet.remove(itemName);
        }
        else{
            VClockItemPair newPair = new VClockItemPair(new VClock(), new Item(itemName, quantity));
            newPair.getVClock().increment(nodeId);
            addSet.put(itemName, newPair);
        }
    }
    
    public void remove(String nodeId, String element) {
        if(addSet.containsKey(element)){
            VClockItemPair newPair = addSet.get(element);
            newPair.getVClock().increment(nodeId);
            removeSet.put(element, newPair);
            addSet.remove(element);
        }
        else if (removeSet.containsKey(element)){
            VClockItemPair newPair = removeSet.get(element);
            newPair.getVClock().increment(nodeId);
            removeSet.put(element, newPair);
        }
        else{
            VClockItemPair newPair = new VClockItemPair(new VClock(), null);
            newPair.getVClock().increment(nodeId);
            removeSet.put(element, newPair);
        }
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
