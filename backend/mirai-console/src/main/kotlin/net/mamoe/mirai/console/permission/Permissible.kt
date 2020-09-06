/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.console.permission

@ExperimentalPermission
public interface Permissible {
    public val identifier: String
}

@ExperimentalPermission
public inline fun Permissible.hasPermission(permission: Permission): Boolean =
    PermissionService.run { permission.testPermission(this@hasPermission) }

@ExperimentalPermission
public inline fun Permissible.hasPermission(permission: PermissionIdentifier): Boolean =
    PermissionService.run { permission.testPermission(this@hasPermission) }
