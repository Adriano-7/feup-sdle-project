package org.project.data_structures;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ORMap<K extends Comparable<K>, V, T> {
    private final String id;
    private final Map<K, Element<V, T>> addMap;
    private final Map<K, Element<V, T>> removeMap;

    public ORMap(String id) {
        this.id = id;
        this.addMap = new HashMap<>();
        this.removeMap = new HashMap<>();
    }
    public void put(K key, V value, T tag) {
        Element<V, T> existingElement = addMap.get(key);

        if (existingElement != null) {
            existingElement.addTag(tag);
        } else {
            Element<V, T> newElement = new Element<>(value);
            newElement.addTag(tag);
            addMap.put(key, newElement);
        }
    }

    public void remove(K key) {
        Element<V, T> addElement = addMap.get(key);
        if (addElement == null) {
            return;
        }

        Element<V, T> removeElement = removeMap.getOrDefault(key, new Element<>(addElement.getElement()));
        removeElement.addTags(addElement.getTags());
        removeMap.put(key, removeElement);
    }
    public boolean containsKey(K key) {
        if (!addMap.containsKey(key)) {
            return false;
        }

        Element<V, T> addElement = addMap.get(key);
        Element<V, T> removeElement = removeMap.get(key);

        Set<T> activeTags = new HashSet<>(addElement.getTags());
        if (removeElement != null) {
            activeTags.removeAll(removeElement.getTags());
        }

        return !activeTags.isEmpty();
    }
    public V get(K key) {
        if (!containsKey(key)) {
            return null;
        }
        return addMap.get(key).getElement();
    }
    public void merge(ORMap<K, V, T> other) {
        mergeMaps(this.addMap, other.addMap);
        mergeMaps(this.removeMap, other.removeMap);
    }
    public boolean equals(ORMap<K, V, T> other) {
        return compareMaps(this.addMap, other.addMap) &&
                compareMaps(this.removeMap, other.removeMap);
    }

    private void mergeMaps(Map<K, Element<V, T>> map1, Map<K, Element<V, T>> map2) {
        for (Map.Entry<K, Element<V, T>> entry : map2.entrySet()) {
            K key = entry.getKey();
            Element<V, T> otherElement = entry.getValue();

            map1.merge(key, new Element<>(otherElement.getElement()), (existing, toMerge) -> {
                existing.addTags(toMerge.getTags());
                return existing;
            });
        }
    }

    private boolean compareMaps(Map<K, Element<V, T>> map1, Map<K, Element<V, T>> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }

        for (Map.Entry<K, Element<V, T>> entry : map1.entrySet()) {
            K key = entry.getKey();
            if (!map2.containsKey(key) || !entry.getValue().equals(map2.get(key))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ORMap(id=").append(id).append(")\n");

        sb.append("Add Map:\n");
        addMap.forEach((key, value) -> sb.append("  ").append(key).append(": ").append(value).append("\n"));

        sb.append("Remove Map:\n");
        removeMap.forEach((key, value) -> sb.append("  ").append(key).append(": ").append(value).append("\n"));

        return sb.toString();
    }
}
