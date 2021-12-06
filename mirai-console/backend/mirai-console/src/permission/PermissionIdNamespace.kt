/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.command.Command
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.PERMISSION_NAME

/**
 * [PermissionId] 的命名空间. 用于提供 [PermissionId.namespace].
 */
public interface PermissionIdNamespace {
    /**
     * 创建一个此命名空间下的 [PermitteeId].
     *
     * 在指令初始化时, 会申请对应权限. 此时 [name] 为 `command.$primaryName` 其中 [primaryName][Command.primaryName].
     */
    public fun permissionId(@ResolveContext(PERMISSION_NAME) name: String): PermissionId
}