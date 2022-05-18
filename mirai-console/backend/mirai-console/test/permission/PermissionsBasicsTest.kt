/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.permission

import kotlin.test.Test
import kotlin.test.assertFails

internal class PermissionsBasicsTest {
    @Suppress("ILLEGAL_PERMISSION_NAMESPACE", "ILLEGAL_PERMISSION_NAME") // inspection by mirai-console-intellij
    @Test
    fun testInvalidPermissionId() {
        assertFails { PermissionId("space namespace", "name") }
        assertFails { PermissionId("namespace", "space name") }
        // assertFails { PermissionId("", "name") }
        // assertFails { PermissionId("namespace", "") }
        assertFails { PermissionId("namespace:name", "name") }
        assertFails { PermissionId("namespace", "namespace:name") }
    }
}