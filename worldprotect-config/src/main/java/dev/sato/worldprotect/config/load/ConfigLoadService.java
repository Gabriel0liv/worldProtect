package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.config.toml.TomlConfigParseResult;
import dev.sato.worldprotect.config.toml.TomlConfigParser;
import dev.sato.worldprotect.minecraft.registry.ResourceRegistryView;
import dev.sato.worldprotect.protection.config.ConfigResourceValidator;
import dev.sato.worldprotect.protection.config.ConfigSeverity;
import dev.sato.worldprotect.protection.config.ConfigToDomainMapper;
import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.region.RegionSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service orchestrating the complete configuration loading, validation, and mapping pipeline.
 */
public final class ConfigLoadService {
    private final TomlConfigParser parser;
    private final FlagRegistry flagRegistry;
    private final ConfigResourceValidator resourceValidator;
    private final ConfigToDomainMapper mapper;
    private final ResourceRegistryView registryView;

    public ConfigLoadService(
            TomlConfigParser parser,
            FlagRegistry flagRegistry,
            ConfigResourceValidator resourceValidator,
            ConfigToDomainMapper mapper,
            ResourceRegistryView registryView
    ) {
        this.parser = Objects.requireNonNull(parser, "parser must not be null");
        this.flagRegistry = Objects.requireNonNull(flagRegistry, "flagRegistry must not be null");
        this.resourceValidator = Objects.requireNonNull(resourceValidator, "resourceValidator must not be null");
        this.mapper = Objects.requireNonNull(mapper, "mapper must not be null");
        this.registryView = registryView;
    }

    public ConfigLoadService(
            TomlConfigParser parser,
            FlagRegistry flagRegistry,
            ConfigResourceValidator resourceValidator,
            ConfigToDomainMapper mapper,
            Optional<ResourceRegistryView> registryView
    ) {
        this(
                parser,
                flagRegistry,
                resourceValidator,
                mapper,
                Objects.requireNonNull(registryView, "registryView must not be null").orElse(null)
        );
    }

    public static ConfigLoadService createDefault(FlagRegistry flagRegistry) {
        Objects.requireNonNull(flagRegistry, "flagRegistry must not be null");
        return new ConfigLoadService(
                new TomlConfigParser(),
                flagRegistry,
                new ConfigResourceValidator(),
                new ConfigToDomainMapper(),
                (ResourceRegistryView) null
        );
    }

    public ConfigLoadResult load(ConfigSource source) {
        return load(source, ConfigLoadOptions.defaults());
    }

    public ConfigLoadResult load(ConfigSource source, ConfigLoadOptions options) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(options, "options must not be null");

        // 1. Parse configuration
        TomlConfigParseResult parseResult;
        try {
            parseResult = source.parse(parser);
        } catch (Exception e) {
            ConfigValidationResult errorResult = ConfigValidationResult.ok().add(
                    ConfigValidationMessage.error("parser", "Parser exception: " + e.getMessage())
            );
            return ConfigLoadResult.failure(errorResult);
        }

        if (parseResult.hasErrors() || !parseResult.hasConfig()) {
            return ConfigLoadResult.failure(parseResult.diagnostics());
        }

        WorldProtectConfig rawConfig = parseResult.config().get();
        ConfigValidationResult diagnostics = parseResult.diagnostics();

        // 2. Validate structures and domains
        ConfigValidationResult structuralResult;
        try {
            structuralResult = rawConfig.validate(flagRegistry);
        } catch (Exception e) {
            ConfigValidationResult errorResult = diagnostics.merge(
                    ConfigValidationResult.ok().add(
                            ConfigValidationMessage.error("validation", "Validation exception: " + e.getMessage())
                    )
            );
            return ConfigLoadResult.failure(errorResult);
        }
        diagnostics = diagnostics.merge(structuralResult);

        // 3. Validate resources (namespaces, tags)
        if (options.validateResources()) {
            if (registryView == null) {
                diagnostics = diagnostics.add(
                        ConfigValidationMessage.warning("resources", "Resource validation requested but no ResourceRegistryView was provided")
                );
            } else {
                try {
                    ConfigValidationResult resourceResult = resourceValidator.validateResources(rawConfig, registryView);
                    diagnostics = diagnostics.merge(resourceResult);
                } catch (Exception e) {
                    diagnostics = diagnostics.add(
                            ConfigValidationMessage.error("resources", "Resource validation exception: " + e.getMessage())
                    );
                }
            }
        }

        // 4. Handle failOnWarnings / strict behavior
        if (options.failOnWarnings() && diagnostics.hasWarnings()) {
            List<ConfigValidationMessage> messages = new ArrayList<>();
            for (ConfigValidationMessage msg : diagnostics.messages()) {
                if (msg.severity() == ConfigSeverity.WARNING) {
                    messages.add(ConfigValidationMessage.error(msg.path(), msg.message()));
                } else {
                    messages.add(msg);
                }
            }
            diagnostics = ConfigValidationResult.of(messages);
        }

        if (diagnostics.hasErrors()) {
            return ConfigLoadResult.failure(diagnostics);
        }

        // 5. Map to domain protection models
        RegionSet regionSet;
        try {
            regionSet = mapper.toRegionSet(rawConfig);
        } catch (Exception e) {
            ConfigValidationResult mapperDiag = diagnostics.add(
                    ConfigValidationMessage.error("mapper", "Mapping to domain failed: " + e.getMessage())
            );
            return ConfigLoadResult.failure(mapperDiag);
        }

        // 6. Return successfully loaded config
        LoadedWorldProtectConfig loadedConfig;
        try {
            loadedConfig = LoadedWorldProtectConfig.of(rawConfig, regionSet, diagnostics);
        } catch (IllegalArgumentException e) {
            return ConfigLoadResult.failure(diagnostics);
        }

        return ConfigLoadResult.success(loadedConfig);
    }

    public TomlConfigParser parser() {
        return parser;
    }

    public FlagRegistry flagRegistry() {
        return flagRegistry;
    }

    public ConfigResourceValidator resourceValidator() {
        return resourceValidator;
    }

    public ConfigToDomainMapper mapper() {
        return mapper;
    }

    public Optional<ResourceRegistryView> registryView() {
        return Optional.ofNullable(registryView);
    }
}
