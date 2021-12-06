/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package org.example.myplugin

import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toPlainText

fun main() {
    val x: String = ""
    val plain: PlainText = PlainText("")

    x + plain

    // x + plain
    run { x } + plain

    run { x.apply { x.let { also { x } } } } + plain
    run { x.apply { x.let { also { x } } } }.plus(plain)

    x + plain
    1 + plain
    x.plus(plain)
    1.plus(plain)
}