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
 * [PermissionId] 与 [Permission] 唯一对应.
 */
@Serializable
@ExperimentalPermission
public data class PermissionId(
    public val namespace: String,
    public val id: String
) {
    init {
        require(!namespace.contains(':')) {
            "':' is not allowed in namespace"
        }
        require(!id.contains(':')) {
            "':' is not allowed in id"
        }
    }

    public override fun toString(): String {
        return "$namespace:$id"
    }

    public companion object {
        public fun parseFromString(string: String): PermissionId =
            string.split(':').let { (namespace, id) -> PermissionId(namespace, id) }
    }
}

