package dev.sato.worldprotect.compat;

import dev.sato.worldprotect.minecraft.snapshot.ItemStackSnapshot;

/**
 * Interface representing common read/write access patterns on any inventory.
 */
public interface InventoryAccess {

    /**
     * Captures a point-in-time snapshot of this inventory.
     */
    InventorySnapshot captureSnapshot();

    /**
     * Checks if this inventory can accept the given item stack.
     */
    boolean canInsert(ItemStackSnapshot stack);

    /**
     * Checks if this inventory can have the given item stack extracted.
     */
    boolean canExtract(ItemStackSnapshot stack);
}
