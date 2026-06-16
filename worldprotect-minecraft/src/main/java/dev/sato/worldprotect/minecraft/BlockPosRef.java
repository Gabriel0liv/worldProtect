package dev.sato.worldprotect.minecraft;

/**
 * Immutable block coordinate reference representing a block position in space.
 */
public final class BlockPosRef {
    private final int x;
    private final int y;
    private final int z;

    public BlockPosRef(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPosRef that = (BlockPosRef) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(y);
        result = 31 * result + Integer.hashCode(z);
        return result;
    }

    @Override
    public String toString() {
        return "BlockPosRef{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
