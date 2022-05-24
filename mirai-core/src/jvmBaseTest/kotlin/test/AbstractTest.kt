/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.test

import net.mamoe.mirai.IMirai
import net.mamoe.mirai.internal.network.framework.SynchronizedStdoutLogger
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.setSystemProp
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

@Timeout(value = 7, unit = TimeUnit.MINUTES)
internal actual abstract class AbstractTest actual constructor() : CommonAbstractTest() {
    actual companion object {
        init {
            initPlatform()

            @Suppress("DEPRECATION_ERROR")
            MiraiLogger.setDefaultLoggerCreator {
                SynchronizedStdoutLogger(it)
            }

            setSystemProp("mirai.network.packet.logger", "true")
            setSystemProp("mirai.network.state.observer.logging", "true")
            setSystemProp("mirai.network.show.all.components", "true")
            setSystemProp("mirai.network.show.components.creation.stacktrace", "true")
            setSystemProp("mirai.network.handle.selector.logging", "true")

            Exception() // create a exception to load relevant classes to estimate invocation time of test cases more accurately.
            IMirai::class.simpleName // similarly, load classes.
        }
    }
}
