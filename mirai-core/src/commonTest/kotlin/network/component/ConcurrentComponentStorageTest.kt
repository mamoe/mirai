/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.component

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal data class TestComponent2(
    val value: Int
) {
    companion object : ComponentKey<TestComponent2>
}

internal data class TestComponent3(
    val value: Int
) {
    companion object : ComponentKey<TestComponent3>
}

internal class ConcurrentComponentStorageTest : AbstractMutableComponentStorageTest() {
    override fun createStorage(): MutableComponentStorage = ConcurrentComponentStorage(showAllComponents = true)

    @Test
    fun `test toString non debug`() {
        val storage = ConcurrentComponentStorage(showAllComponents = false).apply {
            set(TestComponent2, TestComponent2(1))
        }
        assertEquals("ConcurrentComponentStorage(size=1)", storage.toString())
    }

    @Test
    fun `test toString debugging`() {
        val storage = ConcurrentComponentStorage(showAllComponents = true).apply {
            set(TestComponent2, TestComponent2(1))
        }
        assertEquals(
            """ConcurrentComponentStorage(size=1) {
            |  TestComponent2: TestComponent2(value=1)
            |}""".trimMargin(), storage.toString()
        )
    }
}