package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.minecraft.registry.ResourceRegistryView;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.rule.ResourceSelector;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service to validate configuration selectors against the current server/modpack registry view.
 */
public final class ConfigResourceValidator {

    public ConfigValidationResult validateResources(WorldProtectConfig config, ResourceRegistryView registryView) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(registryView, "registryView must not be null");

        ConfigValidationResult result = ConfigValidationResult.ok();

        for (RegionConfig rc : config.regions()) {
            String regionPath = "regions." + rc.id().getValue();
            for (Map.Entry<FlagKey, FlagRuleConfig> entry : rc.flags().entrySet()) {
                FlagKey flagKey = entry.getKey();
                FlagRuleConfig ruleConfig = entry.getValue();
                String flagPath = regionPath + ".flags." + flagKey.getValue();

                result = result.merge(validateRuleSelectors(ruleConfig, flagPath, registryView));
            }
        }

        return result;
    }

    private ConfigValidationResult validateRuleSelectors(
            FlagRuleConfig ruleConfig,
            String flagPath,
            ResourceRegistryView registryView
    ) {
        ConfigValidationResult result = ConfigValidationResult.ok();

        // Validate allow selectors
        List<String> allow = ruleConfig.allowSelectors();
        for (int i = 0; i < allow.size(); i++) {
            String selStr = allow.get(i);
            String path = flagPath + ".allow[" + i + "]";
            result = result.merge(validateSelector(selStr, path, registryView));
        }

        // Validate deny selectors
        List<String> deny = ruleConfig.denySelectors();
        for (int i = 0; i < deny.size(); i++) {
            String selStr = deny.get(i);
            String path = flagPath + ".deny[" + i + "]";
            result = result.merge(validateSelector(selStr, path, registryView));
        }

        return result;
    }

    private ConfigValidationResult validateSelector(
            String selectorStr,
            String path,
            ResourceRegistryView registryView
    ) {
        ConfigValidationResult result = ConfigValidationResult.ok();
        ResourceSelector selector;
        try {
            selector = ResourceSelector.parse(selectorStr);
        } catch (Exception e) {
            // Should not typically throw if structures were validated, but handle gracefully
            return ConfigValidationResult.ok().add(ConfigValidationMessage.error(path, "Invalid selector format: " + e.getMessage()));
        }

        switch (selector.kind()) {
            case EXACT:
                ResourceRef exactId = selector.id().orElse(null);
                if (exactId != null) {
                    String ns = exactId.namespace();
                    if (!registryView.namespaceLoaded(ns)) {
                        result = result.add(ConfigValidationMessage.error(path, "Namespace '" + ns + "' is not loaded in the current modpack/server runtime."));
                    }
                }
                break;
            case NAMESPACE_WILDCARD:
                String ns = selector.namespace().orElse("");
                if (!registryView.namespaceLoaded(ns)) {
                    result = result.add(ConfigValidationMessage.error(path, "Namespace '" + ns + "' is not loaded in the current modpack/server runtime."));
                }
                break;
            case GLOBAL_WILDCARD:
                // Always valid
                break;
            case TAG:
                result = result.add(ConfigValidationMessage.warning(path, "Tag selector parsed but tag membership validation is not implemented yet"));
                break;
        }

        return result;
    }
}
