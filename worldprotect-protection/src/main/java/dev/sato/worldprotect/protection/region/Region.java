package dev.sato.worldprotect.protection.region;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import java.util.Optional;

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

    /**
     * Gets the flags configured for this region.
     */
    RegionFlags flags();

    /**
     * Gets the subjects associated with this region (owners, members).
     */
    dev.sato.worldprotect.protection.subject.RegionSubjects subjects();

    /**
     * Gets the access policy defining how roles affect flag checking.
     */
    dev.sato.worldprotect.protection.subject.RegionAccessPolicy accessPolicy();

    /**
     * Gets the parent region ID, if configured.
     */
    default Optional<RegionId> parentId() {
        return Optional.empty();
    }

    /**
     * Gets the parent region ID, if configured.
     */
    default Optional<RegionId> getParentId() {
        return parentId();
    }
}
