/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.graphical

import kotlinx.coroutines.cancel
import net.mamoe.mirai.console.MiraiConsole
import tornadofx.launch
import kotlin.concurrent.thread

class MiraiConsoleGraphicalLoader {
     companion object {
         internal var coreVersion   :String = "0.0.0"
         internal var consoleVersion: String = "0.0.0"
         @JvmStatic
         fun load(
            coreVersion: String,
            consoleVersion: String
         ) {
            this.coreVersion    = coreVersion
            this.consoleVersion = consoleVersion
             Runtime.getRuntime().addShutdownHook(thread(start = false) {
                 MiraiConsole.cancel()
             })
            launch<MiraiGraphicalUI>()
        }

    }
}