package dev.sato.worldprotect.compat;

import dev.sato.worldprotect.minecraft.snapshot.ItemStackSnapshot;
import java.util.List;

/**
 * Snapshot representation of a container's items at a point in time.
 */
public interface InventorySnapshot {
    
    /**
     * Gets all items captured in this snapshot.
     */
    List<ItemStackSnapshot> getItems();

    /**
     * Checks if this snapshot contains no items.
     */
    boolean isEmpty();
}
