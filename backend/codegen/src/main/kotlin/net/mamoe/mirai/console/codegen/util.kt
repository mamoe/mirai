/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.codegen

import java.io.File

fun codegen(targetFile: String, regionName: String, block: StringBuilder.() -> Unit) {
    //// region PrimitiveValue CODEGEN START ////
    //// region PrimitiveValue CODEGEN END ////

    targetFile.findFileSmart().also {
        println("Codegen target: ${it.absolutePath}")
    }.apply {
        writeText(
            readText()
                .replace(Regex("""//// region $regionName CODEGEN START ////([\s\S]*?)//// endregion $regionName CODEGEN END ////""")) {
                    val code = StringBuilder().apply(block).toString()
                    """
                        |//// region $regionName CODEGEN START ////
                        |
                        |$code
                        |
                        |//// endregion $regionName CODEGEN END ////
                    """.trimMargin()
                }
        )
    }
}

fun String.findFileSmart(): File = kotlin.run {
    if (contains("/")) { // absolute
        File(this)
    } else {
        val list = File(".").walk().filter { it.name == this }.toList()
        if (list.isNotEmpty()) return list.single()

        File(".").walk().filter { it.name.contains(this) }.single()
    }
}.also {
    require(it.exists()) { "file doesn't exist" }
}

fun main() {
    codegen("Value.kt", "PrimitiveValue") {

    }
}