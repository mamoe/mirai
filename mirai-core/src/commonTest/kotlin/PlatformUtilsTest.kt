/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.utils.deflate
import net.mamoe.mirai.utils.gzip
import net.mamoe.mirai.utils.inflate
import net.mamoe.mirai.utils.ungzip
import kotlin.test.Test
import kotlin.test.assertEquals

internal class PlatformUtilsTest : AbstractTest() {

    @Test
    fun testZip() {
        assertEquals("test", "test".toByteArray().deflate().inflate().decodeToString())
    }

    @Test
    fun testGZip() {
        assertEquals("test", "test".toByteArray().gzip().ungzip().decodeToString())
    }
}