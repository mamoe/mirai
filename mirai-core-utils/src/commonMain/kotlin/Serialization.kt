/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("SerializationKt_common")

package net.mamoe.mirai.utils

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmName

public fun SerialDescriptor.copy(newName: String): SerialDescriptor =
    buildClassSerialDescriptor(newName) { takeElementsFrom(this@copy) }

@OptIn(ExperimentalSerializationApi::class) // bad but there is no other solution
public fun ClassSerialDescriptorBuilder.takeElementsFrom(descriptor: SerialDescriptor) {
    with(descriptor) {
        repeat(descriptor.elementsCount) { index ->
            element(
                elementName = getElementName(index),
                descriptor = getElementDescriptor(index),
                annotations = getElementAnnotations(index),
                isOptional = isElementOptional(index),
            )
        }
    }
}

public inline fun <T, R> KSerializer<T>.map(
    resultantDescriptor: SerialDescriptor,
    crossinline deserialize: T.(T) -> R,
    crossinline serialize: R.(R) -> T,
): KSerializer<R> {
    return object : KSerializer<R> {
        override val descriptor: SerialDescriptor get() = resultantDescriptor
        override fun deserialize(decoder: Decoder): R = this@map.deserialize(decoder).let { deserialize(it, it) }
        override fun serialize(encoder: Encoder, value: R) = serialize(encoder, value.let { serialize(it, it) })
    }
}

@OptIn(ExperimentalSerializationApi::class)
public inline fun <T, R> KSerializer<T>.mapPrimitive(
    serialName: String,
    crossinline deserialize: (T) -> R,
    crossinline serialize: R.(R) -> T,
): KSerializer<R> {
    val kind = this@mapPrimitive.descriptor.kind
    check(kind is PrimitiveKind) { "kind must be PrimitiveKind but found $kind" }
    return object : KSerializer<R> {
        override fun deserialize(decoder: Decoder): R =
            this@mapPrimitive.deserialize(decoder).let(deserialize)

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, kind)
        override fun serialize(encoder: Encoder, value: R) =
            this@mapPrimitive.serialize(encoder, value.let { serialize(it, it) })
    }
}


public fun <T> MiraiFile.loadNotBlankAs(
    serializer: DeserializationStrategy<T>,
    stringFormat: StringFormat,
): T? {
    if (!this.exists() || this.length == 0L) {
        return null
    }
    return try {
        stringFormat.decodeFromString(serializer, this.readText())
    } catch (e: Throwable) { //broken file
        e.printStackTrace()
        null
    }
}

public fun <T> MiraiFile.loadNotBlankAs(
    serializer: DeserializationStrategy<T>,
    binaryFormat: BinaryFormat,
): T? {
    if (!this.exists() || this.length == 0L) {
        return null
    }
    return try {
        binaryFormat.decodeFromByteArray(serializer, this.readBytes())
    } catch (e: Throwable) { //broken file
        e.printStackTrace()
        null
    }
}
