package org.project.data_structures;
import java.util.Objects;

public  class Pair<K, Integer> {
    private K key;
    private Integer value;

    public Pair(K key, Integer value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}