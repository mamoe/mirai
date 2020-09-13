/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.internal.data.map


/**
 * 表示一个 [权限][Permission] 的唯一 ID.
 *
 * [PermissionId] 与 [Permission] 唯一对应.
 *
 * ### 字符串表示
 * `"$namespace:$name"`. 如 "console:command.stop", "*:*"
 */
@Serializable(with = PermissionId.PermissionIdAsStringSerializer::class)
public data class PermissionId(
    public val namespace: String,
    public val name: String,
) {
    init {
        require(!namespace.contains(':')) {
            "':' is not allowed in namespace"
        }
        require(!name.contains(':')) {
            "':' is not allowed in id"
        }
    }

    public object PermissionIdAsStringSerializer : KSerializer<PermissionId> by String.serializer().map(
        serializer = { it.namespace + ":" + it.name },
        deserializer = { it.split(':').let { (namespace, id) -> PermissionId(namespace, id) } }
    )

    /**
     * 返回 `$namespace:$id`
     */
    public override fun toString(): String = "$namespace:$name"

    public companion object {
        /**
         * 由 `$namespace:$id` 解析 [PermissionId].
         *
         * @throws IllegalArgumentException 在解析失败时抛出.
         */
        @JvmStatic
        public fun parseFromString(string: String): PermissionId {
            return kotlin.runCatching {
                string.split(':').let { (namespace, id) -> PermissionId(namespace, id) }
            }.getOrElse {
                throw IllegalArgumentException("Could not parse PermissionId from '$string'", it)
            }
        }
    }
}

