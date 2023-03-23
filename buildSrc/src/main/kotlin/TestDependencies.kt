/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

/**
 * 配置 test 依赖 mirai-core:jvmTest, 可访问 `AbstractTest` 等
 * 
 * 用法:
 * 
 * *build.gradle.kts*
 * ```
 * kotlin.sourceSets.test {
 *     dependsOnCoreJvmTest(project)
 * }
 * ```
 */
fun KotlinSourceSet.dependsOnCoreJvmTest(project: Project) {
    project.evaluationDependsOn(":mirai-core")
    dependencies {
        implementation(
            project(":mirai-core").dependencyProject.kotlinMpp!!.targets
                .single { it.name == "jvm" }
                .compilations.getByName("test")
                .output.allOutputs
        )
        implementation(
            project(":mirai-core").dependencyProject.kotlinMpp!!.targets
                .single { it.name == "jvmBase" }
                .compilations.getByName("test")
                .output.allOutputs
        )
    }
}