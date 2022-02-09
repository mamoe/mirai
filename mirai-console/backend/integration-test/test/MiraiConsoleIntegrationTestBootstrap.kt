/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

import net.mamoe.console.integrationtest.testpoints.DoNothingPoint
import net.mamoe.console.integrationtest.testpoints.MCITBSelfAssertions
import net.mamoe.console.integrationtest.testpoints.plugin.PluginDataRenameToIdTest
import net.mamoe.console.integrationtest.testpoints.terminal.TestTerminalLogging
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*
import kotlin.reflect.KClass


class MiraiConsoleIntegrationTestBootstrap {
    @Test
    fun bootstrap() {
        /*
        implementation note:
        不使用 @TempDir 是为了保存最后一次失败快照, 便于 debug
         */
        val workingDir = File("build/IntegrationTest") // mirai-console/backend/integration-test/build/IntegrationTest
        val launcher = MiraiConsoleIntegrationTestLauncher()
        launcher.workingDir = workingDir
        launcher.plugins = readStringListFromEnv("IT_PLUGINS")
        launcher.points = listOf<Any>(
            DoNothingPoint,
            MCITBSelfAssertions,
            PluginDataRenameToIdTest,
            TestTerminalLogging,
        ).asSequence().map { v ->
            when (v) {
                is Class<*> -> v
                is KClass<*> -> v.java
                else -> v.javaClass
            }
        }.map { it.name }.toMutableList()
        launcher.vmoptions = mutableListOf(
            *ManagementFactory.getRuntimeMXBean().inputArguments.filterNot {
                it.startsWith("-Djava.security.manager=")
            }.toTypedArray(),
            *System.getenv("IT_ARGS")!!.splitToSequence(",").map {
                Base64.getDecoder().decode(it).decodeToString()
            }.filter { it.isNotEmpty() }.toList().toTypedArray()
        )
        launcher.launch()
    }
}
