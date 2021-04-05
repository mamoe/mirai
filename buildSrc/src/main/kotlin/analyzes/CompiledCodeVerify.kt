/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package analyzes

import org.gradle.api.Project
import java.io.File

typealias VerifyAction = (classes: Sequence<File>, libraries: Sequence<File>) -> Unit

data class ProjectInfo(val isMpp: Boolean, val name: String)

fun JvmProjectInfo(name: String) = ProjectInfo(false, name)
fun MppProjectInfo(name: String) = ProjectInfo(true, name)

@Suppress("MemberVisibilityCanBePrivate")
object CompiledCodeVerify {

    private const val RUN_ALL_VERITY_TASK_NAME = "runAllVerify"
    private const val VERIFICATION_GROUP_NAME = "verification"

    private val projectInfos = listOf(
        MppProjectInfo("mirai-core-api"), MppProjectInfo("mirai-core-utils"),
        JvmProjectInfo("mirai-console"), JvmProjectInfo("mirai-console-terminal")
    ).associateBy { it.name }

    private val ProjectInfo.compileTasks: Array<String>
        get() = if (isMpp) {
            arrayOf(":$name:jvmMainClasses", ":$name:androidMainClasses")
        } else arrayOf(":$name:classes")

    private fun getCompiledClassesPath(project: Project, info: ProjectInfo): Sequence<Sequence<File>> =
        if (info.isMpp) {
            sequenceOf("kotlin/jvm/main", "kotlin/android/main")
        } else {
            sequenceOf("kotlin/main")
        }.map { sequenceOf(project.buildDir.resolve("classes").resolve(it)) }

    private fun getLibraries(project: Project, info: ProjectInfo): Sequence<Sequence<File>?> =
        if (info.isMpp) {
            sequenceOf("jvmCompileClasspath", "androidCompileClasspath")
        } else {
            sequenceOf("compileClasspath")
        }.map {
            project.configurations.findByName(it)?.files?.asSequence()
        }

    fun Project.registerVerifyTask(taskName: String, action: VerifyAction) {

        val projectInfo = projectInfos[this.name] ?: error("Project info of $name not found")

        tasks.register(taskName) {
            group = VERIFICATION_GROUP_NAME
            projectInfo.compileTasks.forEach {
                tasks.findByPath(it)?.also { compileTask ->
                    mustRunAfter(compileTask)
                }
            }

            doFirst {
                getCompiledClassesPath(project, projectInfo).zip(getLibraries(project, projectInfo))
                    .forEach { (compiledClasses, libraries) ->
                        if (libraries != null) {
                            action(compiledClasses, libraries)
                        }
                    }
            }
        }

        tasks.named("check").configure { dependsOn(taskName) }
        rootProject.tasks.getByName(RUN_ALL_VERITY_TASK_NAME).dependsOn(":$name:$taskName")
    }

    private fun Project.registerVerifyTasks() { // for feature extends
        // https://github.com/mamoe/mirai/pull/1080#issuecomment-801197312
        if (name != "mirai-console") {
            registerVerifyTask("verify_NoNoSuchMethodError", NoSuchMethodAnalyzer::check)
        }
    }

    fun Project/*RootProject*/.registerAllVerifyTasks() {
        tasks.register(RUN_ALL_VERITY_TASK_NAME) {
            group = VERIFICATION_GROUP_NAME
        }
        projectInfos.keys.forEach { projectName ->
            findProject(projectName)?.let { subProject ->
                subProject.afterEvaluate { subProject.registerVerifyTasks() }
            }
        }
    }

}