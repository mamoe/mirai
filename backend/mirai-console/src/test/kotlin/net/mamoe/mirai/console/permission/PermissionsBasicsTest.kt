package net.mamoe.mirai.console.permission

import org.junit.jupiter.api.Test
import kotlin.test.assertFails

internal class PermissionsBasicsTest {
    @Test
    fun testInvalidPermissionId() {
        assertFails { PermissionId("space namespace", "name") }
        assertFails { PermissionId("namespace", "space name") }
        // assertFails { PermissionId("", "name") }
        // assertFails { PermissionId("namespace", "") }
        assertFails { PermissionId("namespace:name", "name") }
        assertFails { PermissionId("namespace", "namespace:name") }
    }

    @Test
    fun parentsWithSelfSequence() {

    }
}