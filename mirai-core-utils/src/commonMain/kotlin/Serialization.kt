/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")


package net.mamoe.mirai.utils

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.descriptors.ClassSerialDescriptorBuilder
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import java.io.File

public fun <T> File.loadNotBlankAs(
    serializer: DeserializationStrategy<T>,
    stringFormat: StringFormat,
): T? {
    if (!this.exists() || this.length() == 0L) {
        return null
    }
    return stringFormat.decodeFromString(serializer, this.readText())
}

public fun <T> File.loadNotBlankAs(
    serializer: DeserializationStrategy<T>,
    binaryFormat: BinaryFormat,
): T? {
    if (!this.exists() || this.length() == 0L) {
        return null
    }
    return binaryFormat.decodeFromByteArray(serializer, this.readBytes())
}


public fun SerialDescriptor.copy(newName: String): SerialDescriptor =
    buildClassSerialDescriptor(newName) { takeElementsFrom(this@copy) }

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
