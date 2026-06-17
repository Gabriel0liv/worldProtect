package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable preview/description of a region mutation.
 */
public final class RegionMutationPlan {
    private final RegionMutationType type;
    private final RegionId targetRegionId;
    private final String summary;
    private final Optional<RegionConfig> beforeRegion;
    private final Optional<RegionConfig> afterRegion;

    private RegionMutationPlan(
            RegionMutationType type,
            RegionId targetRegionId,
            String summary,
            Optional<RegionConfig> beforeRegion,
            Optional<RegionConfig> afterRegion
    ) {
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.targetRegionId = Objects.requireNonNull(targetRegionId, "targetRegionId must not be null");
        this.summary = Objects.requireNonNull(summary, "summary must not be null");
        this.beforeRegion = Objects.requireNonNull(beforeRegion, "beforeRegion must not be null");
        this.afterRegion = Objects.requireNonNull(afterRegion, "afterRegion must not be null");
    }

    public static RegionMutationPlan of(
            RegionMutationType type,
            RegionId targetRegionId,
            String summary,
            Optional<RegionConfig> beforeRegion,
            Optional<RegionConfig> afterRegion
    ) {
        return new RegionMutationPlan(type, targetRegionId, summary, beforeRegion, afterRegion);
    }

    public RegionMutationType type() {
        return type;
    }

    public RegionId targetRegionId() {
        return targetRegionId;
    }

    public String summary() {
        return summary;
    }

    public Optional<RegionConfig> beforeRegion() {
        return beforeRegion;
    }

    public Optional<RegionConfig> afterRegion() {
        return afterRegion;
    }

    public RegionMutationType getType() {
        return type;
    }

    public RegionId getTargetRegionId() {
        return targetRegionId;
    }

    public String getSummary() {
        return summary;
    }

    public Optional<RegionConfig> getBeforeRegion() {
        return beforeRegion;
    }

    public Optional<RegionConfig> getAfterRegion() {
        return afterRegion;
    }
}
