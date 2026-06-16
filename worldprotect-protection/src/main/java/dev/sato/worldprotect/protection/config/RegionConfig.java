package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.region.RegionId;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable configuration representation of a configured region.
 */
public final class RegionConfig {
    private final RegionId id;
    private final DimensionRef dimension;
    private final int priority;
    private final BoundsConfig bounds;
    private final Map<FlagKey, FlagRuleConfig> flags;

    private RegionConfig(RegionId id, DimensionRef dimension, int priority, BoundsConfig bounds, Map<FlagKey, FlagRuleConfig> flags) {
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
    }

    public static RegionConfig of(RegionId id, DimensionRef dimension, int priority, BoundsConfig bounds, Map<FlagKey, FlagRuleConfig> flags) {
        return new RegionConfig(id, dimension, priority, bounds, flags);
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

        return result;
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
               flags.equals(that.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dimension, priority, bounds, flags);
    }

    @Override
    public String toString() {
        return "RegionConfig{id=" + id + ", dimension=" + dimension + ", priority=" + priority + ", bounds=" + bounds + ", flags=" + flags + "}";
    }
}
