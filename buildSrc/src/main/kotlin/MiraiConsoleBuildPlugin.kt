/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.attributes
import org.gradle.kotlin.dsl.getByName
import java.io.File

class MiraiConsoleBuildPlugin : Plugin<Project> {
    override fun apply(target: Project) = target.run {
        apply<ShadowPlugin>()
        val ext = target.extensions.getByName("ext") as org.gradle.api.plugins.ExtraPropertiesExtension

        if (tasks.none { it.name == "shadowJar" }) {
            return@run
        }

        tasks.getByName("shadowJar") {
            with(this as ShadowJar) {
                archiveFileName.set(
                    "${target.name}-${target.version}-all.jar"
                )
                manifest {
                    attributes(
                        "Manifest-Version" to "1",
                        "Implementation-Vendor" to "Mamoe Technologies",
                        "Implementation-Title" to target.name.toString(),
                        "Implementation-Version" to target.version.toString() + "+" + gitVersion
                    )
                }
                @Suppress("UNCHECKED_CAST")
                kotlin.runCatching {
                    (ext["shadowJar"] as? ShadowJar.() -> Unit)?.invoke(this)
                }
            }
        }

        tasks.create("githubUpload") {
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
                            project,
                            "mirai-repo",
                            "shadow/${project.name}/${filename.replace("-all", "")}"
                        )
                    }.exceptionOrNull()?.let {
                        System.err.println("GitHub Upload failed")
                        it.printStackTrace() // force show stacktrace
                        throw it
                    }
                }
            }
        }

        tasks.create("cuiCloudUpload") {
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

fun Project.findLatestFile(): Pair<String, File> {
    return tasks.getByName("shadowJar", ShadowJar::class).run {
        val file = archiveFile.get().asFile
        this@findLatestFile.version.toString() to file
    }/*
    return File(projectDir, "build/libs").walk()
        .filter { it.isFile }
        .onEach { println("all files=$it") }
        .filter { it.name.matches(Regex("""${project.name}-[0-9][0-9]*(\.[0-9]*)*.*\-all\.jar""")) }
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
        } ?: error("cannot find any file to upload")*/
}

val gitVersion: String by lazy {
    runCatching {
        val exec = Runtime.getRuntime().exec("git rev-parse HEAD")
        exec.waitFor()
        exec.inputStream.readBytes().toString(Charsets.UTF_8).trim().also {
            println("Git commit id: $it")
        }
    }.onFailure {
        it.printStackTrace()
        return@lazy "UNKNOWN"
    }.getOrThrow()
}
