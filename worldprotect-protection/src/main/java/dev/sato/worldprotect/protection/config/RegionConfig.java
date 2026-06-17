package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable configuration representation of a configured region.
 */
public final class RegionConfig {
    private final RegionId id;
    private final DimensionRef dimension;
    private final int priority;
    private final BoundsConfig bounds;
    private final Map<FlagKey, FlagRuleConfig> flags;
    private final RegionSubjectsConfig subjectsConfig;
    private final RegionAccessPolicyConfig accessPolicyConfig;
    private final Optional<RegionId> parentId;

    private RegionConfig(
            RegionId id,
            DimensionRef dimension,
            int priority,
            BoundsConfig bounds,
            Map<FlagKey, FlagRuleConfig> flags,
            RegionSubjectsConfig subjectsConfig,
            RegionAccessPolicyConfig accessPolicyConfig,
            Optional<RegionId> parentId
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.dimension = Objects.requireNonNull(dimension, "dimension must not be null");
        this.bounds = Objects.requireNonNull(bounds, "bounds must not be null");
        Objects.requireNonNull(flags, "flags must not be null");
        
        flags.forEach((key, val) -> {
            Objects.requireNonNull(key, "flag key must not be null");
            Objects.requireNonNull(val, "flag rule config must not be null");
        });
        
        this.flags = Map.copyOf(flags);
        this.priority = priority;
        this.subjectsConfig = Objects.requireNonNull(subjectsConfig, "subjectsConfig must not be null");
        this.accessPolicyConfig = Objects.requireNonNull(accessPolicyConfig, "accessPolicyConfig must not be null");
        this.parentId = Objects.requireNonNull(parentId, "parentId must not be null");
    }

    public static RegionConfig of(RegionId id, DimensionRef dimension, int priority, BoundsConfig bounds, Map<FlagKey, FlagRuleConfig> flags) {
        return new RegionConfig(id, dimension, priority, bounds, flags, RegionSubjectsConfig.empty(), RegionAccessPolicyConfig.defaults(), Optional.empty());
    }

    public static RegionConfig of(
            RegionId id,
            DimensionRef dimension,
            int priority,
            BoundsConfig bounds,
            Map<FlagKey, FlagRuleConfig> flags,
            RegionSubjectsConfig subjectsConfig,
            RegionAccessPolicyConfig accessPolicyConfig
    ) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, Optional.empty());
    }

    public static RegionConfig of(
            RegionId id,
            DimensionRef dimension,
            int priority,
            BoundsConfig bounds,
            Map<FlagKey, FlagRuleConfig> flags,
            RegionSubjectsConfig subjectsConfig,
            RegionAccessPolicyConfig accessPolicyConfig,
            Optional<RegionId> parentId
    ) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    public RegionId id() {
        return id;
    }

    public DimensionRef dimension() {
        return dimension;
    }

    public int priority() {
        return priority;
    }

    public BoundsConfig bounds() {
        return bounds;
    }

    public Map<FlagKey, FlagRuleConfig> flags() {
        return flags;
    }

    public RegionSubjectsConfig subjectsConfig() {
        return subjectsConfig;
    }

    public RegionAccessPolicyConfig accessPolicyConfig() {
        return accessPolicyConfig;
    }

    public Optional<RegionId> parentId() {
        return parentId;
    }

    public Optional<RegionId> getParentId() {
        return parentId;
    }

    public RegionId getId() {
        return id;
    }

    public DimensionRef getDimension() {
        return dimension;
    }

    public int getPriority() {
        return priority;
    }

    public BoundsConfig getBounds() {
        return bounds;
    }

    public Map<FlagKey, FlagRuleConfig> getFlags() {
        return flags;
    }

    public RegionSubjectsConfig getSubjectsConfig() {
        return subjectsConfig;
    }

    public RegionAccessPolicyConfig getAccessPolicyConfig() {
        return accessPolicyConfig;
    }

    public ConfigValidationResult validate(FlagRegistry flagRegistry) {
        Objects.requireNonNull(flagRegistry, "flagRegistry must not be null");
        ConfigValidationResult result = ConfigValidationResult.ok();

        // Validate bounds using the required path format: regions.<id>.bounds
        result = result.merge(bounds.validate("regions." + id.getValue() + ".bounds"));

        // Warning when region has no flags
        if (flags.isEmpty()) {
            result = result.add(ConfigValidationMessage.warning("regions." + id.getValue(), "Region has no flags configured"));
        }

        // Validate all flags to collect all issues
        for (Map.Entry<FlagKey, FlagRuleConfig> entry : flags.entrySet()) {
            FlagKey key = entry.getKey();
            FlagRuleConfig ruleConfig = entry.getValue();
            String path = "regions." + id.getValue() + ".flags." + key.getValue();

            if (!flagRegistry.exists(key)) {
                result = result.add(ConfigValidationMessage.error(path, "Unknown flag key: " + key.getValue()));
            } else {
                result = result.merge(ruleConfig.validate(path));
            }
        }

        // Validate subjects
        result = result.merge(subjectsConfig.validate("regions." + id.getValue() + ".subjects"));

        // Validate access policy
        result = result.merge(accessPolicyConfig.validate("regions." + id.getValue() + ".access", flagRegistry));

        // Validate parentId (self parent check)
        if (parentId.isPresent() && parentId.get().equals(id)) {
            result = result.add(ConfigValidationMessage.error("regions." + id.getValue() + ".parent", "Region parent must not be itself"));
        }

        return result;
    }

    public RegionConfig withBounds(BoundsConfig bounds) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    public RegionConfig withPriority(int priority) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    public RegionConfig withParentId(Optional<RegionId> parentId) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    public RegionConfig withFlags(Map<FlagKey, FlagRuleConfig> flags) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    public RegionConfig withSubjectsConfig(RegionSubjectsConfig subjectsConfig) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    public RegionConfig withAccessPolicyConfig(RegionAccessPolicyConfig accessPolicyConfig) {
        return new RegionConfig(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegionConfig that = (RegionConfig) o;
        return priority == that.priority &&
               id.equals(that.id) &&
               dimension.equals(that.dimension) &&
               bounds.equals(that.bounds) &&
               flags.equals(that.flags) &&
               subjectsConfig.equals(that.subjectsConfig) &&
               accessPolicyConfig.equals(that.accessPolicyConfig) &&
               parentId.equals(that.parentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dimension, priority, bounds, flags, subjectsConfig, accessPolicyConfig, parentId);
    }

    @Override
    public String toString() {
        return "RegionConfig{id=" + id + ", dimension=" + dimension + ", priority=" + priority +
               ", bounds=" + bounds + ", flags=" + flags + ", subjectsConfig=" + subjectsConfig +
               ", accessPolicyConfig=" + accessPolicyConfig + ", parentId=" + parentId + "}";
    }
}
