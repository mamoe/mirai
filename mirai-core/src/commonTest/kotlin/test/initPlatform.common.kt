/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.test

import kotlinx.coroutines.*
import kotlin.test.AfterTest
import kotlin.test.Test

internal expect fun initPlatform()


@Suppress("UnnecessaryOptInAnnotation") // on JVM
@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
internal abstract class CommonAbstractTest {
    private val dispatchers = mutableListOf<CloseableCoroutineDispatcher>()

    fun borrowSingleThreadDispatcher(): CoroutineDispatcher {
        return newSingleThreadContext(this::class.simpleName ?: "CommonAbstractTest")
    }

    @AfterTest
    fun closeAllDispatchers() {
        for (dispatcher in dispatchers) {
            dispatcher.close()
        }
    }
}

/**
 * All test classes should inherit from [AbstractTest]
 */
internal expect abstract class AbstractTest() : CommonAbstractTest {

    companion object
}

internal expect class PlatformInitializationTest() : AbstractTest {
    @Test
    fun test()
}