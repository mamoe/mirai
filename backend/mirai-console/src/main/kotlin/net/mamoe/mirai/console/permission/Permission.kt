/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI


/**
 * 一个权限节点.
 *
 * 由 [PermissionService] 实现不同, [Permission] 可能会有多种实例. 但一个权限总是拥有确定的 [id]
 */
@ExperimentalPermission
public interface Permission {
    public val id: PermissionId
    public val description: String
    public val parentId: PermissionId
}

/**
 * 所有权限的父权限.
 */
@ExperimentalPermission
public object AncestorPermission :
    Permission {
    override val id: PermissionId = PermissionId("*", "*")
    override val description: String get() = "The parent of any permission"
    override val parentId: PermissionId get() = id
}

@ConsoleExperimentalAPI
@ExperimentalPermission
public fun Permission.parentsWithSelfSequence(): Sequence<Permission> =
    generateSequence(this) { p ->
        p.parentId.findCorrespondingPermission()?.takeIf { parent -> parent != p }
    }

/**
 * [Permission] 的简单实现
 */
@Serializable
@ExperimentalPermission
public class PermissionImpl(
    override val id: PermissionId,
    override val description: String,
    override val parentId: PermissionId = AncestorPermission.id
) : Permission