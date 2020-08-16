/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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
                MiraiConsoleTerminalFrontEnd.start()
                thread {
                    MiraiConsole.start(
                        MiraiConsoleTerminalFrontEnd
                    )
                }
            }
            Runtime.getRuntime().addShutdownHook(thread(start = false) {
                MiraiConsole.stop()
                MiraiConsoleTerminalFrontEnd.exit()
            })
        }
    }
}