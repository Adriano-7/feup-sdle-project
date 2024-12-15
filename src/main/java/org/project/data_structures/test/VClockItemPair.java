package org.project.data_structures.test;

import org.project.model.Item;

public class VClockItemPair {
    private VClock vClock;
    private Item item;

    public VClockItemPair(VClock vClock, Item item) {
        this.vClock = vClock;
        this.item = item;
    }

    public VClock getVClock() {
        return vClock;
    }

    public Item getItem() {
        return item;
    }

    public void setVClock(VClock vClock) {
        this.vClock = vClock;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return "VClockItemPair{" +
                "vClock=" + vClock +
                ", item=" + item +
                '}';
    }
}