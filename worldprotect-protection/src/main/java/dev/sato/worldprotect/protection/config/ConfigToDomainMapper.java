package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.region.GlobalRegion;
import dev.sato.worldprotect.protection.region.Region;
import dev.sato.worldprotect.protection.region.RegionFlags;
import dev.sato.worldprotect.protection.region.RegionSet;
import dev.sato.worldprotect.protection.rule.FlagRule;
import dev.sato.worldprotect.protection.rule.ResourceSelector;
import dev.sato.worldprotect.protection.rule.ResourceSelectorSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service mapping valid in-memory configuration representations into active domain protection objects.
 */
public final class ConfigToDomainMapper {

    public RegionSet toRegionSet(WorldProtectConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        List<Region> domainRegions = new ArrayList<>();
        for (RegionConfig rc : config.regions()) {
            domainRegions.add(toRegion(rc));
        }
        return RegionSet.of(domainRegions);
    }

    public Region toRegion(RegionConfig regionConfig) {
        Objects.requireNonNull(regionConfig, "regionConfig must not be null");
        BoundsConfig bounds = regionConfig.bounds();
        RegionFlags domainFlags = toRegionFlags(regionConfig.flags());

        if (bounds.type() == BoundsType.CUBOID) {
            return new CuboidRegion(
                    regionConfig.id(),
                    regionConfig.dimension(),
                    bounds.min(),
                    bounds.max(),
                    regionConfig.priority(),
                    domainFlags,
                    regionConfig.subjectsConfig().toDomain(),
                    regionConfig.accessPolicyConfig().toDomain(),
                    regionConfig.parentId()
            );
        } else if (bounds.type() == BoundsType.GLOBAL) {
            return new GlobalRegion(
                    regionConfig.id(),
                    regionConfig.dimension(),
                    regionConfig.priority(),
                    domainFlags,
                    regionConfig.subjectsConfig().toDomain(),
                    regionConfig.accessPolicyConfig().toDomain(),
                    regionConfig.parentId()
            );
        } else {
            throw new IllegalArgumentException("Unsupported bounds type: " + bounds.type());
        }
    }

    public RegionFlags toRegionFlags(Map<FlagKey, FlagRuleConfig> flags) {
        Objects.requireNonNull(flags, "flags must not be null");
        Map<FlagKey, FlagRule> domainRules = new HashMap<>();
        for (Map.Entry<FlagKey, FlagRuleConfig> entry : flags.entrySet()) {
            domainRules.put(entry.getKey(), toFlagRule(entry.getValue()));
        }
        return RegionFlags.ofRules(domainRules);
    }

    public FlagRule toFlagRule(FlagRuleConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        if (config.isSimple()) {
            return FlagRule.simple(config.defaultState());
        }

        List<ResourceSelector> allow = config.allowSelectors().stream()
                .map(ResourceSelector::parse)
                .collect(Collectors.toList());

        List<ResourceSelector> deny = config.denySelectors().stream()
                .map(ResourceSelector::parse)
                .collect(Collectors.toList());

        return FlagRule.conditional(
                config.defaultState(),
                ResourceSelectorSet.of(allow),
                ResourceSelectorSet.of(deny)
        );
    }
}
