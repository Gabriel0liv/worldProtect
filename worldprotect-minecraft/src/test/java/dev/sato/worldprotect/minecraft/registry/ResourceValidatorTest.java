package dev.sato.worldprotect.minecraft.registry;

import dev.sato.worldprotect.minecraft.ResourceRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class ResourceValidatorTest {

    private FakeResourceRegistryView registry;
    private ResourceValidator validator;

    @BeforeEach
    public void setUp() {
        registry = new FakeResourceRegistryView();
        validator = new ResourceValidator(registry);
    }

    @Test
    public void testExistingBlockFromLoadedNamespace() {
        registry.addNamespace("create");
        ResourceRef pressId = ResourceRef.of("create", "mechanical_press");
        registry.registerId(ResourceKind.BLOCK, pressId);

        ResourceValidationResult result = validator.validate(ResourceKind.BLOCK, pressId);
        assertTrue(result.valid(), "Should be valid since namespace is loaded and block exists");
        assertTrue(result.messages().isEmpty());
    }

    @Test
    public void testMissingNamespaceFails() {
        ResourceRef pressId = ResourceRef.of("create", "mechanical_press");
        // 'create' namespace is NOT added to registry

        ResourceValidationResult result = validator.validate(ResourceKind.BLOCK, pressId);
        assertFalse(result.valid(), "Should fail since namespace is not loaded");
        assertEquals(1, result.messages().size());
        assertTrue(result.messages().get(0).contains("is not loaded"));
    }

    @Test
    public void testExistingNamespaceButMissingBlockFails() {
        registry.addNamespace("create"); // Namespace loaded
        ResourceRef fakeBlock = ResourceRef.of("create", "fake_block");

        ResourceValidationResult result = validator.validate(ResourceKind.BLOCK, fakeBlock);
        assertFalse(result.valid(), "Should fail since block does not exist in registry");
        assertEquals(1, result.messages().size());
        assertTrue(result.messages().get(0).contains("does not exist in registry kind"));
    }

    @Test
    public void testKindIsolation() {
        registry.addNamespace("create");
        ResourceRef id = ResourceRef.of("create", "mechanical_press");
        
        // Register it ONLY as an ITEM, not as a BLOCK
        registry.registerId(ResourceKind.ITEM, id);

        // Validating as ITEM should pass
        ResourceValidationResult resultItem = validator.validate(ResourceKind.ITEM, id);
        assertTrue(resultItem.valid());

        // Validating as BLOCK should fail
        ResourceValidationResult resultBlock = validator.validate(ResourceKind.BLOCK, id);
        assertFalse(resultBlock.valid(), "Should fail because it is registered as ITEM, not BLOCK");
    }

    @Test
    public void testUnknownKindValidatesNamespaceOnly() {
        registry.addNamespace("create");
        ResourceRef unregisteredId = ResourceRef.of("create", "some_unregistered_thing");

        // Validate as UNKNOWN
        ResourceValidationResult result = validator.validate(ResourceKind.UNKNOWN, unregisteredId);
        assertTrue(result.valid(), "UNKNOWN kind should only validate namespace presence");
        
        // Validate with completely missing namespace as UNKNOWN should still fail
        ResourceRef missingNamespaceId = ResourceRef.of("missing_mod", "thing");
        ResourceValidationResult resultMissing = validator.validate(ResourceKind.UNKNOWN, missingNamespaceId);
        assertFalse(resultMissing.valid(), "UNKNOWN should still fail if namespace is not loaded");
    }

    @Test
    public void testMinecraftNamespaceBehavesNormally() {
        // Test that minecraft namespace also fails if not loaded
        ResourceRef stoneId = ResourceRef.parse("minecraft:stone");
        
        ResourceValidationResult result1 = validator.validate(ResourceKind.BLOCK, stoneId);
        assertFalse(result1.valid(), "Should fail if 'minecraft' is not added to loaded namespaces");

        // Add 'minecraft' namespace and register stone
        registry.addNamespace("minecraft");
        registry.registerId(ResourceKind.BLOCK, stoneId);

        ResourceValidationResult result2 = validator.validate(ResourceKind.BLOCK, stoneId);
        assertTrue(result2.valid(), "Should pass now that 'minecraft' namespace is loaded and stone is registered");
    }

    @Test
    public void testNullChecks() {
        assertThrows(NullPointerException.class, () -> new ResourceValidator(null));
        assertThrows(NullPointerException.class, () -> validator.validate(null, ResourceRef.parse("minecraft:stone")));
        assertThrows(NullPointerException.class, () -> validator.validate(ResourceKind.BLOCK, null));
    }
}
