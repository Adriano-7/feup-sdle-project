package org.project.data_structures;

import java.util.*;

public class DotContext<K> {
    private Map<K, Integer> cc;

    private Set<Map.Entry<K, Integer>> dc;

    public DotContext() {
        cc = new HashMap<>();
        dc = new HashSet<>();
    }

    public DotContext(DotContext<K> other) {
        this.cc = new HashMap<>(other.cc);
        this.dc = new HashSet<>(other.dc);
    }

    public DotContext<K> copy(DotContext<K> other) {
        if (this == other) return this;
        this.cc = new HashMap<>(other.cc);
        this.dc = new HashSet<>(other.dc);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Context: CC ( ");
        for (Map.Entry<K, Integer> entry : cc.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
        }
        sb.append(") DC ( ");
        for (Map.Entry<K, Integer> entry : dc) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

    public boolean dotIn(Map.Entry<K, Integer> d) {
        Integer ccValue = cc.get(d.getKey());
        if (ccValue != null && d.getValue() <= ccValue) return true;
        return dc.contains(d);
    }

    public void compact() {
        boolean flag;
        do {
            flag = false;
            Iterator<Map.Entry<K, Integer>> sit = dc.iterator();
            while (sit.hasNext()) {
                Map.Entry<K, Integer> dot = sit.next();
                Integer ccValue = cc.get(dot.getKey());

                if (ccValue == null) {
                    // No CC entry
                    if (dot.getValue() == 1) {
                        cc.put(dot.getKey(), dot.getValue());
                        sit.remove();
                        flag = true;
                    }
                } else {
                    // There is a CC entry
                    if (dot.getValue() == ccValue + 1) {
                        // Contiguous, can compact
                        cc.put(dot.getKey(), dot.getValue());
                        sit.remove();
                        flag = true;
                    } else if (dot.getValue() <= ccValue) {
                        // Dominated, so prune
                        sit.remove();
                    }
                }
            }
        } while (flag);
    }

    public Map.Entry<K, Integer> makeDot(K id) {
        cc.merge(id, 1, Integer::sum);
        return Map.entry(id, cc.get(id));
    }

    public void insertDot(Map.Entry<K, Integer> d, boolean compactNow) {
        dc.add(d);
        if (compactNow) compact();
    }

    public void join(DotContext<K> other) {
        if (this == other) return;

        for (Map.Entry<K, Integer> entry : other.cc.entrySet()) {
            cc.merge(entry.getKey(), entry.getValue(), Integer::max);
        }

        for (Map.Entry<K, Integer> entry : other.dc) {
            insertDot(entry, false);
        }

        compact();
    }

    public Map<K, Integer> getCompactContext() {
        return new HashMap<>(cc);
    }

    public Set<Map.Entry<K, Integer>> getDotCloud() {
        return new HashSet<>(dc);
    }
}