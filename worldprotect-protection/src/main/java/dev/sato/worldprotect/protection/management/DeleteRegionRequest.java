package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class DeleteRegionRequest {
    private final RegionId regionId;

    private DeleteRegionRequest(RegionId regionId) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
    }

    public static DeleteRegionRequest of(RegionId regionId) {
        return new DeleteRegionRequest(regionId);
    }

    public RegionId regionId() { return regionId; }
    public RegionId getRegionId() { return regionId; }
}
