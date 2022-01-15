/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
                    dependencies.add(dep.moduleGroup + ":" + dep.moduleName)
                    dep.children.forEach { emit(it) }
                }
                project.configurations.getByName(confName).resolvedConfiguration.firstLevelModuleDependencies.forEach { dependency ->
                    emit(dependency)
                }
                val stdep = dependencies.toMutableList()
                stdep.sort()
                action(stdep)
            }
        }
    }

}
