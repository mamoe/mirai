/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.setting

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

internal val KProperty<*>.serialNameOrPropertyName: String get() = this.findAnnotation<SerialName>()?.value ?: this.name

internal fun Int.isOdd() = this and 0b1 != 0

internal inline fun <E> KSerializer<E>.bind(
    crossinline setter: (E) -> Unit,
    crossinline getter: () -> E
): KSerializer<E> {
    return object : KSerializer<E> {
        override val descriptor: SerialDescriptor get() = this@bind.descriptor
        override fun deserialize(decoder: Decoder): E = this@bind.deserialize(decoder).also { setter(it) }

        @Suppress("UNCHECKED_CAST")
        override fun serialize(encoder: Encoder, value: E) =
            this@bind.serialize(encoder, getter())
    }
}

internal inline fun <E, R> KSerializer<E>.map(
    crossinline serializer: (R) -> E,
    crossinline deserializer: (E) -> R
): KSerializer<R> {
    return object : KSerializer<R> {
        override val descriptor: SerialDescriptor get() = this@map.descriptor
        override fun deserialize(decoder: Decoder): R = this@map.deserialize(decoder).let(deserializer)
        override fun serialize(encoder: Encoder, value: R) = this@map.serialize(encoder, value.let(serializer))
    }
}
