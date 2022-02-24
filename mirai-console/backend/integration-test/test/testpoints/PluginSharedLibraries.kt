/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest.testpoints

import net.mamoe.console.integrationtest.AbstractTestPoint
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

internal object PluginSharedLibraries : AbstractTestPoint() {
    override fun beforeConsoleStartup() {
        if (System.getenv("CI").orEmpty().toBoolean()) {
            println("CI env")
            File("config/Console/PluginDependencies.yml").writeText(
                "repoLoc: ['https://repo.maven.apache.org/maven2']"
            )
        }
        File("plugin-shared-libraries").mkdirs()
        File("plugin-shared-libraries/libraries.txt").writeText(
            """
                io.github.karlatemp:unsafe-accessor:1.6.2
            """.trimIndent()
        )
        ZipOutputStream(File("plugin-shared-libraries/test.jar").outputStream().buffered()).use { zipOutput ->
            zipOutput.putNextEntry(ZipEntry("net/mamoe/console/it/psl/PluginSharedLib.class"))
            ClassWriter(0).also { writer ->
                writer.visit(
                    Opcodes.V1_8,
                    0,
                    "net/mamoe/console/it/psl/PluginSharedLib",
                    null,
                    "java/lang/Object",
                    null
                )
            }.toByteArray().let { zipOutput.write(it) }
        }
    }
}