/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.logging

import org.junit.jupiter.api.Test

@Suppress("ClassName")
internal class TestALC_PathBased {
    @Test
    fun `test AbstractLoggerController$PathBased`() {
        val config = mapOf(
            "test" to "ALL",
            "test.test" to "VERBOSE",
            "test.test.test" to "NONE",
        ).mapValues { AbstractLoggerController.LogPriority.valueOf(it.value) }

        val c = object : AbstractLoggerController.PathBased() {
            override val defaultPriority: LogPriority
                get() = LogPriority.NONE

            override fun findPriority(identity: String?): LogPriority? {
                if (identity == null) return defaultPriority
                return config[identity]
            }

            fun priority(i: String?): LogPriority = getPriority(i)
        }

        fun assertSame(path: String?, p: String) {
            kotlin.test.assertSame(c.priority(path), AbstractLoggerController.LogPriority.valueOf(p))
        }

        assertSame("test.test.test", "NONE")
        assertSame("test.test.test.more.test", "NONE")

        assertSame("test.test.t1", "VERBOSE")
        assertSame("test.test.t15w", "VERBOSE")
        assertSame("test.test", "VERBOSE")

        assertSame("test", "ALL")
        assertSame("test.tes1ww", "ALL")
        assertSame("test.asldjawe.awej2oi3", "ALL")

        assertSame("AWawex", "NONE")
        assertSame("awpejaszx.aljewkz", "NONE")
        assertSame("test0.awekjo23xxxxx", "NONE")
    }
}
