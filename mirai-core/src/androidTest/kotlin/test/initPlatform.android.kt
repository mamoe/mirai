/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.test

import net.mamoe.mirai.utils.MiraiLogger
import kotlin.test.Test
import kotlin.test.assertTrue

internal actual fun initPlatform() {

}

internal actual class PlatformInitializationTest : AbstractTest() {

    @Test
    actual fun test() {
        assertTrue {
            @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
            MiraiLogger.Factory.create(this::class, "1") is net.mamoe.mirai.internal.utils.StdoutLogger
        }
    }
}