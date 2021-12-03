/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.dokka

import kotlinx.serialization.json.Json
import java.io.File

val pages = File("mirai-dokka/pages")
val json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

fun system(cmd: String) {
    val rsp = ProcessBuilder(cmd).inheritIO().start().waitFor()
    if (rsp != 0) error("Exec return $rsp, $cmd")
}

fun exec(vararg cmd: String) {
    val rsp = ProcessBuilder(*cmd).inheritIO().start().waitFor()
    if (rsp != 0) error("Exec return $rsp, ${cmd.joinToString(" ")}")
}

fun repoexec(vararg cmd: String) {
    val rsp = ProcessBuilder(*cmd).inheritIO().directory(pages).start().waitFor()
    if (rsp != 0) error("Exec return $rsp, ${cmd.joinToString(" ")}")
}
