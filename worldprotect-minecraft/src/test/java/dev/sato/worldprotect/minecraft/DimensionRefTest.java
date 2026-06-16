package dev.sato.worldprotect.minecraft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class DimensionRefTest {

    @Test
    public void testValidDimensionRefs() {
        DimensionRef overworld = new DimensionRef(ResourceRef.parse("minecraft:overworld"));
        assertEquals("minecraft:overworld", overworld.asString());
        assertEquals(ResourceRef.parse("minecraft:overworld"), overworld.key());

        DimensionRef nether = new DimensionRef(ResourceRef.parse("minecraft:the_nether"));
        assertEquals("minecraft:the_nether", nether.asString());

        DimensionRef end = new DimensionRef(ResourceRef.parse("minecraft:the_end"));
        assertEquals("minecraft:the_end", end.asString());

        DimensionRef twilight = new DimensionRef(ResourceRef.parse("twilightforest:twilight_forest"));
        assertEquals("twilightforest:twilight_forest", twilight.asString());
    }

    @Test
    public void testInvalidDimensionRefs() {
        // Null checks
        assertThrows(NullPointerException.class, () -> new DimensionRef(null));

        // Invalid format from ResourceRef parsing should raise exception
        assertThrows(IllegalArgumentException.class, () -> new DimensionRef(ResourceRef.parse("minecraft:Overworld")));
        assertThrows(IllegalArgumentException.class, () -> new DimensionRef(ResourceRef.parse("minecraft:overworld:extra")));
        assertThrows(IllegalArgumentException.class, () -> new DimensionRef(ResourceRef.parse("")));
    }
}
