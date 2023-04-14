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
import kotlin.test.Test

internal expect fun initPlatform()

/**
 * All test classes should inherit from [AbstractTest]
 */
expect abstract class AbstractTest() { // public, can be used in other modules
    fun borrowSingleThreadDispatcher(): CoroutineDispatcher

    companion object
}

internal expect class PlatformInitializationTest() : AbstractTest {
    @Test
    fun test()
}