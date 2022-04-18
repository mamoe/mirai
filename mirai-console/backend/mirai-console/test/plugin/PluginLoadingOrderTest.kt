/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.plugin

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.internal.plugin.PluginInfiniteCircularDependencyReferenceException
import net.mamoe.mirai.console.internal.plugin.PluginManagerImpl
import net.mamoe.mirai.console.internal.plugin.PluginMissingDependencyException
import net.mamoe.mirai.console.internal.plugin.impl
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.loader.PluginLoadException
import net.mamoe.mirai.console.testFramework.AbstractConsoleInstanceTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class PluginLoadingOrderTest : AbstractConsoleInstanceTest() {
    private val pm: PluginManagerImpl get() = MiraiConsole.pluginManager.impl

    private class Reorder<T>(val l: List<T>) {
        operator fun get(vararg indexes: Int): List<T> {
            return MutableList(indexes.size) { l[indexes[it]] }
        }
    }

    private val <T> List<T>.reorder: Reorder<T> get() = Reorder(this)

    @Test
    fun singlePlugin() {
        val descriptions = listOf<PluginDescription>(
            JvmPluginDescription("a.a.a", "1.0.0"),
        )
        assertEquals(
            descriptions,
            pm.__sortPluginDescription(descriptions)
        )
    }

    @Test
    fun successIfOptional() {
        val descriptions = listOf<PluginDescription>(
            JvmPluginDescription("a.a.a", "1.0.0") {
                dependsOn("a.a.a.opt", isOptional = true)
            },
            JvmPluginDescription("c.c.c", "1.0.0") {
                dependsOn("c.c.c.opt", isOptional = true)
            },
            JvmPluginDescription("b.b.b", "1.0.0") {
                dependsOn("b.b.b.opt", isOptional = true)
            },
        )
        assertEquals(
            descriptions,
            pm.__sortPluginDescription(descriptions)
        )
    }

    @Test
    fun failedIfDeathLock() {
        assertFailsWith<PluginInfiniteCircularDependencyReferenceException> {
            pm.__sortPluginDescription(
                listOf(
                    JvmPluginDescription("a.a.a", "1.0.0") {
                        dependsOn("b.b.b")
                    },
                    JvmPluginDescription("b.b.b", "1.0.0") {
                        dependsOn("a.a.a")
                    },
                )
            )
        }.let { assertEquals("Found circular plugin dependency: a.a.a -> b.b.b -> a.a.a", it.message) }
    }

    @Test
    fun failedIfMissing() {
        assertFailsWith<PluginMissingDependencyException> {
            pm.__sortPluginDescription(
                listOf(
                    JvmPluginDescription("a.a.a", "1.0.0") { dependsOn("b.b.b") }
                )
            )
        }.let { assertEquals("Cannot load plugin 'a.a.a', missing dependencies: 'b.b.b'", it.message) }
    }

    @Test
    fun failedIfVersionNotMatch() {
        assertFailsWith<PluginLoadException> {
            pm.__sortPluginDescription(
                listOf(
                    JvmPluginDescription("a.a.a", "1.0.0"),
                    JvmPluginDescription("b.b.b", "1.0.0") {
                        dependsOn("a.a.a", "<0.9.9")
                    },
                )
            )
        }.let {
            assertEquals(
                "Plugin 'b.b.b' ('b.b.b') requires 'a.a.a' with version <0.9.9 while the resolved is 1.0.0",
                it.message
            )
        }
        assertFailsWith<PluginLoadException> {
            pm.__sortPluginDescription(
                listOf(
                    JvmPluginDescription("a.a.a", "1.0.0"),
                    JvmPluginDescription("b.b.b", "1.0.0") {
                        dependsOn("a.a.a", "<0.9.9", isOptional = true)
                    },
                )
            )
        }.let {
            assertEquals(
                "Plugin 'b.b.b' ('b.b.b') requires 'a.a.a' with version <0.9.9 while the resolved is 1.0.0",
                it.message
            )
        }
    }


    @Test
    fun allNonDependencyPlugin() {
        val descriptions = listOf<PluginDescription>(
            JvmPluginDescription("a.a.a", "1.0.0"),
            JvmPluginDescription("a.a.b", "1.0.0"),
            JvmPluginDescription("a.c.w", "1.0.0"),
            JvmPluginDescription("a.z.x", "1.0.0"),
            JvmPluginDescription("a.w.q", "1.0.0"),
            JvmPluginDescription("w.z.a", "1.0.0"),
        )
        assertEquals(
            descriptions,
            pm.__sortPluginDescription(descriptions)
        )
    }

    @Test
    fun pluginWithDependencies() {
        val descriptions = listOf<PluginDescription>(
            JvmPluginDescription("a.a.a", "1.0.0") {
                dependsOn("b.b.b")
            },
            JvmPluginDescription("b.b.b", "1.0.0"),
        )
        assertEquals(
            descriptions.reorder[1, 0],
            pm.__sortPluginDescription(descriptions)
        )
    }

    @Test
    fun pluginWithOptionalDependency() {
        val desc = listOf<PluginDescription>(
            JvmPluginDescription("a.a.a.opt", "1.0.0") {
                dependsOn("a.a.a", isOptional = true)
            },
            JvmPluginDescription("a.a.a", "1.0.0"),
            JvmPluginDescription("b.b.b", "1.0.0"),
            JvmPluginDescription("b.b.b.opt", "1.0.0") {
                dependsOn("b.b.b", isOptional = true)
            },
            JvmPluginDescription("c.c.c.opt", "1.0.0") {
                dependsOn("a.a.a")
            },
        )
        assertEquals(
            desc.reorder[1, 2, 0, 3, 4],
            pm.__sortPluginDescription(desc)
        )
    }

    @Test
    fun `2nd direct depend`() {
        val descs = listOf(
            JvmPluginDescription("c.c.c", "1.0.0") {
                dependsOn("b.b.b", "1.0.0")
            },
            JvmPluginDescription("a.a.a", "1.0.0"),
            JvmPluginDescription("b.b.b", "1.0.0") {
                dependsOn("a.a.a", "1.0.0")
            },
        )
        assertEquals(
            descs.reorder[1, 2, 0],
            pm.__sortPluginDescription(descs)
        )
    }

    @Test
    fun `3nd optional depend`() {
        val desc = listOf(
            JvmPluginDescription("b.b.b", "1.0.0") {
                dependsOn("a.a.a", isOptional = true)
            },
            JvmPluginDescription("a.a.a", "1.0.0"),
            JvmPluginDescription("d.d.d", "1.0.0") {
                dependsOn("a.a.a")
                dependsOn("c.c.c")
            },
            JvmPluginDescription("c.c.c", "1.0.0") {
                dependsOn("b.b.b", isOptional = true)
            },
            JvmPluginDescription("e.e.e", "1.0.0") {
                dependsOn("c.c.c", isOptional = true)
                dependsOn("d.d.d")
            },
        )
        assertEquals(
            desc.reorder[1, 0, 3, 2, 4],
            pm.__sortPluginDescription(desc)
        )
    }

}