/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlatformUtilsTest {

    @Test
    fun testZip() {
        assertEquals("test", MiraiPlatformUtils.unzip(MiraiPlatformUtils.zip("test".toByteArray())).encodeToString())
    }

    @Test
    fun testGZip() {
        assertEquals("test", MiraiPlatformUtils.ungzip(MiraiPlatformUtils.gzip("test".toByteArray())).encodeToString())
    }
}