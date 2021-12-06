/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.permission

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.*
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
    @ResolveContext(PERMISSION_NAMESPACE) public val namespace: String,
    @ResolveContext(PERMISSION_NAME) public val name: String,
) {
    init {
        checkPermissionIdName(name)
        checkPermissionIdName(namespace)
    }

    public object PermissionIdAsStringSerializer : KSerializer<PermissionId> by String.serializer().map(
        serializer = { it.namespace + ":" + it.name },
        deserializer = ::parseFromString
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
        public fun parseFromString(@ResolveContext(PERMISSION_ID) string: String): PermissionId {
            return kotlin.runCatching {
                string.split(':').let { (namespace, id) -> PermissionId(namespace, id) }
            }.getOrElse {
                throw IllegalArgumentException("Could not parse PermissionId from '$string'", it)
            }
        }

        /**
         * 检查 [PermissionId.name] 的合法性. 在非法时抛出 [IllegalArgumentException]
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun checkPermissionIdName(@ResolveContext(PERMISSION_NAME) name: String) {
            when {
                name.isBlank() -> throw IllegalArgumentException("PermissionId.name should not be blank.")
                name.any(Char::isWhitespace) -> throw IllegalArgumentException("Spaces are not yet allowed in PermissionId.name.")
                name.contains(':') -> throw IllegalArgumentException("':' is forbidden in PermissionId.name.")
            }
        }

        /**
         * 检查 [PermissionId.namespace] 的合法性. 在非法时抛出 [IllegalArgumentException]
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public fun checkPermissionIdNamespace(@ResolveContext(PERMISSION_NAME) namespace: String) {
            when {
                namespace.isBlank() -> throw IllegalArgumentException("PermissionId.namespace should not be blank.")
                namespace.any(Char::isWhitespace) -> throw IllegalArgumentException("Spaces are not yet allowed in PermissionId.namespace.")
                namespace.contains(':') -> throw IllegalArgumentException("':' is forbidden in PermissionId.namespace.")
            }
        }
    }
}

