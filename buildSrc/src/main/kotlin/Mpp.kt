/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySubstitutions
import org.gradle.api.artifacts.ResolutionStrategy
import org.gradle.api.artifacts.component.ComponentSelector
import org.gradle.api.plugins.ExtensionAware
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

val <T> NamedDomainObjectCollection<T>.androidMain: NamedDomainObjectProvider<T>
    get() = named("androidMain")

val <T> NamedDomainObjectCollection<T>.jvmMain: NamedDomainObjectProvider<T>
    get() = named("jvmMain")

val <T> NamedDomainObjectCollection<T>.androidTest: NamedDomainObjectProvider<T>
    get() = named("androidTest")

val <T> NamedDomainObjectCollection<T>.jvmTest: NamedDomainObjectProvider<T>
    get() = named("jvmTest")

val <T> NamedDomainObjectCollection<T>.commonMain: NamedDomainObjectProvider<T>
    get() = named("commonMain")


inline fun forMppModules(action: (suffix: String) -> Unit) {
    arrayOf(
        "",
        "-common",
        "-metadata",
        "-jvm",
        "-jdk7",
        "-jdk8"
    ).forEach(action)
}

fun Project.substituteDependenciesUsingExpectedVersion() {
    configurations.all {
        resolutionStrategy.substituteDependencies {
            forMppModules { suffix ->
                module("org.jetbrains.kotlin:kotlin-stdlib$suffix") using module("org.jetbrains.kotlin:kotlin-stdlib$suffix:${Versions.kotlinStdlib}")
                module("org.jetbrains.kotlin:kotlin-reflect$suffix") using module("org.jetbrains.kotlin:kotlin-reflect$suffix:${Versions.kotlinStdlib}")
                module("org.jetbrains.kotlinx:kotlinx-coroutines-core$suffix") using
                        module(kotlinx("coroutines-core$suffix", Versions.coroutines))
                module("org.jetbrains.kotlinx:kotlinx-coroutines-debug$suffix") using
                        module(kotlinx("coroutines-debug$suffix", Versions.coroutines))
            }
        }
    }
}

class ResolutionStrategyDsl(
    private val origin: DependencySubstitutions
) : DependencySubstitutions by origin {
    infix fun ComponentSelector.using(notation: ComponentSelector): DependencySubstitutions.Substitution {
        return substitute(this).using(notation)
    }
}

fun ResolutionStrategy.substituteDependencies(action: ResolutionStrategyDsl.() -> Unit) {
    dependencySubstitution {
        action(ResolutionStrategyDsl(this))
    }
}


val Project.kotlinMpp
    get() = runCatching {
        (this as ExtensionAware).extensions.getByName("kotlin") as? KotlinMultiplatformExtension
    }.getOrNull()


val Project.kotlinJvm
    get() = runCatching {
        (this as ExtensionAware).extensions.getByName("kotlin") as? KotlinJvmProjectExtension
    }.getOrNull()

