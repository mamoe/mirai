package net.mamoe.mirai.console.graphical

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.graphical.controller.MiraiGraphicalUIController
import net.mamoe.mirai.console.graphical.styleSheet.PrimaryStyleSheet
import net.mamoe.mirai.console.graphical.view.Decorator
import tornadofx.App
import tornadofx.find
import tornadofx.launch

fun main(args: Array<String>) {
    launch<MiraiGraphicalUI>(args)
}

class MiraiGraphicalUI : App(Decorator::class, PrimaryStyleSheet::class) {

    override fun init() {
        super.init()

        MiraiConsole.start(find<MiraiGraphicalUIController>())
    }

    override fun stop() {
        super.stop()
        MiraiConsole.stop()
    }
}
