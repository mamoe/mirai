/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import org.junit.jupiter.api.AfterEach
import kotlin.test.BeforeTest

abstract class AbstractConsoleInstanceTest {
    init {
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        net.mamoe.mirai.utils.MiraiLoggerFactoryImplementationBridge.reinit()
    }

    val mockPlugin by lazy { mockKotlinPlugin() }
    private lateinit var implementation: MiraiConsoleImplementation
    val consoleImplementation: MiraiConsoleImplementation by ::implementation

    @BeforeTest
    protected open fun initializeConsole() {
        this.implementation = MockConsoleImplementation().apply { start() }
        CommandManager
        consoleImplementation.jvmPluginLoader.load(mockPlugin)
        consoleImplementation.jvmPluginLoader.enable(mockPlugin)
    }

    @AfterEach
    protected open fun stopConsole() {
        if (MiraiConsoleImplementation.instanceInitialized) {
            try {
                consoleImplementation.jvmPluginLoader.disable(mockPlugin)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                runBlocking { MiraiConsole.job.cancelAndJoin() }
            } catch (e: CancellationException) {
                // ignored
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                MiraiConsoleImplementation.currentBridge = null
            }
        }
        println("=========".repeat(4) + "CONSOLE STOPPED" + "=========".repeat(4))
    }

    companion object {
        fun mockKotlinPlugin(id: String = "org.test.test"): KotlinPlugin {
            return object : KotlinPlugin(JvmPluginDescription(id, "1.0.0")) {}
        }
    }
}