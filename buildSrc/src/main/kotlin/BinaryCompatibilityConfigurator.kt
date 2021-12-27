import org.gradle.api.Project
import org.gradle.configurationcache.extensions.useToRun
import org.gradle.kotlin.dsl.get
import java.io.File

/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

object BinaryCompatibilityConfigurator {
    fun Project.configureBinaryValidators(vararg targetNames: String) {
        targetNames.forEach { configureBinaryValidator(it) }
    }


    fun Project.configureBinaryValidator(targetName: String?) {
        val validationDir = projectDir.resolve("compatibility-validation")

        val dir = validationDir.resolve(targetName ?: "jvm")
        dir.mkdirs()
        createValidator(this, dir, targetName)
        val apiDumpAll = tasks.maybeCreate("apiDumpAll").apply {
            group = "mirai"
        }
        val apiCheckAll = tasks.maybeCreate("apiCheckAll").apply {
            group = "mirai"
        }

        project.afterEvaluate {
            val validatorProject = findProject(project.path + ":validator-${dir.name}")
            validatorProject
                ?.getTasksByName("apiDump", false)
                ?.let { apiDumpAll.dependsOn(it) }

            validatorProject
                ?.getTasksByName("apiCheck", false)
                ?.let { apiCheckAll.dependsOn(it) }
        }
    }

    /**
     * @param targetName `null` for JVM projects.
     */
    fun createValidator(project: Project, dir: File, targetName: String?) {
        dir.resolve("build.gradle.kts").writeText(
            applyTemplate(
                project.path,
                if (targetName == null) "classes/kotlin/main" else "classes/kotlin/$targetName/main"
            )
        )
        dir.resolve(".gitignore").writeText(
            this::class.java.classLoader
                .getResourceAsStream("binary-compatibility-validator-ignore.txt")!!.readBytes().decodeToString()
        )

        project.afterEvaluate {
            findProject(project.path + ":validator-${dir.name}")
                ?.afterEvaluate {
                    tasks["apiBuild"].dependsOn(project.tasks["build"])
                }
        }
    }

    fun applyTemplate(projectPath: String, buildDir: String): String {
        return this::class.java.classLoader
            .getResourceAsStream("binary-compatibility-validator-build.txt")!!
            .useToRun { readBytes() }
            .decodeToString()
            .replace("$\$PROJECT_PATH$$", projectPath)
            .replace("$\$BUILD_DIR$$", buildDir)
            .replace("$\$PLUGIN_VERSION$$", Versions.binaryValidator)
    }
}