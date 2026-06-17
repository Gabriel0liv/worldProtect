package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class SetRegionBoundsRequest {
    private final RegionId regionId;
    private final BoundsConfig bounds;

    private SetRegionBoundsRequest(RegionId regionId, BoundsConfig bounds) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
    }

    public static SetRegionBoundsRequest of(RegionId regionId, BoundsConfig bounds) {
        return new SetRegionBoundsRequest(regionId, bounds);
    }

    public RegionId regionId() { return regionId; }
    public BoundsConfig bounds() { return bounds; }
    public RegionId getRegionId() { return regionId; }
    public BoundsConfig getBounds() { return bounds; }
}
