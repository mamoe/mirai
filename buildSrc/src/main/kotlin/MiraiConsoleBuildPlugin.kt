/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.creating
import java.io.File
import kotlin.math.pow

class MiraiConsoleBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.run {
        apply<ShadowPlugin>()

        if (tasks.none { it.name == "shadowJar" }) {
            return@run
        }

        tasks.getByName("shadowJar") {
            doLast {
                this.outputs.files.forEach {
                    if (it.nameWithoutExtension.endsWith("-all")) {
                        val output = File(
                            it.path.substringBeforeLast(File.separator) + File.separator + it.nameWithoutExtension.substringBeforeLast(
                                "-all"
                            ) + "." + it.extension
                        )

                        println("Renaming to ${output.path}")
                        if (output.exists()) {
                            output.delete()
                        }

                        it.renameTo(output)
                    }
                }
            }
        }

        tasks.creating {
            group = "mirai"
            dependsOn(tasks.getByName("shadowJar"))

            doFirst {
                timeout.set(java.time.Duration.ofHours(3))
                findLatestFile().let { (_, file) ->
                    val filename = file.name
                    println("Uploading file $filename")
                    runCatching {
                        upload.GitHub.upload(
                            file,
                            "https://api.github.com/repos/mamoe/mirai-repo/contents/shadow/${project.name}/$filename",
                            project
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("GitHub Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }
        }

        tasks.creating {
            group = "mirai"
            dependsOn(tasks.getByName("shadowJar"))

            doFirst {
                timeout.set(java.time.Duration.ofHours(3))
                findLatestFile().let { (_, file) ->
                    val filename = file.name
                    println("Uploading file $filename")
                    runCatching {
                        upload.CuiCloud.upload(
                            file,
                            project
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("CuiCloud Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }

        }
    }
}

fun Project.findLatestFile(): Map.Entry<String, File> {
    return File(projectDir, "build/libs").walk()
        .filter { it.isFile }
        .onEach { println("all files=$it") }
        .filter { it.name.matches(Regex("""${project.name}-[0-9][0-9]*(\.[0-9]*)*.*\.jar""")) }
        .onEach { println("matched file: ${it.name}") }
        .associateBy { it.nameWithoutExtension.substringAfterLast('-') }
        .onEach { println("versions: $it") }
        .maxBy { (version, _) ->
            version.split('.').let {
                if (it.size == 2) it + "0"
                else it
            }.reversed().foldIndexed(0) { index: Int, acc: Int, s: String ->
                acc + 100.0.pow(index).toInt() * (s.toIntOrNull() ?: 0)
            }
        } ?: error("cannot find any file to upload")
}
