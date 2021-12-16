/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

import org.junit.jupiter.api.Test
import java.io.File
import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


class MiraiConsoleIntegrationTestBootstrap {
    @Test
    fun bootstrap() {
        /*
        implementation note:
        不使用 @TempDir 是为了保存最后一次失败快照, 便于 debug
         */
        val workingDir = File("build/IntegrationTest") // mirai-console/backend/integration-test/build/IntegrationTest
        workingDir.deleteRecursively()
        workingDir.mkdirs()
        var isDebugging = false

        val builder = ProcessBuilder(
            findJavaExec(),
            *ManagementFactory.getRuntimeMXBean().inputArguments.filterNot {
                it.startsWith("-Djava.security.manager=")
            }.toTypedArray(),
            *System.getenv("IT_ARGS")!!.splitToSequence(",").map {
                Base64.getDecoder().decode(it).decodeToString()
            }.onEach { arg0 ->
                if (arg0.startsWith("-agentlib:")) {
                    isDebugging = true
                }
            }.filter { it.isNotEmpty() }.toList().toTypedArray(),
            "-cp", ManagementFactory.getRuntimeMXBean().classPath,
            "net.mamoe.console.integrationtest.IntegrationTestBootstrap",
        )
            .directory(workingDir)
        // .inheritIO() // No output in idea

        builder.environment()["MIRAI_CONSOLE_INTEGRATION_TEST"] = "true"

        println("[MCIT] Launching IntegrationTest")
        println("[MCIT]    `- Arguments: ${builder.command().joinToString(" ")}")
        println("[MCIT]    `- Directory: ${builder.directory().absoluteFile}")
        println("[MCIT]    `- Debugging: $isDebugging")
        if (isDebugging) {
            println("[MCIT] Running in debug mode. Watchdog thread will not start")
        }

        val process = builder.start()

        val timedOut = AtomicBoolean(false)
        val watchcat = thread {
            if (isDebugging) return@thread
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(5))
                timedOut.set(true)
                process.destroyForcibly()
            } catch (ignored: InterruptedException) {
            }
        }

        thread { process.inputStream.copyTo(System.out);println("Stdout copy end") }
        thread { process.errorStream.copyTo(System.err);println("Stderr copy end") }

        val rsp = process.waitFor()
        if (timedOut.get()) {
            error("Mirai console daemon timed out")
        }
        watchcat.interrupt()
        if (rsp != 0) error("Rsp $rsp")
    }

    private fun findJavaExec(): String {
        findJavaExec0()?.let { return it.absolutePath }
        System.err.println("[MCIT] WARNING: Unable to determine the current runtime executable path.")
        System.err.println("[MCIT] WARNING: Using default executable to launch test unit")
        return "java"
    }

    private fun findJavaExec0(): File? {
        val ext = if ("windows" in System.getProperty("os.name").lowercase()) {
            ".exe"
        } else ""

        val javaHome = File(System.getProperty("java.home"))
        javaHome.resolve("bin/java$ext").takeIf { it.exists() }?.let { return it }
        javaHome.resolve("java$ext").takeIf { it.exists() }?.let { return it }


        javaHome.resolve("jre/bin/java$ext").takeIf { it.exists() }?.let { return it }
        javaHome.resolve("jre/java$ext").takeIf { it.exists() }?.let { return it }

        return null
    }

}
