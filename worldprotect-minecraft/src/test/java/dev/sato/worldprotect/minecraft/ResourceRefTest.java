package dev.sato.worldprotect.minecraft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class ResourceRefTest {

    @Test
    public void testValidIdentifiers() {
        // Namespace and path specified
        ResourceRef ref1 = ResourceRef.parse("minecraft:stone");
        assertEquals("minecraft", ref1.namespace());
        assertEquals("stone", ref1.path());
        assertEquals("minecraft:stone", ref1.asString());

        // Default namespace
        ResourceRef ref2 = ResourceRef.parse("stone");
        assertEquals("minecraft", ref2.namespace());
        assertEquals("stone", ref2.path());

        // Under-score and dash
        ResourceRef ref3 = ResourceRef.parse("create:mechanical_press");
        assertEquals("create", ref3.namespace());
        assertEquals("mechanical_press", ref3.path());

        // Subdirectories / slashes in path
        ResourceRef ref4 = ResourceRef.parse("c:ingots/iron");
        assertEquals("c", ref4.namespace());
        assertEquals("ingots/iron", ref4.path());

        ResourceRef ref5 = ResourceRef.parse("forge:storage_blocks/iron");
        assertEquals("forge", ref5.namespace());
        assertEquals("storage_blocks/iron", ref5.path());
        
        // Constructor & of() factory validation check
        ResourceRef refOf = ResourceRef.of("sophisticatedstorage", "gold_chest");
        assertEquals("sophisticatedstorage", refOf.namespace());
        assertEquals("gold_chest", refOf.path());
    }

    @Test
    public void testInvalidIdentifiers() {
        // Empty inputs
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse(""));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.of("", "stone"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.of("minecraft", ""));

        // Empty namespace or path parts
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse(":stone"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("minecraft:"));

        // Uppercase letters
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("Minecraft:stone"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("minecraft:Stone"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.of("Minecraft", "stone"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.of("minecraft", "Stone"));

        // Whitespace and invalid characters
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("bad namespace:stone"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("minecraft:bad path"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("minecraft:bad#path"));
        assertThrows(IllegalArgumentException.class, () -> ResourceRef.parse("minecraft::stone"));
    }
}
