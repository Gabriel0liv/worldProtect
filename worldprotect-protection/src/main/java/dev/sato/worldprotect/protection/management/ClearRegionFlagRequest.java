package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class ClearRegionFlagRequest {
    private final RegionId regionId;
    private final FlagKey flagKey;

    private ClearRegionFlagRequest(RegionId regionId, FlagKey flagKey) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.flagKey = Objects.requireNonNull(flagKey, "flagKey must not be null");
    }

    public static ClearRegionFlagRequest of(RegionId regionId, FlagKey flagKey) {
        return new ClearRegionFlagRequest(regionId, flagKey);
    }

    public RegionId regionId() { return regionId; }
    public FlagKey flagKey() { return flagKey; }
    public RegionId getRegionId() { return regionId; }
    public FlagKey getFlagKey() { return flagKey; }
}
