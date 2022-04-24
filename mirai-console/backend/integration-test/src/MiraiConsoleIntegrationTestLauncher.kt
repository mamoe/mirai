/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

import net.mamoe.mirai.utils.lateinitMutableProperty
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

// TODO: 不完整, 还无法完全公开, 目前仅允许 console 内部使用
/**
 * MiraiConsoleIntegrationTest 启动器
 */
public class MiraiConsoleIntegrationTestLauncher {
    /** java.exe 路径 */
    public var javaexec: String by lateinitMutableProperty { findJavaExec() }

    /**
     * 测试环境运行目录, **每次启动前会直接删除该文件夹的内容**(IMPORTANT)
     */
    public var workingDir: File = File("mirai-console-integration-test")

    /** 额外 JVM 参数 */
    public var vmoptions: MutableList<String> = mutableListOf()

    /** 额外环境变量 */
    public var extraEnvironment: MutableMap<String, String> = mutableMapOf()

    /** 类路径, 需要包含 MiraiConsoleIntegrationTest Framework */
    public var classpath: String by lateinitMutableProperty { ManagementFactory.getRuntimeMXBean().classPath }

    /** 标准输出重定向位置 */
    public var output: OutputStream = System.out
    /** 标准错误重定向位置 */
    public var error: OutputStream = System.err
    /** [MiraiConsoleIntegrationTestLauncher] 启动日志的输出 */
    public var log: PrintStream = System.out

    /** 测试单元完整类名, 需要可以在 [classpath] 中找到 */
    public var points: MutableCollection<String> = mutableListOf()
    /** 测试环境的额外插件, 为文件路径, 相对于 [workingDir] */
    public var plugins: MutableCollection<String> = mutableListOf()

    public fun launch() {
        workingDir.listFiles()?.filterNot { it.name.contains("libraries") }
            ?.forEach { it.deleteRecursively() } // do not download repeatedly
        workingDir.mkdirs()
        val isDebugging = vmoptions.any { it.startsWith("-agentlib:") }

        val builder = ProcessBuilder(
            javaexec,
            *vmoptions.toTypedArray(),
            "-cp", classpath,
            "net.mamoe.console.integrationtest.IntegrationTestBootstrap",
        )
            .directory(workingDir)
        // .inheritIO() // No output in idea
        val env = builder.environment()
        env.putAll(extraEnvironment)
        env["MIRAI_CONSOLE_INTEGRATION_TEST"] = "true"
        saveStringListToEnv("IT_PLUGINS", plugins, env)
        saveStringListToEnv("IT_POINTS", points, env)

        log.println("[MCIT] Launching IntegrationTest")
        log.println("[MCIT]    `- Arguments: ${builder.command().joinToString(" ")}")
        log.println("[MCIT]    `- Directory: ${builder.directory().absoluteFile}")
        log.println("[MCIT]    `- Debugging: $isDebugging")
        if (isDebugging) {
            log.println("[MCIT] Running in debug mode. Watchdog thread will not start")
        }

        val process = builder.start()

        val timedOut = AtomicBoolean(false)
        val watchdog = thread {
            if (isDebugging) return@thread
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(5))
                timedOut.set(true)
                process.destroyForcibly()
            } catch (ignored: InterruptedException) {
            }
        }

        thread { process.inputStream.copyTo(output) }
        thread { process.errorStream.copyTo(error) }

        val rsp = process.waitFor()
        if (timedOut.get()) {
            error("Mirai console daemon timed out")
        }
        watchdog.interrupt()
        if (rsp != 0) error("Rsp $rsp")
    }
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
