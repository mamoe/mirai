package net.mamoe.mirai.console.graphical

import net.mamoe.mirai.console.graphical.view.PrimaryView
import tornadofx.App
import tornadofx.launch

fun main(args: Array<String>) {
    launch<MainApp>(args)
}

class MainApp: App(PrimaryView::class) {

    override fun init() {
        super.init()

    }
}