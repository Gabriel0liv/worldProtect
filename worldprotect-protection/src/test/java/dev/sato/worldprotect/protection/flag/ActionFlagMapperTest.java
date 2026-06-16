package dev.sato.worldprotect.protection.flag;

import dev.sato.worldprotect.protection.query.ProtectionAction;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public final class ActionFlagMapperTest {

    @Test
    public void testBlockBreakMapping() {
        List<FlagKey> flags = ActionFlagMapper.mapAction(ProtectionAction.BLOCK_BREAK);
        assertEquals(2, flags.size());
        assertEquals(BuiltInFlags.BREAK_BLOCK_KEY, flags.get(0));
        assertEquals(BuiltInFlags.BUILD_KEY, flags.get(1));
    }

    @Test
    public void testBlockPlaceMapping() {
        List<FlagKey> flags = ActionFlagMapper.mapAction(ProtectionAction.BLOCK_PLACE);
        assertEquals(2, flags.size());
        assertEquals(BuiltInFlags.PLACE_BLOCK_KEY, flags.get(0));
        assertEquals(BuiltInFlags.BUILD_KEY, flags.get(1));
    }

    @Test
    public void testItemUseOnBlockMapping() {
        List<FlagKey> flags = ActionFlagMapper.mapAction(ProtectionAction.ITEM_USE_ON_BLOCK);
        assertEquals(2, flags.size());
        assertEquals(BuiltInFlags.USE_ITEM_ON_BLOCK_KEY, flags.get(0));
        assertEquals(BuiltInFlags.USE_ITEM_KEY, flags.get(1));
    }

    @Test
    public void testExplosionBlockDamageMapping() {
        List<FlagKey> flags = ActionFlagMapper.mapAction(ProtectionAction.EXPLOSION_BLOCK_DAMAGE);
        assertEquals(1, flags.size());
        assertEquals(BuiltInFlags.EXPLOSION_BREAK_BLOCKS_KEY, flags.get(0));
    }

    @Test
    public void testExplosionItemDropMapping() {
        List<FlagKey> flags = ActionFlagMapper.mapAction(ProtectionAction.EXPLOSION_ITEM_DROP);
        assertEquals(1, flags.size());
        assertEquals(BuiltInFlags.EXPLOSION_DROP_ITEMS_KEY, flags.get(0));
    }

    @Test
    public void testWorldModifyMapping() {
        List<FlagKey> flags = ActionFlagMapper.mapAction(ProtectionAction.WORLD_MODIFY);
        assertEquals(1, flags.size());
        assertEquals(BuiltInFlags.WORLD_MODIFY_KEY, flags.get(0));
    }

    @Test
    public void testBuildMapping() {
        List<FlagKey> flags = ActionFlagMapper.mapAction(ProtectionAction.BUILD);
        assertEquals(1, flags.size());
        assertEquals(BuiltInFlags.BUILD_KEY, flags.get(0));
    }
}
