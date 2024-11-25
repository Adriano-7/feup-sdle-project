package org.project.data_structures;
import java.util.concurrent.atomic.AtomicBoolean;

public class EWFlag {
    private final AtomicBoolean state; // Atomic boolean to handle concurrency safely
    private String id;

    public EWFlag(String id) {
        this.id = id;
        this.state = new AtomicBoolean(false);
    }

    public void enable() {
        state.set(true);
    }

    public void disable() {
        state.set(false);
    }

    public boolean isEnabled() {
        return state.get();
    }

    public void merge(EWFlag other) {
        if (other.isEnabled()) {
            this.enable();
        }
    }

    @Override
    public String toString() {
        return isEnabled() ? "Enabled" : "Disabled";
    }

    public static void main(String[] args) {
        EWFlag flag1 = new EWFlag("batatas");
        EWFlag flag2 = new EWFlag("couves");

        System.out.println("Flag 1: " + flag1);
        System.out.println("Flag 2: " + flag2);

        // Enable flag1
        flag1.enable();
        System.out.println("Flag 1 after enabling: " + flag1);

        // Merge flag2 into flag1 (flag2 is still disabled)
        flag1.merge(flag2);
        System.out.println("Flag 1 after merge with Flag 2: " + flag1);

        // Disable flag2 and merge again (flag1 remains enabled)
        flag2.disable();
        flag1.merge(flag2);
        System.out.println("Flag 1 after merge with Flag 2 (after disable): " + flag1);

        flag1.disable();
        flag2.disable();
        System.out.println("Flag 1 after merge with Flag 2 (after disable): " + flag1);
    }
}
