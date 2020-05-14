/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.stylesheet.PrimaryStyleSheet
import net.mamoe.mirai.console.graphical.view.Decorator
import tornadofx.App
import tornadofx.find
import kotlin.system.exitProcess

//object MiraiGraphicalLoader {
//    @JvmStatic
//    fun main(args: Array<String>) {
//        launch<MiraiGraphicalUI>(args)
//    }
//}

class MiraiGraphicalUI : App(Decorator::class, PrimaryStyleSheet::class) {

    override fun init() {
        super.init()
        MiraiConsole.start(find<MiraiGraphicalUIController>(),MiraiConsoleGraphicalLoader.coreVersion,MiraiConsoleGraphicalLoader.consoleVersion)
    }

    override fun stop() {
        super.stop()
        MiraiConsole.stop()
        exitProcess(0)
    }
}
