/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

import net.mamoe.console.integrationtest.testpoints.MCITBSelfAssertions
import org.objectweb.asm.ClassReader
import java.io.File
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertTrue


class MiraiConsoleIntegrationTestBootstrap {
    @Test
    fun bootstrap() {
        /*
        implementation note:
        不使用 @TempDir 是为了保存最后一次失败快照, 便于 debug
         */
        val workingDir = File("build/IntegrationTest") // mirai-console/backend/integration-test/build/IntegrationTest
        // 注意: 如果更改 workingDir, 还要更改 mirai-console/backend/integration-test/build.gradle.kts:60 (clean task 的依赖)
        val launcher = MiraiConsoleIntegrationTestLauncher()
        launcher.workingDir = workingDir
        launcher.plugins = readStringListFromEnv("IT_PLUGINS")
        launcher.points = resolveTestPoints().also { points ->
            // Avoid error in resolving points
            assertTrue { points.contains("net.mamoe.console.integrationtest.testpoints.MCITBSelfAssertions") }
            assertTrue { points.contains("net.mamoe.console.integrationtest.testpoints.DoNothingPoint") }
            assertTrue { points.contains("net.mamoe.console.integrationtest.testpoints.plugin.PluginDataRenameToIdTest") }
        }.asSequence().map { v ->
            when (v) {
                is Class<*> -> v.name
                is KClass<*> -> v.java.name
                is String -> v
                else -> v.javaClass.name
            }
        }.map { it.replace('/', '.') }.toMutableList()
        launcher.vmoptions = mutableListOf(
            *ManagementFactory.getRuntimeMXBean().inputArguments.filterNot {
                it.startsWith("-Djava.security.manager=")
            }.filterNot {
                it.startsWith("-Xmx")
            }.toTypedArray(),
            *System.getenv("IT_ARGS")!!.splitToSequence(",").map {
                Base64.getDecoder().decode(it).decodeToString()
            }.filter { it.isNotEmpty() }.toList().toTypedArray()
        )
        launcher.launch()
    }

    private fun resolveTestPoints(): Collection<Any> {
        val ptloc = MCITBSelfAssertions.javaClass.getResource(MCITBSelfAssertions.javaClass.simpleName + ".class")
        ptloc ?: error("Failed to resolve test points")
        val path = Paths.get(ptloc.toURI()).parent
        return Files.walk(path)
            .filter { !it.isDirectory() }
            .filter { it.name.endsWith(".class") }
            .map { pt ->
                pt.inputStream().use {
                    ClassReader(it).className
                }
            }
            .map { it.replace('/', '.') }
            .filter { AbstractTestPoint::class.java.isAssignableFrom(Class.forName(it)) }
            .use { it.collect(Collectors.toList()) }
    }
}
