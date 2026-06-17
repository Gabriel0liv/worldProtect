package dev.sato.worldprotect.config.persistence;

import dev.sato.worldprotect.config.load.ConfigLoadResult;
import dev.sato.worldprotect.config.load.ConfigLoadService;
import dev.sato.worldprotect.config.load.StringTomlConfigSource;
import dev.sato.worldprotect.config.toml.TomlConfigWriteResult;
import dev.sato.worldprotect.config.toml.TomlConfigWriter;
import dev.sato.worldprotect.protection.config.ConfigValidationMessage;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.management.RegionManagementResult;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Repository coordinating load, validate, serialize and safe save-back.
 */
public final class WorldProtectConfigRepository {
    private final ConfigStore store;
    private final ConfigLoadService loadService;
    private final TomlConfigWriter writer;
    private final FlagRegistry flagRegistry;

    public WorldProtectConfigRepository(
            ConfigStore store,
            ConfigLoadService loadService,
            TomlConfigWriter writer,
            FlagRegistry flagRegistry
    ) {
        this.store = Objects.requireNonNull(store, "store must not be null");
        this.loadService = Objects.requireNonNull(loadService, "loadService must not be null");
        this.writer = Objects.requireNonNull(writer, "writer must not be null");
        this.flagRegistry = Objects.requireNonNull(flagRegistry, "flagRegistry must not be null");
    }

    public static WorldProtectConfigRepository withBuiltIns(ConfigStore store) {
        FlagRegistry registry = FlagRegistry.withBuiltIns();
        return new WorldProtectConfigRepository(store, ConfigLoadService.createDefault(registry), new TomlConfigWriter(), registry);
    }

    public ConfigRepositoryResult<WorldProtectConfig> load() {
        ConfigStoreReadResult readResult = store.read();
        if (!readResult.isSuccess()) {
            return ConfigRepositoryResult.failure(
                    ConfigRepositoryStatus.LOAD_FAILED,
                    ConfigValidationResult.ok().add(ConfigValidationMessage.error("store", readResult.message())),
                    readResult.message()
            );
        }

        ConfigLoadResult loadResult = loadService.load(StringTomlConfigSource.of(store.description(), readResult.content().get()));
        if (!loadResult.isSuccess()) {
            return ConfigRepositoryResult.failure(ConfigRepositoryStatus.LOAD_FAILED, loadResult.diagnostics(), "Failed to load config from " + store.description());
        }

        return ConfigRepositoryResult.success(
                loadResult.loadedConfig().get().rawConfig(),
                loadResult.diagnostics(),
                "Loaded config from " + store.description(),
                Optional.of(readResult.content().get())
        );
    }

    public ConfigRepositoryResult<WorldProtectConfig> save(WorldProtectConfig config) {
        Objects.requireNonNull(config, "config must not be null");

        ConfigValidationResult validation = config.validate(flagRegistry);
        if (validation.hasErrors()) {
            return ConfigRepositoryResult.failure(ConfigRepositoryStatus.VALIDATION_FAILED, validation, "Config failed validation before save");
        }

        TomlConfigWriteResult writeResult = writer.write(config);
        if (!writeResult.isSuccess()) {
            return ConfigRepositoryResult.failure(ConfigRepositoryStatus.SERIALIZATION_FAILED, writeResult.diagnostics(), "Failed to serialize config");
        }

        ConfigStoreWriteResult storeWriteResult = store.write(writeResult.content().get());
        if (!storeWriteResult.isSuccess()) {
            return ConfigRepositoryResult.failure(
                    ConfigRepositoryStatus.WRITE_FAILED,
                    validation.add(ConfigValidationMessage.error("store", storeWriteResult.message())),
                    storeWriteResult.message()
            );
        }

        return ConfigRepositoryResult.success(config, validation, "Saved config to " + store.description(), Optional.of(writeResult.content().get()));
    }

    public ConfigRepositoryResult<WorldProtectConfig> update(
            Function<WorldProtectConfig, RegionManagementResult<WorldProtectConfig>> mutation
    ) {
        Objects.requireNonNull(mutation, "mutation must not be null");

        ConfigRepositoryResult<WorldProtectConfig> loadResult = load();
        if (loadResult.isFailure()) {
            return loadResult;
        }

        RegionManagementResult<WorldProtectConfig> mutationResult = mutation.apply(loadResult.value());
        if (mutationResult.isFailure()) {
            return ConfigRepositoryResult.failure(
                    ConfigRepositoryStatus.MUTATION_FAILED,
                    mutationResult.diagnostics(),
                    mutationResult.message()
            );
        }
        if (mutationResult.status() == dev.sato.worldprotect.protection.management.RegionManagementStatus.NO_CHANGE) {
            return ConfigRepositoryResult.noChange(loadResult.value(), mutationResult.diagnostics(), mutationResult.message());
        }

        WorldProtectConfig updatedConfig = mutationResult.value();
        ConfigValidationResult validation = updatedConfig.validate(flagRegistry);
        if (validation.hasErrors()) {
            return ConfigRepositoryResult.failure(ConfigRepositoryStatus.VALIDATION_FAILED, validation, "Updated config failed validation");
        }

        TomlConfigWriteResult writeResult = writer.write(updatedConfig);
        if (!writeResult.isSuccess()) {
            return ConfigRepositoryResult.failure(ConfigRepositoryStatus.SERIALIZATION_FAILED, writeResult.diagnostics(), "Failed to serialize updated config");
        }

        ConfigStoreWriteResult storeWriteResult = store.write(writeResult.content().get());
        if (!storeWriteResult.isSuccess()) {
            return ConfigRepositoryResult.failure(
                    ConfigRepositoryStatus.WRITE_FAILED,
                    validation.add(ConfigValidationMessage.error("store", storeWriteResult.message())),
                    storeWriteResult.message()
            );
        }

        return ConfigRepositoryResult.success(
                updatedConfig,
                validation,
                mutationResult.message(),
                mutationResult.mutationPlan(),
                Optional.of(writeResult.content().get())
        );
    }
}
