/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.test

import kotlinx.coroutines.CoroutineDispatcher
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.utils.setSystemProp

/**
 * All test classes should inherit from [AbstractTest]
 */
expect abstract class AbstractTest() { // public, can be used in other modules
    fun borrowSingleThreadDispatcher(): CoroutineDispatcher

    companion object
}

internal fun initializeTestCommon() {
    setSystemProp("mirai.network.packet.logger", "true")
    setSystemProp("mirai.network.state.observer.logging", "true")
    setSystemProp("mirai.network.show.all.components", "true")
    setSystemProp("mirai.network.show.components.creation.stacktrace", "true")
    setSystemProp("mirai.network.handler.selector.logging", "true")

    Exception() // creates an exception to load relevant classes to estimate invocation time of test cases more accurately.
    IMirai::class.simpleName // similarly, load classes.

    Mirai // load services
}
