/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.TaskProvider
import java.io.File
import java.util.zip.ZipFile

object DependencyDumper {
    fun registerDumpTask(project: Project, confName: String, out: File): TaskProvider<Task> {
        return regDmpTask(project, confName) { deps ->
            deps.forEach { println("  `- $it") }
            out.writeText(deps.joinToString("\n", postfix = "\n"))
        }
    }

    fun registerDumpTaskKtSrc(project: Project, confName: String, out: File, className: String): TaskProvider<Task> {
        val pkgName = className.substringBeforeLast(".")
        val kname = className.substringAfterLast(".")
        return regDmpTask(project, confName) { deps ->
            out.printWriter().use { pr ->
                pr.println("package $pkgName")
                pr.println()
                pr.println("internal object $kname {")
                pr.println("    val dependencies: List<String> = listOf(")
                deps.forEach { dependency ->
                    pr.append("      \"").append(dependency).println("\",")
                }
                pr.println("    )")
                pr.println("}")
            }
        }
    }

    private fun regDmpTask(project: Project, confName: String, action: (List<String>) -> Unit): TaskProvider<Task> {
        val dependenciesDump = project.tasks.maybeCreate("dependenciesDump")
        dependenciesDump.group = "mirai"
        return project.tasks.register("dependenciesDump_${confName.capitalize()}") {
            group = "mirai"
            doLast {
                val dependencies = HashSet<String>()
                fun emit(dep: ResolvedDependency) {
                    if (dependencies.add(dep.moduleGroup + ":" + dep.moduleName)) {
                        dep.children.forEach { emit(it) }
                    }
                }
                project.configurations.getByName(confName).resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
                    emit(dependency)
                }
                val stdep = dependencies.toMutableList()
                stdep.sort()
                action(stdep)
            }
        }.also { dependenciesDump.dependsOn(it) }
    }

    fun registerDumpClassGraph(project: Project, confName: String, out: String): TaskProvider<Task> {
        val dependenciesDump = project.tasks.maybeCreate("dependenciesDump")
        dependenciesDump.group = "mirai"
        return project.tasks.register("dependenciesDumpGraph_${confName.capitalize()}") {
            group = "mirai"
            val outFile = temporaryDir.resolve(out)
            outputs.file(outFile)
            val conf = project.configurations.getByName(confName)

            doLast {
                outFile.parentFile.mkdirs()

                val classes = HashSet<String>()
                conf.resolvedConfiguration.files.forEach { file ->
                    if (file.isFile) {
                        ZipFile(file).use { zipFile ->
                            zipFile.entries().asSequence()
                                .filter { it.name.endsWith(".class") }
                                .filterNot { it.name.startsWith("META-INF") }
                                .map { it.name.substringBeforeLast('.').replace('/', '.') }
                                .map { it.removePrefix(".") }
                                .forEach { classes.add(it) }
                        }
                    } else if (file.isDirectory) {
                        file.walk()
                            .filter { it.isFile }
                            .filter { it.name.endsWith(".class") }
                            .map { it.relativeTo(file).path.substringBeforeLast('.') }
                            .map { it.replace('\\', '.').replace('/', '.') }
                            .map { it.removePrefix(".") }
                            .forEach { classes.add(it) }
                    }
                }
                outFile.bufferedWriter().use { writer ->
                    classes.sorted().forEach { writer.append(it).append('\n') }
                }
            }
        }.also { dependenciesDump.dependsOn(it) }
    }
}
