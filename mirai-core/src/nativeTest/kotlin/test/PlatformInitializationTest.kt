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
import kotlinx.coroutines.test.StandardTestDispatcher
import net.mamoe.mirai.internal.initMirai

/**
 * All test classes should inherit from [AbstractTest]
 *
 * Note: To run a test in native sourceSets, use IDEA key shortcut 'control + shift + R' on macOS and 'Ctrl + Shift + R' on Windows.
 * Or you can right-click the function name of the test case and invoke 'Run ...'. You should not expect to see a button icon around the line numbers.
 */
actual abstract class AbstractTest actual constructor() {
    actual fun borrowSingleThreadDispatcher(): CoroutineDispatcher = StandardTestDispatcher()

    init {
        Companion
    }

    actual companion object {
        init {
            initMirai()
            initializeTestCommon()
        }
    }

}