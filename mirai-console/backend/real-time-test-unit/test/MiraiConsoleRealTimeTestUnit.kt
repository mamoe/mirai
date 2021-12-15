/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.rttu

import org.junit.jupiter.api.Test
import java.io.File
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


class MiraiConsoleRealTimeTestUnit {
    @Test
    fun bootstrap() {
        /*
        implementation note:
        不使用 @TempDir 是为了保存最后一次失败快照, 便于 debug
         */
        val workingDir = File("build/rttu") // mirai-console/backend/rttu/build/trru
        workingDir.deleteRecursively()
        workingDir.mkdirs()

        val builder = ProcessBuilder(
            findJavaExec().absolutePath,
            "-cp", ManagementFactory.getRuntimeMXBean().classPath,
            "net.mamoe.console.rttu.RealTimeTestUnitBootstrap",
        )
            .directory(workingDir)
        // .inheritIO() // No output in idea

        val process = builder.start()

        val timedOut = AtomicBoolean(false)
        val watchcat = thread {
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

    private fun findJavaExec(): File {
        val ext = if ("windows" in System.getProperty("os.name").lowercase()) {
            ".exe"
        } else ""

        val javaHome = File(System.getProperty("java.home"))
        javaHome.resolve("bin/java$ext").takeIf { it.exists() }?.let { return it }
        javaHome.resolve("java$ext").takeIf { it.exists() }?.let { return it }


        javaHome.resolve("jre/bin/java$ext").takeIf { it.exists() }?.let { return it }
        javaHome.resolve("jre/java$ext").takeIf { it.exists() }?.let { return it }

        error("java executable for current process not found.")
    }

}
