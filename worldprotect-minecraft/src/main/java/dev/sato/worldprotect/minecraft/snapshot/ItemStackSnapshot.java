package dev.sato.worldprotect.minecraft.snapshot;

import dev.sato.worldprotect.minecraft.ResourceRef;
import java.util.Objects;

/**
 * Immutable snapshot of an item stack.
 */
public final class ItemStackSnapshot {
    private final ResourceRef itemId;
    private final int count;
    private final NbtSnapshot nbt;

    public ItemStackSnapshot(ResourceRef itemId, int count, NbtSnapshot nbt) {
        this.itemId = Objects.requireNonNull(itemId, "itemId must not be null");
        this.count = count;
        this.nbt = Objects.requireNonNull(nbt, "nbt must not be null");
    }

    public ResourceRef getItemId() {
        return itemId;
    }

    public int getCount() {
        return count;
    }

    public NbtSnapshot getNbt() {
        return nbt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStackSnapshot that = (ItemStackSnapshot) o;
        return count == that.count && itemId.equals(that.itemId) && nbt.equals(that.nbt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, count, nbt);
    }

    @Override
    public String toString() {
        return "ItemStackSnapshot{item=" + itemId + ", count=" + count + ", hasNbt=" + !nbt.isEmpty() + "}";
    }
}
