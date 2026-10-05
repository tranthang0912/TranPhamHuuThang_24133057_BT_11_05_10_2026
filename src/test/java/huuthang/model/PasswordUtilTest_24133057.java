package huuthang.model;

import huuthang.util.PasswordUtil_24133057;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordUtilTest_24133057 {
    @Test
    void createsHashThatFitsNvarchar50() {
        String hash = PasswordUtil_24133057.hash("Admin@123");
        assertEquals("6G94qKPK8LYNjnTllCqm2G3BUM08AzOK7yW30tfjrMc=", hash);
        assertTrue(hash.length() <= 50);
    }

    @Test
    void matchesOnlyTheCorrectPassword() {
        String hash = PasswordUtil_24133057.hash("User@123");
        assertTrue(PasswordUtil_24133057.matches("User@123", hash));
        assertFalse(PasswordUtil_24133057.matches("wrong", hash));
    }
}
