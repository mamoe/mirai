/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.events.MessagePostSendEvent
import net.mamoe.mirai.event.events.MessagePreSendEvent
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.fail
import java.net.URL
import kotlin.reflect.jvm.jvmName
import kotlin.test.assertFails

internal open class TestBase {

    internal inline fun <reified T> assertIsInstance(value: Any?, block: T.() -> Unit = {}) {
        if (value !is T) {
            fail { "Actual value $value (${value?.javaClass}) is not instanceof ${T::class.jvmName}" }
        }
        block(value)
    }

    internal fun runTest(action: suspend CoroutineScope.() -> Unit) {
        runBlocking(block = action)
    }

    internal fun List<Event>.dropMessagePrePost() = filterNot {
        it is MessagePreSendEvent || it is MessagePostSendEvent<*>
    }

    internal fun List<Event>.dropMsgChat() = filterNot {
        it is MessageEvent || it is MessagePreSendEvent || it is MessagePostSendEvent<*>
    }

    internal fun String.toUrl(): URL = URL(this)

    internal inline fun <T> T.runAndAssertFails(block: T.() -> Unit) {
        assertFails { block() }
    }

    internal fun dynamicTest(displayName: String, action: suspend CoroutineScope.() -> Unit): DynamicTest {
        return DynamicTest.dynamicTest(displayName) { runBlocking(block = action) }
    }

    internal fun dynamicContainer(
        displayName: String,
        action: suspend CoroutineScope.() -> Iterable<DynamicNode>
    ): DynamicContainer {
        return DynamicContainer.dynamicContainer(displayName, runBlocking(block = action))
    }

}