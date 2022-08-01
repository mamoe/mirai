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

        // test cancel fail (by parent)
        val cause1 = assertFails { builtIn.cancel(member, command, false) }
        assertTrue { cause1 is UnsupportedOperationException }
        assertEquals("""
            m12345.6789 的 plugin:command 权限来自
            m12345.* plugin:*
            Mirai Console 内置权限系统目前不支持单独禁用继承得到的权限. 可取消继承来源再为其分别分配.
            
        """.trimIndent(), cause1.message)

        // test recursive cancel
        builtIn.cancel(any, builtIn.rootPermission, true)
        assertFalse { builtIn.testPermission(any, plugin) }
        assertFalse { builtIn.testPermission(member, plugin) }
        assertFalse { builtIn.testPermission(any, command) }
        assertFalse { builtIn.testPermission(member, command) }

        // test cancel (no permit)
        val cause2 = assertFails { builtIn.cancel(member, command, false) }
        assertTrue { cause2 is UnsupportedOperationException }
        assertEquals("${member.asString()} 不拥有权限 ${command.id}", cause2.message)

        // test not recursive cancel
        builtIn.permit(any, plugin)
        builtIn.permit(any, command)
        builtIn.cancel(any, plugin, false)
        assertFalse { builtIn.testPermission(any, plugin) }
        assertFalse { builtIn.testPermission(member, plugin) }
        assertTrue { builtIn.testPermission(any, command) }
        assertTrue { builtIn.testPermission(member, command) }
    }
}