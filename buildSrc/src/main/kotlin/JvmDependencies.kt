/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */


@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo

@Suppress("unused")
fun DependencyHandler.compileAndTestRuntime(any: Any) {
    add("compileOnly", any)
    add("testImplementation", any)
}

fun DependencyHandler.smartApi(
    dependencyNotation: String
): ExternalModuleDependency {
    return smart("api", dependencyNotation)
}

fun DependencyHandler.smartImplementation(
    dependencyNotation: String
): ExternalModuleDependency {
    return smart("implementation", dependencyNotation)
}

private fun DependencyHandler.smart(
    configuration: String,
    dependencyNotation: String
): ExternalModuleDependency {
    return addDependencyTo(
        this, configuration, dependencyNotation
    ) {
        fun exclude(group: String, module: String) {
            exclude(
                mapOf(
                    "group" to group,
                    "module" to module
                )
            )
        }
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-common")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
        exclude("org.jetbrains.kotlinx", "kotlinx-serialization-common")
        exclude("org.jetbrains.kotlinx", "kotlinx-serialization-core")
    }
}
