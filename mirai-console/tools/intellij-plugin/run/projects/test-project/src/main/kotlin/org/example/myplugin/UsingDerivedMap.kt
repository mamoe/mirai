/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package org.example.myplugin

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.messageChainOf
import org.example.myplugin.DataTest1.provideDelegate
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object UsingDerivedMap : AutoSavePluginConfig("data") {
    var p1 by value<MutableMap<String, String>>()
    var p2 by value<Map<String, String>>()

    var p4 by value<HashMap<String, String>>()
    var p3 by value<ConcurrentHashMap<String, String>>()

    var p8 by value<List<String>>()
    var p5 by value<ArrayList<String>>()
    var p7 by value<AbstractList<String>>()

    var p6 by value<MessageChain>(messageChainOf()) // no error
}
