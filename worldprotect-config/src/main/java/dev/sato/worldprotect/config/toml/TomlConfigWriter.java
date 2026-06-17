package dev.sato.worldprotect.config.toml;

import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.RegionSubjectsConfig;
import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.subject.RegionGroup;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Canonical TOML writer for worldProtect configs.
 *
 * <p>This writer does not preserve original comments or formatting.</p>
 */
public final class TomlConfigWriter {

    public TomlConfigWriteResult write(WorldProtectConfig config) {
        Objects.requireNonNull(config, "config must not be null");

        StringBuilder builder = new StringBuilder();
        if (config.regions().isEmpty()) {
            builder.append("[regions]\n");
            return TomlConfigWriteResult.success(builder.toString(), config.validate(dev.sato.worldprotect.protection.flag.FlagRegistry.withBuiltIns()));
        }

        boolean firstRegion = true;
        for (RegionConfig region : config.regions()) {
            if (!firstRegion) {
                builder.append('\n');
            }
            firstRegion = false;
            writeRegion(builder, region);
        }

        return TomlConfigWriteResult.success(builder.toString(), config.validate(dev.sato.worldprotect.protection.flag.FlagRegistry.withBuiltIns()));
    }

    private void writeRegion(StringBuilder builder, RegionConfig region) {
        String path = "regions." + region.id().getValue();
        builder.append('[').append(path).append("]\n");
        builder.append("dimension = ").append(quote(region.dimension().asString())).append('\n');
        builder.append("priority = ").append(region.priority()).append('\n');
        if (region.parentId().isPresent()) {
            builder.append("parent = ").append(quote(region.parentId().get().getValue())).append('\n');
        }

        builder.append('[').append(path).append(".bounds]\n");
        writeBounds(builder, region.bounds());

        if (!region.subjectsConfig().isEmpty()) {
            builder.append('[').append(path).append(".subjects]\n");
            writeSubjects(builder, region.subjectsConfig());
        }

        if (!RegionAccessPolicyConfig.defaults().equals(region.accessPolicyConfig())) {
            builder.append('[').append(path).append(".access]\n");
            writeAccessPolicy(builder, region.accessPolicyConfig());
        }

        if (!region.flags().isEmpty()) {
            writeFlags(builder, path, region.flags());
        }
    }

    private void writeBounds(StringBuilder builder, BoundsConfig bounds) {
        if (bounds.isGlobal()) {
            builder.append("type = ").append(quote("global")).append('\n');
            return;
        }
        builder.append("type = ").append(quote("cuboid")).append('\n');
        builder.append("min = ").append(intArray(bounds.min().x(), bounds.min().y(), bounds.min().z())).append('\n');
        builder.append("max = ").append(intArray(bounds.max().x(), bounds.max().y(), bounds.max().z())).append('\n');
    }

    private void writeSubjects(StringBuilder builder, RegionSubjectsConfig subjectsConfig) {
        List<String> owners = subjectsConfig.owners().stream()
                .map(SubjectRefConfig::asString)
                .sorted()
                .collect(Collectors.toList());
        List<String> members = subjectsConfig.members().stream()
                .map(SubjectRefConfig::asString)
                .sorted()
                .collect(Collectors.toList());
        if (!owners.isEmpty()) {
            builder.append("owners = ").append(stringArray(owners)).append('\n');
        }
        if (!members.isEmpty()) {
            builder.append("members = ").append(stringArray(members)).append('\n');
        }
    }

    private void writeAccessPolicy(StringBuilder builder, RegionAccessPolicyConfig accessPolicyConfig) {
        if (accessPolicyConfig.ownersBypass() != null) {
            builder.append("owners-bypass = ").append(accessPolicyConfig.ownersBypass()).append('\n');
        }
        if (accessPolicyConfig.membersBypass() != null) {
            builder.append("members-bypass = ").append(accessPolicyConfig.membersBypass()).append('\n');
        }
        if (!accessPolicyConfig.ownerBypassFlags().isEmpty()) {
            builder.append("owner-bypass-flags = ").append(stringArray(sorted(accessPolicyConfig.ownerBypassFlags()))).append('\n');
        }
        if (!accessPolicyConfig.memberBypassFlags().isEmpty()) {
            builder.append("member-bypass-flags = ").append(stringArray(sorted(accessPolicyConfig.memberBypassFlags()))).append('\n');
        }
    }

    private void writeFlags(StringBuilder builder, String regionPath, Map<FlagKey, FlagRuleConfig> flags) {
        List<Map.Entry<FlagKey, FlagRuleConfig>> sortedEntries = flags.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getValue()))
                .collect(Collectors.toList());

        List<Map.Entry<FlagKey, FlagRuleConfig>> inlineFlags = sortedEntries.stream()
                .filter(entry -> entry.getValue().isSimple() && entry.getValue().group() == RegionGroup.ALL)
                .collect(Collectors.toList());
        List<Map.Entry<FlagKey, FlagRuleConfig>> tableFlags = sortedEntries.stream()
                .filter(entry -> !(entry.getValue().isSimple() && entry.getValue().group() == RegionGroup.ALL))
                .collect(Collectors.toList());

        if (!inlineFlags.isEmpty()) {
            builder.append('[').append(regionPath).append(".flags]\n");
            for (Map.Entry<FlagKey, FlagRuleConfig> entry : inlineFlags) {
                builder.append(entry.getKey().getValue())
                        .append(" = ")
                        .append(quote(flagState(entry.getValue().defaultState())))
                        .append('\n');
            }
        }

        for (Map.Entry<FlagKey, FlagRuleConfig> entry : tableFlags) {
            builder.append('[').append(regionPath).append(".flags.").append(entry.getKey().getValue()).append("]\n");
            writeFlagRule(builder, entry.getValue());
        }
    }

    private void writeFlagRule(StringBuilder builder, FlagRuleConfig rule) {
        if (rule.isSimple()) {
            builder.append("state = ").append(quote(flagState(rule.defaultState()))).append('\n');
            if (rule.group() != RegionGroup.ALL) {
                builder.append("group = ").append(quote(rule.group().configKey())).append('\n');
            }
            return;
        }

        builder.append("default = ").append(quote(flagState(rule.defaultState()))).append('\n');
        if (rule.group() != RegionGroup.ALL) {
            builder.append("group = ").append(quote(rule.group().configKey())).append('\n');
        }
        if (!rule.allowSelectors().isEmpty()) {
            builder.append("allow = ").append(stringArray(sorted(rule.allowSelectors()))).append('\n');
        }
        if (!rule.denySelectors().isEmpty()) {
            builder.append("deny = ").append(stringArray(sorted(rule.denySelectors()))).append('\n');
        }
    }

    private String flagState(FlagState state) {
        return state.name().toLowerCase(java.util.Locale.ROOT);
    }

    private List<String> sorted(List<String> values) {
        List<String> copy = new ArrayList<>(values);
        copy.sort(String::compareTo);
        return copy;
    }

    private String stringArray(List<String> values) {
        return "[" + values.stream().map(this::quote).collect(Collectors.joining(", ")) + "]";
    }

    private String intArray(int x, int y, int z) {
        return "[" + x + ", " + y + ", " + z + "]";
    }

    private String quote(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
