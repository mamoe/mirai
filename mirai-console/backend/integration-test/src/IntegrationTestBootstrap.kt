/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("IntegrationTestBootstrap")

package net.mamoe.console.integrationtest

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import net.mamoe.console.integrationtest.testpoints.DoNothingPoint
import net.mamoe.console.integrationtest.testpoints.MCITBSelfAssertions
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.ConsoleTerminalExperimentalApi
import net.mamoe.mirai.console.terminal.ConsoleTerminalSettings
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

/**
 * 入口点为 /test/MiraiConsoleRealTimeTestUnit.kt 并非此函数(文件),
 * 不要直接执行此函数
 */
@OptIn(ConsoleTerminalExperimentalApi::class)
public fun main() {
    // PRE CHECK
    kotlin.run {
        if (!System.getenv("MIRAI_CONSOLE_INTEGRATION_TEST").orEmpty().toBoolean()) {
            error("Don't launch IntegrationTestBootstrap directly. See /test/MiraiConsoleIntegrationTestBootstrap.kt")
        }
    }
    // @context: env.testunit = true
    // @context: env.inJUnitProcess = false
    // @context: env.exitProcessSafety = true
    // @context: process.type = sandbox
    // @context: process.cwd = /mirai-console/backend/build/rttu
    // @context: process.timeout = 5min

    ConsoleTerminalSettings.setupAnsi = false
    ConsoleTerminalSettings.noConsole = true

    val testUnits: List<AbstractTestPointAsPlugin> = listOf(
        DoNothingPoint,
        MCITBSelfAssertions,
    )

    File("plugins").mkdirs()
    prepareConsole()

    testUnits.forEach { it.generatePluginJar() }
    testUnits.forEach { it.beforeConsoleStartup() }

    MiraiConsoleTerminalLoader.startAsDaemon()

    if (!MiraiConsole.isActive) {
        error("Failed to start console")
    }

    // I/main: mirai-console started successfully.

    testUnits.forEach { it.onConsoleStartSuccessfully() }

    runBlocking {
        MiraiConsole.job.cancelAndJoin()
    }
    exitProcess(0)
}

private fun File.mkparents(): File = apply { parentFile?.mkdirs() }
private fun prepareConsole() {
    File("config/Console/Logger.yml").mkparents().writeText(
        """
defaultPriority: ALL
loggers: 
  Bot: ALL
"""
    )

    for (i in 0 until System.getenv("IT_PLUGINS")!!.toInt()) {
        val jarFile = File(System.getenv("IT_PLUGIN_$i"))
        val target = File("plugins/${jarFile.name}").mkparents()
        jarFile.copyTo(target, overwrite = true)
        println("[MCIT] Copied external plugin: $jarFile")
    }
}

private fun AbstractTestPointAsPlugin.generatePluginJar() {
    val simpleName = this.javaClass.simpleName
    val point = this
    val jarFile = File("plugins").resolve("$simpleName.jar")
    // PluginMainPoint: net.mamoe.console.integrationtestAbstractTestPointAsPlugin$TestPointPluginImpl
    jarFile.mkparents()
    ZipOutputStream(
        FileOutputStream(jarFile).buffered()
    ).use { zipOutputStream ->

        // META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin
        zipOutputStream.putNextEntry(
            ZipEntry(
                "META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin"
            )
        )
        val delegateClassName = "net.mamoe.console.integrationtest.tpd.$simpleName"
        zipOutputStream.write(delegateClassName.toByteArray())

        // MainClass
        val internalClassName = delegateClassName.replace('.', '/')
        zipOutputStream.putNextEntry(ZipEntry("$internalClassName.class"))
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val superName = "net/mamoe/console/integrationtest/AbstractTestPointAsPlugin\$TestPointPluginImpl"
        classWriter.visit(
            Opcodes.V1_8,
            Opcodes.ACC_PUBLIC,
            internalClassName,
            null,
            superName,
            null
        )
        classWriter.visitMethod(
            Opcodes.ACC_PUBLIC,
            "<init>", "()V", null, null
        )!!.let { initMethod ->
            initMethod.visitVarInsn(Opcodes.ALOAD, 0)
            initMethod.visitLdcInsn(Type.getType(point.javaClass))
            initMethod.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "(Ljava/lang/Class;)V", false)
            initMethod.visitInsn(Opcodes.RETURN)
            initMethod.visitMaxs(0, 0)
            initMethod.visitEnd()
        }

        zipOutputStream.write(classWriter.toByteArray())
    }
}
