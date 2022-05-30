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
    override val baseTempDir: MiraiFile = MiraiFile.create("mirai_unit_tests")
    override val tempPath = "mirai_unit_tests/temp$rand"

    @Test
    override fun parent() {
        assertEquals("C:/Users/Shared/mirai_test", tempDir.parent!!.absolutePath)
        super.parent()
    }

    @Test
    override fun `resolve absolute`() {
        MiraiFile.create("$tempPath/").resolve("C:/Users").let {
            assertEquals("C:/Users", it.path)
            assertEquals("C:/Users", it.absolutePath)
        }
    }
}