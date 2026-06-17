package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class RegionInfoRequest {
    private final RegionId regionId;

    private RegionInfoRequest(RegionId regionId) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
    }

    public static RegionInfoRequest of(RegionId regionId) {
        return new RegionInfoRequest(regionId);
    }

    public RegionId regionId() { return regionId; }
    public RegionId getRegionId() { return regionId; }
}
