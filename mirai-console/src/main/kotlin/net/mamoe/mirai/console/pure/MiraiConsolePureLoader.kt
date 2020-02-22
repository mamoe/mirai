package net.mamoe.mirai.console.pure

import net.mamoe.mirai.console.MiraiConsole
import kotlin.concurrent.thread

class MiraiConsolePureLoader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MiraiConsole.start(MiraiConsoleUIPure())
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                MiraiConsole.stop()
            })
        }
    }
}