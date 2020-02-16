package net.mamoe.mirai.console

import kotlin.concurrent.thread

class MiraiConsoleTerminalLoader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MiraiConsoleTerminalUI.start()
            thread {
                MiraiConsole.start(
                    MiraiConsoleTerminalUI
                )
            }
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                MiraiConsole.stop()
            })
        }
    }
}