/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "NO_REFLECTION_IN_CLASS_PATH")

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.loadService


internal fun Any?.structureToString(): String = StructureToStringTransformer.instance.transform(this)

@Suppress("FunctionName")
@Deprecated(
    "",
    ReplaceWith("this.structureToString()", "net.mamoe.mirai.internal.utils.structureToString"),
    level = DeprecationLevel.ERROR
) // kept for local developers for some time
@DeprecatedSinceMirai(errorSince = "2.10")
internal fun Any?._miraiContentToString(): String = this.structureToString()

private val SoutvLogger: MiraiLogger by lazy {
    MiraiLogger.Factory.create(
        StructureToStringTransformer::class,
        "printStructurally"
    )
}

@Deprecated(
    "",
    ReplaceWith("this.printStructure(name)", "net.mamoe.mirai.internal.utils.printStructure"),
    level = DeprecationLevel.ERROR
)
@DeprecatedSinceMirai(errorSince = "2.10")
internal fun Any?.soutv(name: String = "unnamed") = this.printStructure(name)

internal fun Any?.printStructure(name: String = "unnamed") {
    return SoutvLogger.debug { "$name = ${this.structureToString()}" }
}

internal fun interface StructureToStringTransformer {
    fun transform(any: Any?): String

    companion object {
        private class ObjectToStringStructureToStringTransformer : StructureToStringTransformer {
            override fun transform(any: Any?): String = any.toString()
        }

        val instance by lazy {
            loadService(StructureToStringTransformer::class) { ObjectToStringStructureToStringTransformer() }
        }
    }
}

