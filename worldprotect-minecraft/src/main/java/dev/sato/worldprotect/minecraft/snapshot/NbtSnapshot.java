package dev.sato.worldprotect.minecraft.snapshot;

import java.util.Arrays;
import java.util.Objects;

/**
 * Platform-independent serialized raw representation of NBT data.
 */
public final class NbtSnapshot {
    public static final NbtSnapshot EMPTY = new NbtSnapshot(new byte[0]);

    private final byte[] rawData;

    public NbtSnapshot(byte[] rawData) {
        this.rawData = Objects.requireNonNull(rawData, "rawData must not be null").clone();
    }

    public byte[] getRawData() {
        return rawData.clone();
    }

    public boolean isEmpty() {
        return rawData.length == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NbtSnapshot that = (NbtSnapshot) o;
        return Arrays.equals(rawData, that.rawData);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(rawData);
    }

    @Override
    public String toString() {
        return "NbtSnapshot{size=" + rawData.length + " bytes}";
    }
}
