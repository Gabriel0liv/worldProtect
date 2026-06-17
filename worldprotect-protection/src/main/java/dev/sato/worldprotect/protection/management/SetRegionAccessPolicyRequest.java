package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class SetRegionAccessPolicyRequest {
    private final RegionId regionId;
    private final RegionAccessPolicyConfig accessPolicy;

    private SetRegionAccessPolicyRequest(RegionId regionId, RegionAccessPolicyConfig accessPolicy) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "accessPolicy must not be null");
    }

    public static SetRegionAccessPolicyRequest of(RegionId regionId, RegionAccessPolicyConfig accessPolicy) {
        return new SetRegionAccessPolicyRequest(regionId, accessPolicy);
    }

    public RegionId regionId() { return regionId; }
    public RegionAccessPolicyConfig accessPolicy() { return accessPolicy; }
    public RegionId getRegionId() { return regionId; }
    public RegionAccessPolicyConfig getAccessPolicy() { return accessPolicy; }
}
