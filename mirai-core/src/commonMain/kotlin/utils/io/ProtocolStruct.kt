/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.io

import kotlin.reflect.KClass

internal interface ProtocolStruct
internal interface ProtoBuf : ProtocolStruct
internal interface JceStruct : ProtocolStruct

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
internal annotation class NestedStructure(
    val serializer: KClass<out NestedStructureDesensitizer<*, *>>
)

internal interface NestedStructureDesensitizer<in C : ProtocolStruct, T> {
    fun deserialize(context: C, byteArray: ByteArray): T?
}