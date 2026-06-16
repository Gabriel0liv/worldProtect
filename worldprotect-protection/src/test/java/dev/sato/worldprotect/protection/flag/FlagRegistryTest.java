package dev.sato.worldprotect.protection.flag;

import org.junit.jupiter.api.Test;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public final class FlagRegistryTest {

    @Test
    public void testBuiltInsAreRegistered() {
        FlagRegistry registry = FlagRegistry.withBuiltIns();
        assertTrue(registry.exists(BuiltInFlags.BUILD_KEY));
        assertTrue(registry.exists(BuiltInFlags.BREAK_BLOCK_KEY));
        assertTrue(registry.exists(BuiltInFlags.PLACE_BLOCK_KEY));
        assertTrue(registry.exists(BuiltInFlags.WORLD_MODIFY_KEY));

        FlagDefinition def = registry.get(BuiltInFlags.BUILD_KEY).orElse(null);
        assertNotNull(def);
        assertEquals(FlagState.PASS, def.defaultState());
    }

    @Test
    public void testDuplicateRegistrationThrows() {
        FlagRegistry registry = new FlagRegistry();
        FlagDefinition def1 = FlagDefinition.of(FlagKey.of("custom-flag"), "desc", FlagState.PASS);
        FlagDefinition def2 = FlagDefinition.of(FlagKey.of("custom-flag"), "desc 2", FlagState.ALLOW);

        registry.register(def1);
        assertThrows(IllegalArgumentException.class, () -> registry.register(def2));
    }

    @Test
    public void testDefinitionsCollectionIsImmutable() {
        FlagRegistry registry = FlagRegistry.withBuiltIns();
        Collection<FlagDefinition> definitions = registry.definitions();

        assertThrows(UnsupportedOperationException.class, () -> definitions.add(
            FlagDefinition.of(FlagKey.of("new-flag"), "desc", FlagState.PASS)
        ));
    }
}
