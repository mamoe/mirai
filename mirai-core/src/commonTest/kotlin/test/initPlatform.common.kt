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

        System.setProperty("mirai.debug.network.state.observer.logging", "false")
        System.setProperty("mirai.debug.network.show.all.components", "true")
        System.setProperty("mirai.debug.network.show.components.creation.stacktrace", "true")

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