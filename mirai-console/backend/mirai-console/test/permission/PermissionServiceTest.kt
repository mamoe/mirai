/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.internal.permission.BuiltInPermissionService
import net.mamoe.mirai.console.internal.permission.PermissionImpl
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class PermissionServiceTest {

    @Test
    fun `test built in`() {
        val builtIn: PermissionService<PermissionImpl> = BuiltInPermissionService()
        assertEquals(PermissionId.parseFromString("*:*"), builtIn.rootPermission.id)

        val plugin = builtIn.register(PermissionId.parseFromString("plugin:*"), "", builtIn.rootPermission)
        assertEquals(builtIn[PermissionId.parseFromString("plugin:*")], plugin)

        val command = builtIn.register(PermissionId.parseFromString("plugin:command"), "", plugin)
        assertEquals(builtIn[PermissionId.parseFromString("plugin:command")], command)

        val any = AbstractPermitteeId.parseFromString("m12345.*")
        val member =  AbstractPermitteeId.parseFromString("m12345.6789")

        assertFalse { builtIn.testPermission(any, plugin) }

        // test permit
        builtIn.permit(any, plugin)
        assertTrue { builtIn.testPermission(any, plugin) }
        assertTrue { builtIn.testPermission(member, plugin) }
        assertTrue { builtIn.testPermission(any, command) }
        assertTrue { builtIn.testPermission(member, command) }

        assertFails { builtIn.cancel(member, command, false) }

        // test recursive cancel
        builtIn.cancel(any, builtIn.rootPermission, true)
        assertFalse { builtIn.testPermission(any, plugin) }
        assertFalse { builtIn.testPermission(member, plugin) }
        assertFalse { builtIn.testPermission(any, command) }
        assertFalse { builtIn.testPermission(member, command) }

        // test not recursive cancel
        builtIn.permit(any, plugin)
        builtIn.permit(any, command)
        builtIn.cancel(any, plugin, false)
        assertFalse { builtIn.testPermission(any, plugin) }
        assertFalse { builtIn.testPermission(member, plugin) }
        assertTrue { builtIn.testPermission(any, command) }
        assertTrue { builtIn.testPermission(member, command) }

        assertFails { builtIn.cancel(member, command, false) }
    }
}