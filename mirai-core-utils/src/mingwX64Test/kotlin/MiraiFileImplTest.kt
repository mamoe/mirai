/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

internal class WindowsMiraiFileImplTest : AbstractNativeMiraiFileImplTest() {
    private val rand = Random.nextInt().absoluteValue
    override val baseTempDir: MiraiFile = MiraiFile.create("C:\\mirai_unit_tests")
    override val tempPath = "C:\\mirai_unit_tests\\temp$rand"

    @Test
    override fun parent() {
        assertEquals("C:\\mirai_unit_tests", tempDir.parent!!.absolutePath)
        super.parent()
    }

    @Test
    override fun `canonical paths for non-canonical input`() {
        super.`canonical paths for non-canonical input`()

        // extra /sss/..
        MiraiFile.create("$tempPath/sss/..").resolve("test").let {
            assertPathEquals("${tempPath}/test", it.path) // Windows resolves always
            assertPathEquals("${tempPath}/test", it.absolutePath)
        }
    }

    @Test
    override fun `resolve absolute`() {
        MiraiFile.create("$tempPath/").resolve("C:\\mirai_unit_tests").let {
            assertEquals("C:\\mirai_unit_tests", it.path)
            assertEquals("C:\\mirai_unit_tests", it.absolutePath)
        }
    }
}