package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;

/**
 * Base interface for protected regions in worldProtect.
 */
public interface Region {
    
    /**
     * Gets the unique identifier for this region.
     */
    RegionId getId();

    /**
     * Gets the dimension this region is located in.
     */
    DimensionRef getDimension();

    /**
     * Gets the priority of the region (higher values take precedence).
     */
    int getPriority();

    /**
     * Checks if the given position is contained within the bounds of this region.
     */
    boolean contains(BlockPosRef pos);
}
