package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;
import java.util.Optional;

public final class CreateRegionRequest {
    private final RegionId regionId;
    private final DimensionRef dimension;
    private final int priority;
    private final BoundsConfig bounds;
    private final Optional<RegionId> parentId;

    private CreateRegionRequest(RegionId regionId, DimensionRef dimension, int priority, BoundsConfig bounds, Optional<RegionId> parentId) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.priority = priority;
        this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
        this.parentId = Objects.requireNonNull(parentId, "parentId must not be null");
    }

    public static CreateRegionRequest of(RegionId regionId, DimensionRef dimension, int priority, BoundsConfig bounds) {
        return new CreateRegionRequest(regionId, dimension, priority, bounds, Optional.empty());
    }

    public static CreateRegionRequest of(RegionId regionId, DimensionRef dimension, int priority, BoundsConfig bounds, Optional<RegionId> parentId) {
        return new CreateRegionRequest(regionId, dimension, priority, bounds, parentId);
    }

    public RegionId regionId() { return regionId; }
    public DimensionRef dimension() { return dimension; }
    public int priority() { return priority; }
    public BoundsConfig bounds() { return bounds; }
    public Optional<RegionId> parentId() { return parentId; }
    public RegionId getRegionId() { return regionId; }
    public DimensionRef getDimension() { return dimension; }
    public int getPriority() { return priority; }
    public BoundsConfig getBounds() { return bounds; }
    public Optional<RegionId> getParentId() { return parentId; }
}
