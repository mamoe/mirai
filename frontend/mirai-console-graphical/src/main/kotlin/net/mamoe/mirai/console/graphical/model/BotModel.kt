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