/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.StructureToStringTransformer
import net.mamoe.mirai.utils.structureToString


private val SoutvLogger: MiraiLogger by lazy {
    MiraiLogger.Factory.create(
        StructureToStringTransformer::class,
        "printStructurally"
    )
}

internal fun Any?.printStructure(name: String = "unnamed") {
    return SoutvLogger.debug { "$name = ${this.structureToString()}" }
}
