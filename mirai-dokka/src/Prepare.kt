/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.dokka

fun main() {
    if (pages.resolve(".git").isDirectory) {
        return
    }
    exec("git", "clone", "https://github.com/project-mirai/mirai-doc.git", pages.absolutePath)
}
