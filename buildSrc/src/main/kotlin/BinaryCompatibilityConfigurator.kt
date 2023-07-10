/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.Project
import org.gradle.configurationcache.extensions.useToRun
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
    fun Project.configureBinaryValidators(targetNames: Set<String>) {
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
            val validatorProject = findProject(getValidatorDir(dir))
            validatorProject?.afterEvaluate {
                tasks.getByName("apiDump").let { apiDumpAll.dependsOn(it) }
            }

            validatorProject?.afterEvaluate {
                tasks.getByName("apiCheck").let { apiCheckAll.dependsOn(it) }
            }

        }
    }

    // Also change: settings.gradle.kts:116
    private fun Project.getValidatorDir(dir: File) = ":validator" + project.path + "-validator:${dir.name}"

    private fun File.writeTextIfNeeded(text: String) {
        if (!this.exists()) return this.writeText(text)
        if (this.readText() == text) return
        return this.writeText(text)
    }

    /**
     * @param targetName `null` for JVM projects.
     */
    fun createValidator(project: Project, dir: File, targetName: String?) {
        dir.resolve("build.gradle.kts").writeTextIfNeeded(
            applyTemplate(
                project.path,
                listOfNotNull(
                    if (targetName == null) "classes/kotlin/main" else "classes/kotlin/$targetName/main",
                    if (targetName?.contains("android") == true && project.usingAndroidInstrumentedTests) "tmp/kotlin-classes/debug" else ""
                )
            )
        )
        dir.resolve(".gitignore").writeTextIfNeeded(
            this::class.java.classLoader
                .getResourceAsStream("binary-compatibility-validator-ignore.txt")!!.readBytes().decodeToString()
        )

        project.afterEvaluate {
            findProject(getValidatorDir(dir))
                ?.afterEvaluate {
                    if (targetName == null) {
                        tasks.findByName("apiBuild")?.dependsOn(
                            *listOfNotNull(
                                project.tasks.getByName("jar"),
                                project.tasks.findByName("compileDebugKotlinAndroid")
                            ).toTypedArray()
                        )
                    } else {
                        tasks.findByName("apiBuild")?.dependsOn(
                            if (targetName.contains("android") && ENABLE_ANDROID_INSTRUMENTED_TESTS) {
                                project.tasks.getByName("bundleDebugAar")
                            } else {
                                project.tasks.getByName("${targetName}Jar")
                            }
                        )
                    }
                }
        }
    }

    fun applyTemplate(projectPath: String, buildDirs: List<String>): String {
        return this::class.java.classLoader
            .getResourceAsStream("binary-compatibility-validator-build.txt")!!
            .useToRun { readBytes() }
            .decodeToString()
            .replace("$\$PROJECT_PATH$$", projectPath)
            .replace("$\$BUILD_DIR$$", buildDirs.joinToString("\n"))
            .replace("$\$PLUGIN_VERSION$$", Versions.binaryValidator)
    }
}
