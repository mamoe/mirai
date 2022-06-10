/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.test

import net.mamoe.mirai.IMirai
import net.mamoe.mirai.internal.network.framework.SynchronizedStdoutLogger
import net.mamoe.mirai.utils.MiraiLogger
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

internal expect fun initPlatform()

/**
 * All test classes should inherit from [AbstractTest]
 */
@Timeout(value = 7, unit = TimeUnit.MINUTES)
abstract class AbstractTest {
    init {
        initPlatform()

        restoreLoggerFactory()

        System.setProperty("mirai.network.packet.logger", "true")
        System.setProperty("mirai.network.state.observer.logging", "true")
        System.setProperty("mirai.network.show.all.components", "true")
        System.setProperty("mirai.network.show.components.creation.stacktrace", "true")
        System.setProperty("mirai.network.handle.selector.logging", "true")

    }

    @AfterEach
    protected fun restoreLoggerFactory() {
        @Suppress("DEPRECATION_ERROR")
        MiraiLogger.setDefaultLoggerCreator {
            SynchronizedStdoutLogger(it)
        }
    }

    companion object {
        init {
            Exception() // create a exception to load relevant classes to estimate invocation time of test cases more accurately.
            IMirai::class.simpleName // similarly, load classes.
        }
    }
}

internal expect class PlatformInitializationTest() : AbstractTest {
    @Test
    fun test()
}