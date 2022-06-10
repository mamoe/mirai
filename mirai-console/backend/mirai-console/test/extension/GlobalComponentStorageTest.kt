/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extension

import net.mamoe.mirai.console.internal.extension.GlobalComponentStorage
import net.mamoe.mirai.console.internal.extension.GlobalComponentStorageImpl
import net.mamoe.mirai.console.testFramework.AbstractConsoleInstanceTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GlobalComponentStorageTest : AbstractConsoleInstanceTest() {
    class MyInstance

    class MyExtension(override val instance: MyInstance, override val priority: Int) : InstanceExtension<MyInstance> {
        companion object EP : AbstractInstanceExtensionPoint<MyExtension, MyInstance>(MyExtension::class)
    }

    @Test
    fun `can register`() {
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 1))
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 2))
        GlobalComponentStorage.getExtensionsList(MyExtension).run {
            assertEquals(2, size)
        }
    }

    @Test
    fun `can contribute`() {
        GlobalComponentStorage.contribute(MyExtension, mockPlugin, MyExtension(MyInstance(), 1))
        GlobalComponentStorage.contribute(MyExtension, mockPlugin, MyExtension(MyInstance(), 2))
        GlobalComponentStorage.getExtensionsList(MyExtension).run {
            assertEquals(2, size)
        }
    }

    @Test
    fun `can sort by priority`() {
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 1))
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 2))
        GlobalComponentStorage.getExtensionsList(MyExtension).run {
            assertEquals(2, size)
            assertEquals(2, first().extension.priority)
            assertEquals(1, get(1).extension.priority)
        }
    }

    @Test
    fun `can sort by priority 2`() {
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 2))
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 1))
        GlobalComponentStorage.getExtensionsList(MyExtension).run {
            assertEquals(2, size)
            assertEquals(2, first().extension.priority)
            assertEquals(1, get(1).extension.priority)
        }
    }

    @Test
    fun `can fold`() {
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 2))
        GlobalComponentStorage.contributeConsole(MyExtension, MyExtension(MyInstance(), 1))
        val list = GlobalComponentStorage.foldExtensions(MyExtension, listOf<MyExtension>()) { acc, extension ->
            acc + extension
        }
        assertEquals(2, list.size)
        assertEquals(2, list.first().priority)
        assertEquals(1, list[1].priority)
    }
}

private fun <T : Extension> GlobalComponentStorageImpl.getExtensionsList(ep: ExtensionPoint<T>): List<ExtensionRegistry<T>> {
    return getExtensions(ep).toList()
}