package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class SetRegionParentRequest {
    private final RegionId regionId;
    private final RegionId parentId;

    private SetRegionParentRequest(RegionId regionId, RegionId parentId) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.parentId = Objects.requireNonNull(parentId, "parentId must not be null");
    }

    public static SetRegionParentRequest of(RegionId regionId, RegionId parentId) {
        return new SetRegionParentRequest(regionId, parentId);
    }

    public RegionId regionId() { return regionId; }
    public RegionId parentId() { return parentId; }
    public RegionId getRegionId() { return regionId; }
    public RegionId getParentId() { return parentId; }
}
