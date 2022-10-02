/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("StructureToStringTransformerKt_common")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmName

public interface StructureToStringTransformer {
    public fun transform(any: Any?): String

    public fun transformAndDesensitize(any: Any?): String

    public companion object {
        private class ObjectToStringStructureToStringTransformer : StructureToStringTransformer {
            override fun transform(any: Any?): String = any.toString()
            override fun transformAndDesensitize(any: Any?): String = any.toString()
        }

        public val instance: StructureToStringTransformer by lazy {
            loadService(StructureToStringTransformer::class) {
                getPlatformDefaultStructureToStringTransformer()
                    ?: ObjectToStringStructureToStringTransformer()
            }
        }

        public val available: Boolean = instance !is ObjectToStringStructureToStringTransformer
    }
}


internal expect fun getPlatformDefaultStructureToStringTransformer(): StructureToStringTransformer?

/**
 * Do not call this inside [Any.toString]. `StackOverflowError` may happen.
 */
public fun Any?.structureToString(): String = StructureToStringTransformer.instance.transform(this)
public fun Any?.structureToStringIfAvailable(): String? {
    return if (StructureToStringTransformer.available) {
        StructureToStringTransformer.instance.transform(this)
    } else null
}


public fun Any?.structureToStringAndDesensitize(): String =
    StructureToStringTransformer.instance.transformAndDesensitize(this)

public fun Any?.structureToStringAndDesensitizeIfAvailable(): String? {
    return if (StructureToStringTransformer.available) {
        StructureToStringTransformer.instance.transformAndDesensitize(this)
    } else null
}