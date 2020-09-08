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


/**
 * 一个权限节点
 */
@ExperimentalPermission
public interface Permission {
    public val id: PermissionId
    public val description: String
    public val base: PermissionId?
}

/**
 * [Permission] 的简单实现
 */
@Serializable
@ExperimentalPermission
public class PermissionImpl(
    override val id: PermissionId,
    override val description: String,
    override val base: PermissionId?
) : Permission