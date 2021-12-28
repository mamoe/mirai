/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package org.example.myplugin

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value
import org.example.myplugin.DataTest1.provideDelegate


object DataTest2 : ReadOnlyPluginConfig("data") {
    var pp by value<String>()
    val x by value<V>(V(""))
    // var should be reported

    class V  constructor(val s: String)
}
