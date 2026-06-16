package dev.sato.worldprotect.audit;

import dev.sato.worldprotect.minecraft.snapshot.ItemStackSnapshot;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Record of item additions and removals from a container.
 */
public final class InventoryTransaction {
    private final List<ItemStackSnapshot> additions;
    private final List<ItemStackSnapshot> removals;

    public InventoryTransaction(List<ItemStackSnapshot> additions, List<ItemStackSnapshot> removals) {
        this.additions = Collections.unmodifiableList(Objects.requireNonNull(additions, "additions must not be null"));
        this.removals = Collections.unmodifiableList(Objects.requireNonNull(removals, "removals must not be null"));
    }

    public List<ItemStackSnapshot> getAdditions() {
        return additions;
    }

    public List<ItemStackSnapshot> getRemovals() {
        return removals;
    }

    public boolean isEmpty() {
        return additions.isEmpty() && removals.isEmpty();
    }

    @Override
    public String toString() {
        return "InventoryTransaction{additions=" + additions.size() + ", removals=" + removals.size() + "}";
    }
}
