package dev.sato.worldprotect.minecraft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public final class BlockPosRefTest {

    @Test
    public void testCoordinates() {
        BlockPosRef pos = new BlockPosRef(10, 64, -20);
        assertEquals(10, pos.x());
        assertEquals(64, pos.y());
        assertEquals(-20, pos.z());
        
        // Legacy getters
        assertEquals(10, pos.getX());
        assertEquals(64, pos.getY());
        assertEquals(-20, pos.getZ());
    }

    @Test
    public void testEquality() {
        BlockPosRef pos1 = new BlockPosRef(5, 5, 5);
        BlockPosRef pos2 = new BlockPosRef(5, 5, 5);
        BlockPosRef pos3 = new BlockPosRef(-5, 5, 5);

        assertEquals(pos1, pos2);
        assertNotEquals(pos1, pos3);
        assertEquals(pos1.hashCode(), pos2.hashCode());
        assertNotEquals(pos1.hashCode(), pos3.hashCode());
    }

    @Test
    public void testBoundaries() {
        BlockPosRef posMin = new BlockPosRef(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, posMin.x());
        assertEquals(Integer.MIN_VALUE, posMin.y());
        assertEquals(Integer.MIN_VALUE, posMin.z());

        BlockPosRef posMax = new BlockPosRef(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, posMax.x());
        assertEquals(Integer.MAX_VALUE, posMax.y());
        assertEquals(Integer.MAX_VALUE, posMax.z());
    }
}
