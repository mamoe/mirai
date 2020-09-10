/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.util.ConsoleExperimentalAPI


/**
 * 一个权限节点.
 *
 * 由 [PermissionService] 实现不同, [Permission] 可能会有多种实例. 但一个权限总是拥有确定的 [id].
 *
 * 请不要手动实现这个接口. 总是从 [PermissionService.register] 获得实例.
 */
@ExperimentalPermission
public interface Permission {
    public val id: PermissionId
    public val description: String

    /**
     * [RootPermission] 的 parent 为自身
     */
    public val parent: Permission
}

/**
 * 所有权限的父权限.
 */
@get:JvmName("getRootPermission")
@ExperimentalPermission
public val RootPermission: Permission
    get() = PermissionService.INSTANCE.rootPermission

/**
 * 所有内建指令的权限
 */
@ExperimentalPermission
public val RootConsoleBuiltInPermission: Permission by lazy {
    PermissionService.INSTANCE.register(
        PermissionId("console", "*"),
        "The parent of any built-in commands"
    )
}

@ConsoleExperimentalAPI
@ExperimentalPermission
public fun Permission.parentsWithSelfSequence(): Sequence<Permission> =
    generateSequence(this) { p ->
        p.parent.takeIf { parent -> parent != p }
    }