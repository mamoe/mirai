/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.testFramework

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.initTestEnvironment
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AbstractConsoleTest {
    val mockPlugin by lazy { mockKotlinPlugin() }

    @BeforeEach
    protected open fun initializeConsole() {
        initTestEnvironment()
    }

    @AfterEach
    protected open fun cancelConsole() {
        if (MiraiConsoleImplementation.instanceInitialized) {
            try {
                runBlocking { MiraiConsole.job.cancelAndJoin() }
            } catch (e: CancellationException) {
                // ignored
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                MiraiConsoleImplementation.instance = null
            }
        }
    }

    companion object {
        fun mockKotlinPlugin(): KotlinPlugin {
            return object : KotlinPlugin(JvmPluginDescription("org.test.test", "1.0.0")) {}
        }
    }
}