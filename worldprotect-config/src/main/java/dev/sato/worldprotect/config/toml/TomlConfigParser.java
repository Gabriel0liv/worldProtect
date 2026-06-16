package dev.sato.worldprotect.config.toml;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagState;
import dev.sato.worldprotect.protection.region.RegionId;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseError;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service to parse TOML configuration strings or files into WorldProtectConfig.
 */
public final class TomlConfigParser {

    public TomlConfigParseResult parseString(String toml) {
        Objects.requireNonNull(toml, "toml input must not be null");

        TomlParseResult result = Toml.parse(toml);
        if (result.hasErrors()) {
            ConfigValidationResult diagnostics = ConfigValidationResult.ok();
            for (TomlParseError err : result.errors()) {
                diagnostics = diagnostics.add(ConfigValidationMessage.error("toml", err.toString()));
            }
            return TomlConfigParseResult.failure(diagnostics);
        }

        ConfigValidationResult diagnostics = ConfigValidationResult.ok();
        TomlTable regionsTable = result.getTable("regions");
        if (regionsTable == null) {
            diagnostics = diagnostics.add(ConfigValidationMessage.error("regions", "No regions table found"));
            return TomlConfigParseResult.failure(diagnostics);
        }

        List<RegionConfig> regionsList = new ArrayList<>();

        for (String regionKey : regionsTable.keySet()) {
            Object regionVal = regionsTable.get(regionKey);
            if (!(regionVal instanceof TomlTable)) {
                diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey, "Region definition must be a table"));
                continue;
            }

            TomlTable region = (TomlTable) regionVal;
            boolean regionValid = true;

            // 1. RegionId validation
            RegionId regionId = null;
            try {
                regionId = RegionId.of(regionKey);
            } catch (Exception e) {
                diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey, "Invalid region ID: " + e.getMessage()));
                regionValid = false;
            }

            // 2. Dimension Ref validation
            DimensionRef dimension = null;
            String dimStr = region.getString("dimension");
            if (dimStr == null) {
                diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".dimension", "Missing dimension"));
                regionValid = false;
            } else {
                try {
                    dimension = new DimensionRef(ResourceRef.parse(dimStr));
                } catch (Exception e) {
                    diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".dimension", "Invalid dimension: " + e.getMessage()));
                    regionValid = false;
                }
            }

            // 3. Priority validation
            int priority = 0;
            if (!region.contains("priority")) {
                diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".priority", "Missing priority"));
                regionValid = false;
            } else {
                Object prioObj = region.get("priority");
                if (!(prioObj instanceof Long)) {
                    diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".priority", "Priority must be an integer"));
                    regionValid = false;
                } else {
                    priority = ((Long) prioObj).intValue();
                }
            }

            // 4. Bounds validation
            BoundsConfig boundsConfig = null;
            TomlTable boundsTable = region.getTable("bounds");
            if (boundsTable == null) {
                diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".bounds", "Missing bounds table"));
                regionValid = false;
            } else {
                boolean boundsValid = true;
                String typeStr = boundsTable.getString("type");
                if (typeStr == null) {
                    diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".bounds", "Missing bounds type"));
                    boundsValid = false;
                } else if (!typeStr.equals("cuboid")) {
                    diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".bounds", "Unsupported bounds type: " + typeStr));
                    boundsValid = false;
                }

                TomlArray minArray = boundsTable.getArray("min");
                TomlArray maxArray = boundsTable.getArray("max");

                ConfigValidationResult[] minDiagRef = new ConfigValidationResult[]{ConfigValidationResult.ok()};
                BlockPosRef minPos = parseCoordinates(minArray, "regions." + regionKey + ".bounds.min", minDiagRef);
                if (minPos == null) {
                    diagnostics = diagnostics.merge(minDiagRef[0]);
                    boundsValid = false;
                }

                ConfigValidationResult[] maxDiagRef = new ConfigValidationResult[]{ConfigValidationResult.ok()};
                BlockPosRef maxPos = parseCoordinates(maxArray, "regions." + regionKey + ".bounds.max", maxDiagRef);
                if (maxPos == null) {
                    diagnostics = diagnostics.merge(maxDiagRef[0]);
                    boundsValid = false;
                }

                if (boundsValid) {
                    boundsConfig = BoundsConfig.cuboid(minPos, maxPos);
                } else {
                    regionValid = false;
                }
            }

            // 5. Flags validation
            Map<FlagKey, FlagRuleConfig> flagsMap = new HashMap<>();
            TomlTable flagsTable = region.getTable("flags");
            if (flagsTable != null) {
                for (String flagKeyStr : flagsTable.keySet()) {
                    FlagKey flagKey = null;
                    try {
                        flagKey = FlagKey.of(flagKeyStr);
                    } catch (Exception e) {
                        diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr, "Invalid flag key: " + e.getMessage()));
                        regionValid = false;
                        continue;
                    }

                    Object flagValObj = flagsTable.get(flagKeyStr);
                    if (flagValObj instanceof String) {
                        String stateStr = (String) flagValObj;
                        FlagState state = parseFlagState(stateStr);
                        if (state == null) {
                            diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr, "Invalid flag state: " + stateStr));
                            regionValid = false;
                            continue;
                        }
                        flagsMap.put(flagKey, FlagRuleConfig.simple(state));
                    } else if (flagValObj instanceof TomlTable) {
                        TomlTable condTable = (TomlTable) flagValObj;
                        String defStr = condTable.getString("default");
                        if (defStr == null) {
                            diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr + ".default", "Missing default state in conditional flag rule"));
                            regionValid = false;
                            continue;
                        }
                        FlagState defaultState = parseFlagState(defStr);
                        if (defaultState == null) {
                            diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr + ".default", "Invalid default flag state: " + defStr));
                            regionValid = false;
                            continue;
                        }

                        List<String> allowList = new ArrayList<>();
                        if (condTable.contains("allow")) {
                            TomlArray allowArray = condTable.getArray("allow");
                            if (allowArray == null) {
                                diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr + ".allow", "allow must be an array of strings"));
                                regionValid = false;
                                continue;
                            }
                            boolean listValid = true;
                            for (int i = 0; i < allowArray.size(); i++) {
                                Object item = allowArray.get(i);
                                if (!(item instanceof String)) {
                                    diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr + ".allow[" + i + "]", "Allow list element must be a string"));
                                    listValid = false;
                                    break;
                                }
                                allowList.add((String) item);
                            }
                            if (!listValid) {
                                regionValid = false;
                                continue;
                            }
                        }

                        List<String> denyList = new ArrayList<>();
                        if (condTable.contains("deny")) {
                            TomlArray denyArray = condTable.getArray("deny");
                            if (denyArray == null) {
                                diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr + ".deny", "deny must be an array of strings"));
                                regionValid = false;
                                continue;
                            }
                            boolean listValid = true;
                            for (int i = 0; i < denyArray.size(); i++) {
                                Object item = denyArray.get(i);
                                if (!(item instanceof String)) {
                                    diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr + ".deny[" + i + "]", "Deny list element must be a string"));
                                    listValid = false;
                                    break;
                                }
                                denyList.add((String) item);
                            }
                            if (!listValid) {
                                regionValid = false;
                                continue;
                            }
                        }

                        try {
                            flagsMap.put(flagKey, FlagRuleConfig.conditional(defaultState, allowList, denyList));
                        } catch (Exception e) {
                            diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr, e.getMessage()));
                            regionValid = false;
                        }
                    } else {
                        diagnostics = diagnostics.add(ConfigValidationMessage.error("regions." + regionKey + ".flags." + flagKeyStr, "Flag value must be a string or a table"));
                        regionValid = false;
                    }
                }
            }

            if (regionValid) {
                regionsList.add(RegionConfig.of(regionId, dimension, priority, boundsConfig, flagsMap));
            }
        }

        if (diagnostics.hasErrors()) {
            return TomlConfigParseResult.failure(diagnostics);
        } else {
            WorldProtectConfig config = WorldProtectConfig.of(regionsList);
            return TomlConfigParseResult.success(config, diagnostics);
        }
    }

    public TomlConfigParseResult parseFile(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try {
            if (!Files.exists(path)) {
                return TomlConfigParseResult.failure(ConfigValidationResult.ok()
                        .add(ConfigValidationMessage.error("file", "File does not exist: " + path.toAbsolutePath())));
            }
            String tomlContent = Files.readString(path, StandardCharsets.UTF_8);
            return parseString(tomlContent);
        } catch (IOException e) {
            return TomlConfigParseResult.failure(ConfigValidationResult.ok()
                    .add(ConfigValidationMessage.error("file", "Failed to read file: " + e.getMessage())));
        }
    }

    private BlockPosRef parseCoordinates(TomlArray array, String path, ConfigValidationResult[] diagnosticsRef) {
        if (array == null) {
            diagnosticsRef[0] = diagnosticsRef[0].add(ConfigValidationMessage.error(path, "Missing coordinate array"));
            return null;
        }
        if (array.size() != 3) {
            diagnosticsRef[0] = diagnosticsRef[0].add(ConfigValidationMessage.error(path, "Coordinate array must contain exactly 3 coordinates"));
            return null;
        }
        int[] coords = new int[3];
        for (int i = 0; i < 3; i++) {
            Object val = array.get(i);
            if (!(val instanceof Long)) {
                diagnosticsRef[0] = diagnosticsRef[0].add(ConfigValidationMessage.error(path, "Coordinates must be integers, got: " + val));
                return null;
            }
            coords[i] = ((Long) val).intValue();
        }
        return new BlockPosRef(coords[0], coords[1], coords[2]);
    }

    private FlagState parseFlagState(String value) {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase("allow")) {
            return FlagState.ALLOW;
        }
        if (value.equalsIgnoreCase("deny")) {
            return FlagState.DENY;
        }
        if (value.equalsIgnoreCase("pass")) {
            return FlagState.PASS;
        }
        return null;
    }
}
