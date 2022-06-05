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

internal class UnixMiraiFileImplTest : AbstractNativeMiraiFileImplTest() {
    private val rand = Random.nextInt().absoluteValue
    override val baseTempDir: MiraiFile by lazy { MiraiFile.create(MiraiFile.getWorkingDir().absolutePath + "/mirai_unit_tests") }
    override val tempPath by lazy { "${baseTempDir.absolutePath}/temp$rand" }

    @Test
    override fun parent() {
        assertEquals(baseTempDir.absolutePath, tempDir.parent!!.absolutePath)
        assertEquals(null, MiraiFile.create("/").parent)
        assertEquals("/", MiraiFile.create("/dev").parent?.path)
        assertEquals("/", MiraiFile.create("/dev").parent?.absolutePath)
        super.parent()
    }

    override fun `canonical paths for non-canonical input`() {
        super.`canonical paths for non-canonical input`()

        // extra /sss/..
        MiraiFile.create("$tempPath/sss/..").resolve("test").let {
            assertPathEquals("${tempPath}/sss/../test", it.path) // because file is not found
            assertPathEquals("${tempPath}/sss/../test", it.absolutePath)
        }
    }

    @Test
    override fun `resolve absolute`() {
        MiraiFile.create("$tempPath/").resolve("/Users").let {
            assertEquals("/Users", it.path)
            assertEquals("/Users", it.absolutePath)
        }
    }
}