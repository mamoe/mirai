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
import kotlin.test.assertTrue

internal class UnixMiraiFileImplTest : AbstractNativeMiraiFileImplTest() {
    private val rand = Random.nextInt().absoluteValue
    override val baseTempDir: MiraiFile = MiraiFile.create("/Users/Shared/mirai_test")
    override val tempPath = "/Users/Shared/mirai_test/temp$rand"
    private val tempDir = MiraiFile.create(tempPath).apply {
        assertTrue("Failed to make temp directory: ${this.absolutePath}") { mkdirs() }
    }

    @Test
    override fun parent() {
        assertEquals("/Users/Shared/mirai_test", tempDir.parent!!.absolutePath)
        assertEquals(null, MiraiFile.create("/").parent)
        assertEquals("/", MiraiFile.create("/dev").parent?.path)
        assertEquals("/", MiraiFile.create("/dev").parent?.absolutePath)
        super.parent()
    }

    @Test
    override fun `resolve absolute`() {
        MiraiFile.create("$tempPath/").resolve("/Users").let {
            assertEquals("/Users", it.path)
            assertEquals("/Users", it.absolutePath)
        }
    }
}