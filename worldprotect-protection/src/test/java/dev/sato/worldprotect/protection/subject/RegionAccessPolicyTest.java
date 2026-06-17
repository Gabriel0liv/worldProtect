package dev.sato.worldprotect.protection.subject;

import dev.sato.worldprotect.protection.flag.FlagKey;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public final class RegionAccessPolicyTest {

    @Test
    public void testDefaults() {
        RegionAccessPolicy policy = RegionAccessPolicy.defaults();
        assertTrue(policy.ownersBypassFlags());
        assertFalse(policy.membersBypassFlags());
        assertTrue(policy.ownerBypasses(FlagKey.of("build")));
        assertFalse(policy.memberBypasses(FlagKey.of("build")));
    }

    @Test
    public void testSpecificBypasses() {
        FlagKey build = FlagKey.of("build");
        FlagKey place = FlagKey.of("place");

        RegionAccessPolicy policy = RegionAccessPolicy.of(
                false,
                false,
                Set.of(build),
                Set.of(place)
        );

        assertFalse(policy.ownersBypassFlags());
        assertFalse(policy.membersBypassFlags());

        assertTrue(policy.ownerBypasses(build));
        assertFalse(policy.ownerBypasses(place));

        assertFalse(policy.memberBypasses(build));
        assertTrue(policy.memberBypasses(place));
    }
}
