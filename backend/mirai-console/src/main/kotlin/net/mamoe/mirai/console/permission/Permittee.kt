/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.command.CommandSender

/**
 * 可被赋予权限的对象, 即 '被许可人'.
 *
 * 被许可人自身不持有拥有的权限列表, 而是拥有 [PermitteeId], 标识自己的身份, 供 [权限服务][PermissionService] 处理.
 *
 * **注意**: 请不要自主实现 [Permittee]
 *
 * @see CommandSender
 */
@PermissionImplementation
public interface Permittee {
    public val permitteeId: PermitteeId
}