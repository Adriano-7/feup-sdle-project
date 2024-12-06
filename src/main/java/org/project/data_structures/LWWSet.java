package org.project.data_structures;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.project.model.Item;

import com.google.gson.Gson;

/**
 * LWW (Last-Write-Wins) Element Set implementation for Conflict-free Replicated Data Types (CRDTs)
 */


/*
CURRENT:
{
    ID-291231312 = {
        "item" =  Item(osjdkla,akksadja,masoidioap)
        "add-time" = 1.3122319312
        "rmv-time" = 1.9182398291
        },
}
* */
public class LWWSet {
    public Map<String, Map<String,Object>> items;
    private final ReadWriteLock addLock;
    private final ReadWriteLock removeLock;

    public LWWSet() {
        this.items = new HashMap<>();
        this.addLock = new ReentrantReadWriteLock();
        this.removeLock = new ReentrantReadWriteLock();
    }

    public void add(String name, int quantity) {
        // Item exists in the shopping list?
        // Yes -> Increase the buying quantity & update the add-time
        // No -> Add the item to the shopping list and set the add-time
        if(items.containsKey(name)){
            Map<String,Object> itemInfo = items.get(name);
            Item item = (Item) itemInfo.get("item");
            item.increaseMax(quantity);
            itemInfo.put("item", item);
            addLock.writeLock().lock();
            try {
                double currentTime = System.currentTimeMillis() / 1000.0;
                Double existingTimestamp = (Double) itemInfo.getOrDefault("add-time", 0.0);

                if (existingTimestamp < currentTime) {
                    itemInfo.put("add-time", currentTime);
                }
            } catch (Exception e) {
                System.out.println("Error in add");
            } finally {
                addLock.writeLock().unlock();
            }
            items.put(name, itemInfo);
        }else{
            Item item = new Item(name, quantity);
            Map<String,Object> itemInfo = new HashMap<>();
            itemInfo.put("item", item);
            addLock.writeLock().lock();
            try {
                double currentTime = System.currentTimeMillis() / 1000.0;
                itemInfo.put("add-time", currentTime);
            } catch (Exception e) {
                System.out.println("Error in add");
            } finally {
                addLock.writeLock().unlock();
                itemInfo.put("rmv-time", 0.0);
            }
            items.put(name, itemInfo);
        }
    }

    public boolean lookup(String id) {
        addLock.readLock().lock();
        removeLock.readLock().lock();
        try {
            if (!items.containsKey(id)) {
                return false;
            }
            var itemInfo = items.get(id);
            return (Double) itemInfo.get("add-time") > (Double) itemInfo.get("rmv-time");
        } finally {
            removeLock.readLock().unlock();
            addLock.readLock().unlock();
        }
    }

    public void remove(String id) {
        removeLock.writeLock().lock();
        try {
            double currentTime = System.currentTimeMillis() / 1000.0;
            Double existingTimestamp = (Double) items.get(id).getOrDefault("rmv-time", 0.0);

            if (existingTimestamp < currentTime) {
                items.get(id).put("rmv-time", currentTime);
            }
        } catch (Exception e) {
            System.out.println("Error in remove");
        } finally {
            removeLock.writeLock().unlock();
        }
    }

    public void remove(Item item) {
        remove(item.getName());
    }

    public boolean compare(LWWSet other) {
        addLock.readLock().lock();
        removeLock.readLock().lock();
        other.addLock.readLock().lock();
        other.removeLock.readLock().lock();
        try {
            return this.items.keySet().containsAll(other.items.keySet());
        } finally {
            other.removeLock.readLock().unlock();
            other.addLock.readLock().unlock();
            removeLock.readLock().unlock();
            addLock.readLock().unlock();
        }
    }

    public static Map<String,Object> mergeItems(Map<String,Object> item1info, Map<String,Object> item2info){
        var result = new HashMap<String,Object>();
        Item item1 = (Item) item1info.get("item");
        Item item2 = (Item) item2info.get("item");
        Item mergedItem = item1.merge(item2);
        result.put("item", mergedItem);
        double addTime1 = (Double) item1info.get("add-time");
        double addTime2 = (Double) item2info.get("add-time");
        double rmvTime1 = (Double) item1info.get("rmv-time");
        double rmvTime2 = (Double) item2info.get("rmv-time");
        result.put("add-time", Math.max(addTime1, addTime2));
        result.put("rmv-time", Math.max(rmvTime1, rmvTime2));
        return result;
    }

    public LWWSet merge(LWWSet other) {
        LWWSet mergedLWW = new LWWSet();

        addLock.readLock().lock();
        removeLock.readLock().lock();
        other.addLock.readLock().lock();
        other.removeLock.readLock().lock();
        try {
            for (String itemID : this.items.keySet()) {
                if (other.items.containsKey(itemID)) {
                    mergedLWW.items.put(itemID, mergeItems(items.get(itemID), other.items.get(itemID)));
                } else {
                    mergedLWW.items.put(itemID, items.get(itemID));
                }
            }
            for (String itemID : other.items.keySet()) {
                if (!this.items.containsKey(itemID)) {
                    mergedLWW.items.put(itemID, other.items.get(itemID));
                }
            }
            return mergedLWW;
        } finally {
            other.removeLock.readLock().unlock();
            other.addLock.readLock().unlock();
            removeLock.readLock().unlock();
            addLock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String itemID : items.keySet()) {
            if(lookup(itemID)){
                sb.append(items.get(itemID).get("item").toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public Item get(String id) {
        return (Item) items.get(id).get("item");
    }

    public long consumeItem(String id, String user, int quantity) {
        return get(id).consume(user, quantity);
    }
}