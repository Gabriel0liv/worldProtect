package dev.sato.worldprotect.config.load;

import dev.sato.worldprotect.config.toml.TomlConfigParseResult;
import dev.sato.worldprotect.config.toml.TomlConfigParser;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.minecraft.registry.ResourceKind;
import dev.sato.worldprotect.minecraft.registry.ResourceRegistryView;
import dev.sato.worldprotect.protection.config.ConfigResourceValidator;
import dev.sato.worldprotect.protection.config.ConfigToDomainMapper;
import dev.sato.worldprotect.protection.config.ConfigValidationResult;
import dev.sato.worldprotect.protection.config.FlagRuleConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.flag.BuiltInFlags;
import dev.sato.worldprotect.protection.flag.FlagKey;
import dev.sato.worldprotect.protection.flag.FlagRegistry;
import dev.sato.worldprotect.protection.region.CuboidRegion;
import dev.sato.worldprotect.protection.subject.SubjectRef;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class ConfigLoadServiceTest {

    private final FlagRegistry flagRegistry = FlagRegistry.withBuiltIns();

    @Test
    public void testCreateDefaultAndConstructor() {
        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry);
        assertNotNull(service.parser());
        assertEquals(flagRegistry, service.flagRegistry());
        assertNotNull(service.resourceValidator());
        assertNotNull(service.mapper());
        assertFalse(service.registryView().isPresent());

        // Test collaborator constructor with standard reference
        ConfigLoadService service2 = new ConfigLoadService(
                new TomlConfigParser(),
                flagRegistry,
                new ConfigResourceValidator(),
                new ConfigToDomainMapper(),
                (ResourceRegistryView) null
        );
        assertNotNull(service2);

        // Test collaborator constructor with Optional
        ConfigLoadService service3 = new ConfigLoadService(
                new TomlConfigParser(),
                flagRegistry,
                new ConfigResourceValidator(),
                new ConfigToDomainMapper(),
                Optional.empty()
        );
        assertNotNull(service3);
    }

    @Test
    public void testNullInputsThrow() {
        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry);
        assertThrows(NullPointerException.class, () -> service.load(null));
        assertThrows(NullPointerException.class, () -> service.load(null, ConfigLoadOptions.defaults()));
        assertThrows(NullPointerException.class, () -> service.load(StringTomlConfigSource.ofToml(""), null));
    }

    @Test
    public void testLoadValidConfigSuccess() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags]\n" +
                "build = \"deny\"\n" +
                "place-block = \"allow\"\n";

        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry);
        ConfigLoadResult result = service.load(StringTomlConfigSource.ofToml(toml));

        assertTrue(result.isSuccess());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertTrue(result.loadedConfig().isPresent());

        LoadedWorldProtectConfig loaded = result.loadedConfig().get();
        assertNotNull(loaded.rawConfig());
        assertNotNull(loaded.regionSet());
        assertEquals(1, loaded.regionSet().regions().size());
    }

    @Test
    public void testParserErrorsTrapped() {
        String toml = "regions = { invalid [ ["; // Syntax error
        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry);
        ConfigLoadResult result = service.load(StringTomlConfigSource.ofToml(toml));

        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertFalse(result.loadedConfig().isPresent());
        assertTrue(result.diagnostics().messages().stream()
                .anyMatch(msg -> msg.path().equals("toml") || msg.path().equals("parser")));
    }

    @Test
    public void testStructuralErrorsTrapped() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags]\n" +
                "build = \"deny\"\n" +
                "[regions.spawn]\n" + // Duplicate ID
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags]\n" +
                "build = \"deny\"\n";

        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry);
        ConfigLoadResult result = service.load(StringTomlConfigSource.ofToml(toml));

        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
    }

    @Test
    public void testResourceValidationWarningWithNullRegistry() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags.build]\n" +
                "default = \"deny\"\n" +
                "allow = [\"minecraft:stone\"]\n";

        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry); // registryView is null
        ConfigLoadResult result = service.load(
                StringTomlConfigSource.ofToml(toml),
                ConfigLoadOptions.validatingResources()
        );

        assertTrue(result.isSuccess()); // Warning does not cause structural failure by default
        assertTrue(result.hasWarnings());
        assertFalse(result.hasErrors());
        assertTrue(result.diagnostics().messages().stream()
                .anyMatch(msg -> msg.path().equals("resources") && msg.message().contains("no ResourceRegistryView was provided")));
    }

    @Test
    public void testStrictModeFailsWithNullRegistry() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags.build]\n" +
                "default = \"deny\"\n" +
                "allow = [\"minecraft:stone\"]\n";

        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry); // registryView is null
        ConfigLoadResult result = service.load(
                StringTomlConfigSource.ofToml(toml),
                ConfigLoadOptions.strict()
        );

        assertFalse(result.isSuccess()); // Strict mode converts warnings to errors
        assertTrue(result.hasErrors());
        assertTrue(result.diagnostics().messages().stream()
                .anyMatch(msg -> msg.path().equals("resources") && msg.severity() == dev.sato.worldprotect.protection.config.ConfigSeverity.ERROR));
    }

    @Test
    public void testResourceValidationWithMockRegistry() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags.build]\n" +
                "default = \"deny\"\n" +
                "allow = [\"minecraft:stone\"]\n";

        ResourceRegistryView mockRegistry = new ResourceRegistryView() {
            @Override
            public boolean namespaceLoaded(String namespace) {
                return "minecraft".equals(namespace);
            }
            @Override
            public boolean exists(ResourceKind kind, ResourceRef id) {
                return true;
            }
            @Override
            public Set<String> loadedNamespaces() {
                return Set.of("minecraft");
            }
            @Override
            public Set<ResourceRef> ids(ResourceKind kind) {
                return Set.of();
            }
        };

        ConfigLoadService service = new ConfigLoadService(
                new TomlConfigParser(),
                flagRegistry,
                new ConfigResourceValidator(),
                new ConfigToDomainMapper(),
                mockRegistry
        );

        ConfigLoadResult result = service.load(
                StringTomlConfigSource.ofToml(toml),
                ConfigLoadOptions.validatingResources()
        );

        assertTrue(result.isSuccess());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
    }

    @Test
    public void testResourceValidationFailureWithUnloadedNamespace() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags.build]\n" +
                "default = \"deny\"\n" +
                "allow = [\"unloaded_mod:stone\"]\n";

        ResourceRegistryView mockRegistry = new ResourceRegistryView() {
            @Override
            public boolean namespaceLoaded(String namespace) {
                return "minecraft".equals(namespace);
            }
            @Override
            public boolean exists(ResourceKind kind, ResourceRef id) {
                return true;
            }
            @Override
            public Set<String> loadedNamespaces() {
                return Set.of("minecraft");
            }
            @Override
            public Set<ResourceRef> ids(ResourceKind kind) {
                return Set.of();
            }
        };

        ConfigLoadService service = new ConfigLoadService(
                new TomlConfigParser(),
                flagRegistry,
                new ConfigResourceValidator(),
                new ConfigToDomainMapper(),
                mockRegistry
        );

        ConfigLoadResult result = service.load(
                StringTomlConfigSource.ofToml(toml),
                ConfigLoadOptions.validatingResources()
        );

        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertTrue(result.diagnostics().messages().stream()
                .anyMatch(msg -> msg.path().contains("allow") && msg.message().contains("Namespace 'unloaded_mod' is not loaded")));
    }

    @Test
    public void testMapperExceptionIsTrapped() {
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags.build]\n" +
                "default = \"deny\"\n" +
                "allow = [\"minecraft:stone\"]\n";

        // 1. Parse once to get a reference to the rule we want to mutate
        TomlConfigParser tempParser = new TomlConfigParser();
        TomlConfigParseResult parseResult = tempParser.parseString(toml);
        assertTrue(parseResult.isSuccess());
        WorldProtectConfig tempConfig = parseResult.config().get();
        FlagRuleConfig ruleToMutate = tempConfig.regions().get(0).flags().get(FlagKey.of("build"));
        assertNotNull(ruleToMutate);

        // 2. Create the custom registry view that will mutate this rule instance on namespace lookup
        ResourceRegistryView mockRegistry = new ResourceRegistryView() {
            @Override
            public boolean namespaceLoaded(String namespace) {
                try {
                    Field allowField = FlagRuleConfig.class.getDeclaredField("allowSelectors");
                    allowField.setAccessible(true);
                    allowField.set(ruleToMutate, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            @Override
            public boolean exists(ResourceKind kind, ResourceRef id) {
                return true;
            }
            @Override
            public Set<String> loadedNamespaces() {
                return Set.of();
            }
            @Override
            public Set<ResourceRef> ids(ResourceKind kind) {
                return Set.of();
            }
        };

        ConfigLoadService service = new ConfigLoadService(
                new TomlConfigParser(),
                flagRegistry,
                new ConfigResourceValidator(),
                new ConfigToDomainMapper(),
                mockRegistry
        );

        // 3. We load using a ConfigSource that returns the pre-parsed tempConfig containing that exact ruleToMutate instance
        ConfigSource customSource = new ConfigSource() {
            @Override
            public String description() {
                return "custom-source";
            }

            @Override
            public TomlConfigParseResult parse(TomlConfigParser parser) {
                return TomlConfigParseResult.success(tempConfig, ConfigValidationResult.ok());
            }
        };

        ConfigLoadResult result = service.load(
                customSource,
                ConfigLoadOptions.validatingResources() // Must validate resources to trigger namespaceLoaded
        );

        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertTrue(result.diagnostics().messages().stream()
                .anyMatch(msg -> msg.path().equals("mapper") && msg.message().contains("Mapping to domain failed")));
    }

    @Test
    public void testLoadConfigWithSubjectsAndAccessSuccess() {
        UUID ownerUuid = UUID.randomUUID();
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.flags]\n" +
                "break-block = \"deny\"\n" +
                "[regions.spawn.subjects]\n" +
                "owners = [\"player:" + ownerUuid + "\"]\n" +
                "members = [\"group:trusted\", \"console\"]\n" +
                "[regions.spawn.access]\n" +
                "owners-bypass = false\n" +
                "members-bypass = true\n" +
                "owner-bypass-flags = [\"break-block\"]\n" +
                "member-bypass-flags = [\"place-block\"]\n";

        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry);
        ConfigLoadResult result = service.load(StringTomlConfigSource.ofToml(toml));

        assertTrue(result.isSuccess());
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertTrue(result.loadedConfig().isPresent());

        LoadedWorldProtectConfig loaded = result.loadedConfig().get();
        CuboidRegion region = (CuboidRegion) loaded.regionSet().regions().get(0);

        assertTrue(region.subjects().isOwner(SubjectRef.player(ownerUuid)));
        assertTrue(region.subjects().isMember(SubjectRef.group("trusted")));
        assertTrue(region.subjects().isMember(SubjectRef.console()));

        assertFalse(region.accessPolicy().ownersBypassFlags());
        assertTrue(region.accessPolicy().membersBypassFlags());
        assertTrue(region.accessPolicy().ownerBypassFlags().contains(BuiltInFlags.BREAK_BLOCK_KEY));
        assertTrue(region.accessPolicy().memberBypassFlags().contains(BuiltInFlags.PLACE_BLOCK_KEY));
    }

    @Test
    public void testLoadConfigWithInvalidSubjectsOrAccessFails() {
        // Unknown flag in access configuration
        String toml =
                "[regions.spawn]\n" +
                "dimension = \"minecraft:overworld\"\n" +
                "priority = 10\n" +
                "[regions.spawn.bounds]\n" +
                "type = \"cuboid\"\n" +
                "min = [0, 60, 0]\n" +
                "max = [100, 80, 100]\n" +
                "[regions.spawn.access]\n" +
                "owner-bypass-flags = [\"completely-unknown-flag-key\"]\n";

        ConfigLoadService service = ConfigLoadService.createDefault(flagRegistry);
        ConfigLoadResult result = service.load(StringTomlConfigSource.ofToml(toml));

        assertFalse(result.isSuccess());
        assertTrue(result.hasErrors());
        assertTrue(result.diagnostics().messages().stream()
                .anyMatch(msg -> msg.path().contains("owner-bypass-flags") && msg.message().contains("Unknown flag key")));
    }
}
