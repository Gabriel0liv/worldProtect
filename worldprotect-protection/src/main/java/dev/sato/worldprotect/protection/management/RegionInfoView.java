package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.BoundsType;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable read-only projection of a region configuration.
 */
public final class RegionInfoView {
    private final RegionId regionId;
    private final DimensionRef dimension;
    private final int priority;
    private final BoundsType boundsType;
    private final Optional<BlockPosRef> min;
    private final Optional<BlockPosRef> max;
    private final Optional<RegionId> parentId;
    private final Map<FlagKey, FlagRuleConfig> flags;
    private final List<SubjectRefConfig> owners;
    private final List<SubjectRefConfig> members;
    private final RegionAccessPolicyConfig accessPolicy;
    private final String accessPolicySummary;

    private RegionInfoView(
            RegionId regionId,
            DimensionRef dimension,
            int priority,
            BoundsType boundsType,
            Optional<BlockPosRef> min,
            Optional<BlockPosRef> max,
            Optional<RegionId> parentId,
            Map<FlagKey, FlagRuleConfig> flags,
            List<SubjectRefConfig> owners,
            List<SubjectRefConfig> members,
            RegionAccessPolicyConfig accessPolicy,
            String accessPolicySummary
    ) {
        this.regionId = Objects.requireNonNull(regionId, "regionId must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.priority = priority;
        this.boundsType = Objects.requireNonNull(boundsType, "boundsType must not be null");
        this.min = Objects.requireNonNull(min, "min must not be null");
        this.max = Objects.requireNonNull(max, "max must not be null");
        this.parentId = Objects.requireNonNull(parentId, "parentId must not be null");
        this.flags = Map.copyOf(Objects.requireNonNull(flags, "flags must not be null"));
        this.owners = List.copyOf(Objects.requireNonNull(owners, "owners must not be null"));
        this.members = List.copyOf(Objects.requireNonNull(members, "members must not be null"));
        this.accessPolicy = Objects.requireNonNull(accessPolicy, "accessPolicy must not be null");
        this.accessPolicySummary = Objects.requireNonNull(accessPolicySummary, "accessPolicySummary must not be null");
    }

    public static RegionInfoView of(
            RegionId regionId,
            DimensionRef dimension,
            int priority,
            BoundsConfig bounds,
            Optional<RegionId> parentId,
            Map<FlagKey, FlagRuleConfig> flags,
            List<SubjectRefConfig> owners,
            List<SubjectRefConfig> members,
            RegionAccessPolicyConfig accessPolicy,
            String accessPolicySummary
    ) {
        Objects.requireNonNull(bounds, "bounds must not be null");
        return new RegionInfoView(
                regionId,
                dimension,
                priority,
                bounds.type(),
                bounds.minOptional(),
                bounds.maxOptional(),
                parentId,
                flags,
                owners,
                members,
                accessPolicy,
                accessPolicySummary
        );
    }

    public RegionId regionId() {
        return regionId;
    }

    public DimensionRef dimension() {
        return dimension;
    }

    public int priority() {
        return priority;
    }

    public BoundsType boundsType() {
        return boundsType;
    }

    public Optional<BlockPosRef> min() {
        return min;
    }

    public Optional<BlockPosRef> max() {
        return max;
    }

    public Optional<RegionId> parentId() {
        return parentId;
    }

    public Map<FlagKey, FlagRuleConfig> flags() {
        return flags;
    }

    public List<SubjectRefConfig> owners() {
        return owners;
    }

    public List<SubjectRefConfig> members() {
        return members;
    }

    public RegionAccessPolicyConfig accessPolicy() {
        return accessPolicy;
    }

    public String accessPolicySummary() {
        return accessPolicySummary;
    }

    public int flagCount() {
        return flags.size();
    }

    public int ownerCount() {
        return owners.size();
    }

    public int memberCount() {
        return members.size();
    }

    public RegionId getRegionId() {
        return regionId;
    }

    public DimensionRef getDimension() {
        return dimension;
    }

    public int getPriority() {
        return priority;
    }

    public BoundsType getBoundsType() {
        return boundsType;
    }

    public Optional<BlockPosRef> getMin() {
        return min;
    }

    public Optional<BlockPosRef> getMax() {
        return max;
    }

    public Optional<RegionId> getParentId() {
        return parentId;
    }

    public Map<FlagKey, FlagRuleConfig> getFlags() {
        return flags;
    }

    public List<SubjectRefConfig> getOwners() {
        return owners;
    }

    public List<SubjectRefConfig> getMembers() {
        return members;
    }

    public RegionAccessPolicyConfig getAccessPolicy() {
        return accessPolicy;
    }

    public String getAccessPolicySummary() {
        return accessPolicySummary;
    }
}
