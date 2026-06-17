package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;

public final class SetRegionFlagRequest {
    private final RegionId regionId;
    private final FlagKey flagKey;
    private final FlagRuleConfig rule;

    private SetRegionFlagRequest(RegionId regionId, FlagKey flagKey, FlagRuleConfig rule) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.flagKey = Objects.requireNonNull(flagKey, "flagKey must not be null");
        this.rule = Objects.requireNonNull(rule, "rule must not be null");
    }

    public static SetRegionFlagRequest of(RegionId regionId, FlagKey flagKey, FlagRuleConfig rule) {
        return new SetRegionFlagRequest(regionId, flagKey, rule);
    }

    public RegionId regionId() { return regionId; }
    public FlagKey flagKey() { return flagKey; }
    public FlagRuleConfig rule() { return rule; }
    public RegionId getRegionId() { return regionId; }
    public FlagKey getFlagKey() { return flagKey; }
    public FlagRuleConfig getRule() { return rule; }
}
