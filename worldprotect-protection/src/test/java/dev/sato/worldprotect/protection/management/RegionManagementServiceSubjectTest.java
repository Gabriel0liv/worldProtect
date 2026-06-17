package dev.sato.worldprotect.protection.management;

import dev.sato.worldprotect.minecraft.BlockPosRef;
import dev.sato.worldprotect.minecraft.DimensionRef;
import dev.sato.worldprotect.minecraft.ResourceRef;
import dev.sato.worldprotect.protection.config.BoundsConfig;
import dev.sato.worldprotect.protection.config.ConfigToDomainMapper;
import dev.sato.worldprotect.protection.config.RegionConfig;
import dev.sato.worldprotect.protection.config.SubjectRefConfig;
import dev.sato.worldprotect.protection.config.WorldProtectConfig;
import dev.sato.worldprotect.protection.region.RegionId;
import dev.sato.worldprotect.protection.subject.RegionRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionManagementServiceSubjectTest {

    private RegionManagementService service;
    private ConfigToDomainMapper mapper;
    private DimensionRef overworld;
    private BoundsConfig bounds;
    private SubjectRefConfig subjectA;
    private SubjectRefConfig subjectB;

    @BeforeEach
    public void setUp() {
        service = RegionManagementService.withBuiltIns();
        mapper = new ConfigToDomainMapper();
        overworld = new DimensionRef(ResourceRef.of("minecraft", "overworld"));
        bounds = BoundsConfig.cuboid(new BlockPosRef(0, 0, 0), new BlockPosRef(10, 10, 10));
        subjectA = SubjectRefConfig.of("player:" + UUID.randomUUID());
        subjectB = SubjectRefConfig.of("player:" + UUID.randomUUID());
    }

    @Test
    public void testAddOwnerAndMemberSucceed() {
        WorldProtectConfig config = baseConfig();

        RegionManagementResult<WorldProtectConfig> ownerResult = service.addSubject(
                config,
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.OWNER, subjectA)
        );
        RegionManagementResult<WorldProtectConfig> memberResult = service.addSubject(
                config,
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.MEMBER, subjectB)
        );

        assertEquals(RegionManagementStatus.SUCCESS, ownerResult.status());
        assertEquals(1, ownerResult.value().regions().get(0).subjectsConfig().owners().size());
        assertEquals(RegionManagementStatus.SUCCESS, memberResult.status());
        assertEquals(1, memberResult.value().regions().get(0).subjectsConfig().members().size());
    }

    @Test
    public void testAddingSameMemberReturnsNoChange() {
        RegionConfig region = RegionConfig.of(
                RegionId.of("spawn"),
                overworld,
                100,
                bounds,
                Map.of(),
                dev.sato.worldprotect.protection.config.RegionSubjectsConfig.of(List.of(), List.of(subjectA)),
                dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig.defaults()
        );
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        RegionManagementResult<WorldProtectConfig> result = service.addSubject(
                config,
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.MEMBER, subjectA)
        );

        assertEquals(RegionManagementStatus.NO_CHANGE, result.status());
    }

    @Test
    public void testOwnerOverridesMemberAndAddingMemberWhenOwnerReturnsNoChange() {
        WorldProtectConfig config = baseConfig();

        RegionManagementResult<WorldProtectConfig> ownerResult = service.addSubject(
                config,
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.OWNER, subjectA)
        );
        WorldProtectConfig memberConfig = WorldProtectConfig.of(List.of(
                RegionConfig.of(
                        RegionId.of("spawn"),
                        overworld,
                        100,
                        bounds,
                        Map.of(),
                        dev.sato.worldprotect.protection.config.RegionSubjectsConfig.of(List.of(), List.of(subjectA)),
                        dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig.defaults()
                )
        ));
        RegionManagementResult<WorldProtectConfig> promoteResult = service.addSubject(
                memberConfig,
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.OWNER, subjectA)
        );
        RegionManagementResult<WorldProtectConfig> memberNoChange = service.addSubject(
                ownerResult.value(),
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.MEMBER, subjectA)
        );

        assertEquals(RegionManagementStatus.SUCCESS, promoteResult.status());
        assertTrue(promoteResult.value().regions().get(0).subjectsConfig().members().isEmpty());
        assertEquals(RegionManagementStatus.NO_CHANGE, memberNoChange.status());
    }

    @Test
    public void testRemoveOwnerMemberAndMissingSubjectBehavior() {
        RegionConfig region = RegionConfig.of(
                RegionId.of("spawn"),
                overworld,
                100,
                bounds,
                Map.of(),
                dev.sato.worldprotect.protection.config.RegionSubjectsConfig.of(List.of(subjectA), List.of(subjectB)),
                dev.sato.worldprotect.protection.config.RegionAccessPolicyConfig.defaults()
        );
        WorldProtectConfig config = WorldProtectConfig.of(List.of(region));

        RegionManagementResult<WorldProtectConfig> removeOwner = service.removeSubject(
                config,
                RemoveRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.OWNER, subjectA)
        );
        RegionManagementResult<WorldProtectConfig> removeMember = service.removeSubject(
                config,
                RemoveRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.MEMBER, subjectB)
        );
        RegionManagementResult<WorldProtectConfig> removeMissing = service.removeSubject(
                config,
                RemoveRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.MEMBER, SubjectRefConfig.of("player:" + UUID.randomUUID()))
        );

        assertEquals(RegionManagementStatus.SUCCESS, removeOwner.status());
        assertTrue(removeOwner.value().regions().get(0).subjectsConfig().owners().isEmpty());
        assertEquals(RegionManagementStatus.SUCCESS, removeMember.status());
        assertTrue(removeMember.value().regions().get(0).subjectsConfig().members().isEmpty());
        assertEquals(RegionManagementStatus.NO_CHANGE, removeMissing.status());
    }

    @Test
    public void testInvalidSubjectFailsAndMappingPreservesEffectiveSubjects() {
        RegionManagementResult<WorldProtectConfig> invalid = service.addSubject(
                baseConfig(),
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.OWNER, SubjectRefConfig.of("player:not-a-uuid"))
        );
        RegionManagementResult<WorldProtectConfig> valid = service.addSubject(
                baseConfig(),
                AddRegionSubjectRequest.of(RegionId.of("spawn"), RegionRole.OWNER, subjectA)
        );

        assertEquals(RegionManagementStatus.VALIDATION_FAILED, invalid.status());
        assertEquals(1, mapper.toRegionSet(valid.value()).regions().get(0).subjects().owners().size());
    }

    private WorldProtectConfig baseConfig() {
        return WorldProtectConfig.of(List.of(
                RegionConfig.of(RegionId.of("spawn"), overworld, 100, bounds, Map.of())
        ));
    }
}
