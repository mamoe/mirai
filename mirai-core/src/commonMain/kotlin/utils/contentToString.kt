/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "NO_REFLECTION_IN_CLASS_PATH")

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.loadService


/**
 * Do not call this inside [Any.toString]. [StackOverflowError] may happen. Call [structureToStringIfAvailable] instead.
 */
internal fun Any?.structureToString(): String = StructureToStringTransformer.instance.transform(this)
internal fun Any?.structureToStringIfAvailable(): String? {
    return if (StructureToStringTransformer.available) {
        StructureToStringTransformer.instance.transform(this)
    } else null
}


internal fun Any?.structureToStringAndDesensitize(): String =
    StructureToStringTransformer.instance.transformAndDesensitize(this)

internal fun Any?.structureToStringAndDesensitizeIfAvailable(): String? {
    return if (StructureToStringTransformer.available) {
        StructureToStringTransformer.instance.transformAndDesensitize(this)
    } else null
}

private val SoutvLogger: MiraiLogger by lazy {
    MiraiLogger.Factory.create(
        StructureToStringTransformer::class,
        "printStructurally"
    )
}

internal fun Any?.printStructure(name: String = "unnamed") {
    return SoutvLogger.debug { "$name = ${this.structureToString()}" }
}

internal interface StructureToStringTransformer {
    fun transform(any: Any?): String

    fun transformAndDesensitize(any: Any?): String

    companion object {
        private class ObjectToStringStructureToStringTransformer : StructureToStringTransformer {
            override fun transform(any: Any?): String = any.toString()
            override fun transformAndDesensitize(any: Any?): String = any.toString()
        }

        val instance by lazy {
            loadService(StructureToStringTransformer::class) { ObjectToStringStructureToStringTransformer() }
        }

        val available = instance !is ObjectToStringStructureToStringTransformer
    }
}

