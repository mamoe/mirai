/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")

package net.mamoe.mirai.console.permission

/**
 * @see PermissionService.register
 */
public class PermissionRegistryConflictException(
    public val newInstance: Permission,
    public val existingInstance: Permission,
) : Exception("Conflicting Permission registry. new: $newInstance, existing: $existingInstance")