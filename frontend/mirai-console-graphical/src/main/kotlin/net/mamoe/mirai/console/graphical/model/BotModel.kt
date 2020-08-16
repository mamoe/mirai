/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical.model

import javafx.beans.property.SimpleObjectProperty
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalFrontEndController
import net.mamoe.mirai.utils.SimpleLogger
import tornadofx.*
import java.util.*

class BotModel(val uin: Long) {
    val botProperty = SimpleObjectProperty<Bot>(null)
    var bot: Bot by botProperty

    val logHistory = observableListOf<Pair<String, String>>()
    val logger: SimpleLogger =
        SimpleLogger(uin.toString()) { priority: SimpleLogger.LogPriority, message: String?, e: Throwable? ->

            val frontend = find<MiraiGraphicalFrontEndController>()

            frontend.run {
                logHistory.apply {
                    val time = sdf.format(Date())
                    add("[$time] $uin $message" to priority.name)
                    trim()
                }
            }
        }

    val admins = observableListOf<Long>()
}

class BotViewModel(botModel: BotModel? = null) : ItemViewModel<BotModel>(botModel) {
    val bot = bind(BotModel::botProperty)
    val logHistory = bind(BotModel::logHistory)
    val admins = bind(BotModel::admins)
}