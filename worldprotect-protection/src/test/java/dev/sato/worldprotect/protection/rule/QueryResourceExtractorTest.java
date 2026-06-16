package dev.sato.worldprotect.protection.rule;

import dev.sato.worldprotect.core.actor.Actor;
import dev.sato.worldprotect.core.actor.ActorType;
import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.query.CauseChain;
import dev.sato.worldprotect.protection.query.ProtectionAction;
import dev.sato.worldprotect.protection.query.ProtectionCause;
import dev.sato.worldprotect.protection.query.ProtectionQuery;
import dev.sato.worldprotect.protection.query.ProtectionTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

public final class QueryResourceExtractorTest {

    private Actor actor;
    private DimensionRef overworld;
    private BlockPosRef pos;

    @BeforeEach
    public void setUp() {
        actor = new Actor("player1", ActorType.PLAYER);
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        pos = new BlockPosRef(5, 5, 5);
    }

    @Test
    public void testBlockBreakExtractsTargetBlock() {
        ProtectionTarget target = ProtectionTarget.block(ResourceRef.of("minecraft", "stone"), overworld, pos);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BLOCK_BREAK, CauseChain.of(ProtectionCause.player()), target, overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isPresent());
        assertEquals(ResourceRef.of("minecraft", "stone"), resource.get());
    }

    @Test
    public void testItemUseExtractsDirectItemCause() {
        ProtectionCause itemCause = ProtectionCause.item(ResourceRef.of("create", "wrench"));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE, CauseChain.of(itemCause), ProtectionTarget.unknown(), overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isPresent());
        assertEquals(ResourceRef.of("create", "wrench"), resource.get());
    }

    @Test
    public void testItemUseOnBlockExtractsDirectItemCauseFirst() {
        ProtectionCause itemCause = ProtectionCause.item(ResourceRef.of("create", "wrench"));
        ProtectionTarget target = ProtectionTarget.block(ResourceRef.of("minecraft", "furnace"), overworld, pos);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.ITEM_USE_ON_BLOCK, CauseChain.of(itemCause), target, overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isPresent());
        assertEquals(ResourceRef.of("create", "wrench"), resource.get());
    }

    @Test
    public void testContainerOpenExtractsContainerTarget() {
        ProtectionTarget target = ProtectionTarget.container(ResourceRef.of("minecraft", "chest"), overworld, pos);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.CONTAINER_OPEN, CauseChain.of(ProtectionCause.player()), target, overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isPresent());
        assertEquals(ResourceRef.of("minecraft", "chest"), resource.get());
    }

    @Test
    public void testExplosionBlockDamageExtractsTargetBlock() {
        ProtectionTarget target = ProtectionTarget.block(ResourceRef.of("minecraft", "stone"), overworld, pos);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.EXPLOSION_BLOCK_DAMAGE, CauseChain.of(ProtectionCause.explosion(ResourceRef.of("minecraft", "tnt"))), target, overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isPresent());
        assertEquals(ResourceRef.of("minecraft", "stone"), resource.get());
    }

    @Test
    public void testExplosionItemDropExtractsDropItem() {
        ProtectionTarget target = ProtectionTarget.drop(ResourceRef.of("minecraft", "diamond"), overworld, pos);
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.EXPLOSION_ITEM_DROP, CauseChain.of(ProtectionCause.explosion(ResourceRef.of("minecraft", "tnt"))), target, overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isPresent());
        assertEquals(ResourceRef.of("minecraft", "diamond"), resource.get());
    }

    @Test
    public void testWorldModifyFallsBackToDirectCauseSource() {
        // Target has no ID (unknown)
        ProtectionTarget target = ProtectionTarget.unknown();
        // Cause has sourceId (blockEntity)
        ProtectionCause blockEntityCause = ProtectionCause.blockEntity(ResourceRef.of("create", "mechanical_press"));
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.WORLD_MODIFY, CauseChain.of(blockEntityCause), target, overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isPresent());
        assertEquals(ResourceRef.of("create", "mechanical_press"), resource.get());
    }

    @Test
    public void testNoResourceReturnsEmpty() {
        // Unknown query with unknown target
        ProtectionQuery query = new ProtectionQuery(actor, ProtectionAction.BUILD, CauseChain.of(ProtectionCause.player()), ProtectionTarget.unknown(), overworld, pos);

        Optional<ResourceRef> resource = QueryResourceExtractor.primaryResource(query);
        assertTrue(resource.isEmpty());
    }
}
