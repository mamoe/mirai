package net.mamoe.mirai.console

import net.mamoe.mirai.console.pure.MiraiConsoleUIPure
import kotlin.concurrent.thread

class MiraiConsoleTerminalLoader {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.contains("pure") || args.contains("-pure") || System.getProperty(
                    "os.name",
                    ""
                ).toLowerCase().contains("windows")
            ) {
                println("[MiraiConsoleTerminalLoader]: 将以Pure[兼容模式]启动Console")
                MiraiConsole.start(MiraiConsoleUIPure())
            } else {
                MiraiConsoleTerminalUI.start()
                thread {
                    MiraiConsole.start(
                        MiraiConsoleTerminalUI
                    )
                }
            }
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                MiraiConsole.stop()
                MiraiConsoleTerminalUI.exit()
            })
        }
    }
}