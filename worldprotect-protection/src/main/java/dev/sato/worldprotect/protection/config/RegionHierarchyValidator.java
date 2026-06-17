package dev.sato.worldprotect.protection.config;

import dev.sato.worldprotect.protection.region.RegionId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Validator to check parent references, dimension constraints, and circular inheritance at config level.
 */
public final class RegionHierarchyValidator {

    public ConfigValidationResult validate(WorldProtectConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        ConfigValidationResult result = ConfigValidationResult.ok();

        // Map regions by ID for quick lookup
        Map<RegionId, RegionConfig> registry = new HashMap<>();
        for (RegionConfig rc : config.regions()) {
            registry.put(rc.id(), rc);
        }

        for (RegionConfig rc : config.regions()) {
            Optional<RegionId> parentIdOpt = rc.parentId();
            if (parentIdOpt.isEmpty()) {
                continue;
            }
            RegionId parentId = parentIdOpt.get();
            String path = "regions." + rc.id().getValue() + ".parent";

            // 1. Parent ID must exist
            if (!registry.containsKey(parentId)) {
                result = result.add(ConfigValidationMessage.error(path, "Parent region '" + parentId.getValue() + "' does not exist"));
                continue;
            }

            RegionConfig parentConfig = registry.get(parentId);

            // 2. Parent and child must be in the same dimension
            if (!rc.dimension().equals(parentConfig.dimension())) {
                result = result.add(ConfigValidationMessage.error(path, "Parent region '" + parentId.getValue() 
                    + "' is in dimension '" + parentConfig.dimension().asString() 
                    + "', but child region '" + rc.id().getValue() 
                    + "' is in dimension '" + rc.dimension().asString() + "'"));
            }
        }

        // 3. Circular inheritance validation (cycle detection)
        Set<RegionId> visiting = new HashSet<>();
        Set<RegionId> visited = new HashSet<>();

        for (RegionConfig rc : config.regions()) {
            if (!visited.contains(rc.id())) {
                List<RegionId> pathList = new ArrayList<>();
                result = detectCycles(rc.id(), registry, visiting, visited, pathList, result);
            }
        }

        return result;
    }

    private ConfigValidationResult detectCycles(
            RegionId currentId,
            Map<RegionId, RegionConfig> registry,
            Set<RegionId> visiting,
            Set<RegionId> visited,
            List<RegionId> pathList,
            ConfigValidationResult result
    ) {
        visiting.add(currentId);
        pathList.add(currentId);

        RegionConfig rc = registry.get(currentId);
        if (rc != null && rc.parentId().isPresent()) {
            RegionId parentId = rc.parentId().get();
            if (visiting.contains(parentId)) {
                // Cycle detected!
                int cycleStartIndex = pathList.indexOf(parentId);
                StringBuilder cyclePath = new StringBuilder();
                for (int i = cycleStartIndex; i < pathList.size(); i++) {
                    cyclePath.append(pathList.get(i).getValue()).append(" -> ");
                }
                cyclePath.append(parentId.getValue());
                String errorPath = "regions." + currentId.getValue() + ".parent";
                result = result.add(ConfigValidationMessage.error(errorPath, "Circular inheritance detected: " + cyclePath.toString()));
            } else if (!visited.contains(parentId)) {
                result = detectCycles(parentId, registry, visiting, visited, pathList, result);
            }
        }

        pathList.remove(pathList.size() - 1);
        visiting.remove(currentId);
        visited.add(currentId);
        return result;
    }
}
