/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("IntegrationTestBootstrap")
@file:OptIn(ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class, ConsoleInternalApi::class)

package net.mamoe.console.integrationtest

import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import net.mamoe.console.integrationtest.AbstractTestPoint.Companion.internalBCS
import net.mamoe.console.integrationtest.AbstractTestPoint.Companion.internalOSS
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.terminal.ConsoleTerminalExperimentalApi
import net.mamoe.mirai.console.terminal.ConsoleTerminalSettings
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ConsoleInternalApi
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.sha1
import net.mamoe.mirai.utils.toUHexString
import org.objectweb.asm.*
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

internal object IntegrationTestBootstrapContext {
    val failures = ConcurrentLinkedDeque<Class<*>>()
}

/**
 * 入口点为 /test/MiraiConsoleIntegrationTestBootstrap.kt 并非此函数(文件),
 * 不要直接执行此函数
 */
@OptIn(ConsoleTerminalExperimentalApi::class)
@PublishedApi
internal fun main() {
    // PRE CHECK
    run {
        if (!System.getenv("MIRAI_CONSOLE_INTEGRATION_TEST").orEmpty().toBoolean()) {
            error("Don't launch IntegrationTestBootstrap directly. See /test/MiraiConsoleIntegrationTestBootstrap.kt")
        }
    }
    System.setProperty("mirai.console.skip-end-user-readme", "")
    // @context: env.testunit = true
    // @context: env.inJUnitProcess = false
    // @context: env.exitProcessSafety = true
    // @context: process.type = sandbox
    // @context: process.cwd = /mirai-console/backend/build/rttu
    // @context: process.timeout = 5min

    ConsoleTerminalSettings.setupAnsi = false
    ConsoleTerminalSettings.noConsole = true
    ConsoleTerminalSettings.launchOptions.crashWhenPluginLoadFailed = true

    val testUnits: List<AbstractTestPoint> = readStringListFromEnv("IT_POINTS").asSequence()
        .onEach { println("[MCIT] Loading test point: $it") }
        .map { Class.forName(it) }
        .map {
            @Suppress("DEPRECATION")
            it.kotlin.objectInstance ?: it.newInstance()
        }
        .map { it.cast<AbstractTestPoint>() }
        .toList()

    File("plugins").mkdirs()
    File("modules").mkdirs()
    prepareConsole()

    testUnits.forEach { (it as? AbstractTestPointAsPlugin)?.generatePluginJar() }
    testUnits.forEach { it.internalBCS() }

    Thread.sleep(2000L)

    try {
        MiraiConsoleTerminalLoader.startAsDaemon()
    } catch (e: Throwable) {
        val ps = PrintStream(FileOutputStream(FileDescriptor.out))
        e.printStackTrace(ps)
        ps.flush()
        exitProcess(1)
    }
    if (!MiraiConsole.isActive) {
        error("Failed to start console")
    }
    if (IntegrationTestBootstrapContext.failures.isNotEmpty()) {
        val logger = MiraiConsole.mainLogger
        logger.error("Failed tests: ")
        IntegrationTestBootstrapContext.failures.toSet().forEach {
            logger.error("  `- $it")
        }
        error("Failed tests: ${IntegrationTestBootstrapContext.failures.toSet()}")
    }

    // I/main: mirai-console started successfully.

    testUnits.forEach { it.internalOSS() }

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
  org.eclipse.aether.internal: INFO
  org.apache.http.wire: INFO
"""
    )
    fun saveSha1(t: File) {
        val sfile = File(t.path + ".sha1")
        sfile.writeText(t.readBytes().sha1().toUHexString(separator = "").lowercase())
    }

    readStringListFromEnv("IT_PLUGINS").forEach plLoop@{ path ->
        val jarFile = File(path)
        ZipFile(jarFile).use { zipFile ->
            zipFile.getEntry("mvn.txt")?.let { zipFile.getInputStream(it) }?.bufferedReader()?.useLines { lines ->
                val libName = lines.filterNot { it.isBlank() }.filterNot { it[0] == '#' }.first()
                // net.mamoe:test:1.0.0
                val (gid, art, ver) = libName.split(':')
                val targetDir = File("plugin-libraries").resolve(gid.replace('.', '/'))
                    .resolve(art).resolve(ver).also { it.mkdirs() }
                val fname = "$art-$ver"
                val pom = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <project>
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>$gid</groupId>
                    <artifactId>$art</artifactId>
                    <version>$ver</version>
                    </project>
                """.trimIndent()
                targetDir.resolve("$fname.pom").let { it.writeText(pom); saveSha1(it) }
                targetDir.resolve("$fname.jar").let {
                    jarFile.copyTo(it, overwrite = true)
                    saveSha1(it)
                }
                targetDir.resolve("_remote.repositories").writeText(
                    """
                        $fname.pom>=
                        $fname.jar>=
                    """.trimIndent()
                )
                println("[MCIT] Copied module: $libName $jarFile -> $targetDir")
                return@plLoop
            }
            if (zipFile.getEntry("module.txt") == null) {
                if (zipFile.entries().asSequence().any {
                        it.name.contains("services/net.mamoe.mirai.console.plugin.jvm")
                    }
                ) {
                    var target = File("plugins/${jarFile.name}").mkparents()
                    var counter = 0
                    while (target.exists()) {
                        target = File("plugins/${jarFile.nameWithoutExtension}${counter}.${jarFile.extension}")
                        counter++
                    }
                    jarFile.copyTo(target, overwrite = true)
                    println("[MCIT] Copied external plugin: $jarFile -> $target")
                    return@plLoop
                }
            }
            // DYN MODULE
            val target = File("modules/${jarFile.name}").mkparents()
            jarFile.copyTo(target, overwrite = true)
            println("[MCIT] Copied module: $jarFile")
        }
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

        // region Copy class annotations
        this.javaClass.getResourceAsStream(javaClass.simpleName + ".class")!!.use {
            ClassReader(it)
        }.accept(object : ClassVisitor(Opcodes.ASM9) {
            override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor? {
                if ("kotlin/Metadata" in descriptor) return null
                return classWriter.visitAnnotation(descriptor, visible)
            }

            override fun visitTypeAnnotation(
                typeRef: Int,
                typePath: TypePath,
                descriptor: String,
                visible: Boolean
            ): AnnotationVisitor? {
                if ("kotlin/Metadata" in descriptor) return null
                return classWriter.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
            }
        }, ClassReader.SKIP_CODE)
        // endregion

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
