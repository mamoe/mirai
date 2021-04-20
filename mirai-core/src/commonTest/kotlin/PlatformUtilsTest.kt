/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.utils.*
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlatformUtilsTest : AbstractTest() {

    @Test
    fun testZip() {
        assertEquals("test", "test".toByteArray().zip().unzip().encodeToString())
    }

    @Test
    fun testGZip() {
        assertEquals("test", "test".toByteArray().gzip().ungzip().encodeToString())
    }
}