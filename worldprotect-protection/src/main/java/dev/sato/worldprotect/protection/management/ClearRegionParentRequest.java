package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class ClearRegionParentRequest {
    private final RegionId regionId;

    private ClearRegionParentRequest(RegionId regionId) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
    }

    public static ClearRegionParentRequest of(RegionId regionId) {
        return new ClearRegionParentRequest(regionId);
    }

    public RegionId regionId() { return regionId; }
    public RegionId getRegionId() { return regionId; }
}
