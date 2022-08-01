/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinSingleTargetExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile


fun Project.useIr() {
    kotlinCompilations?.forEach { kotlinCompilation ->
        kotlinCompilation.kotlinOptions.freeCompilerArgs += "-Xuse-ir"
    }
}

private fun Project.jvmVersion(): JavaVersion {
    return if (project.path.endsWith("mirai-console-intellij")) {
        JavaVersion.VERSION_11
    } else {
        JavaVersion.VERSION_1_8
    }
}

fun Project.preConfigureJvmTarget() {
    val defaultVer = jvmVersion()

    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions.jvmTarget = defaultVer.toString()
        kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"

        // Support for parallel compilation: https://youtrack.jetbrains.com/issue/KT-46085
        // Using /2 processors: jvm and android targets are compiled at the same time, sharing the processors.
        // Also reserved 2 processors for Gradle multi-tasking
        // On Apple M1 Max parallelism reduces compilation time by 1/3.
//        kotlinOptions.freeCompilerArgs += "-Xbackend-threads=" + (Runtime.getRuntime().availableProcessors() / 2 - 1).coerceAtLeast(1)
    }

    tasks.withType(JavaCompile::class.java) {
        sourceCompatibility = defaultVer.toString()
        targetCompatibility = defaultVer.toString()
    }
}

fun Project.configureJvmTarget() {
    val defaultVer = jvmVersion()

    configure(kotlinSourceSets.orEmpty()) {
        languageSettings {
            optIn("net.mamoe.mirai.utils.TestOnly")
            optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }
    }

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = defaultVer
        targetCompatibility = defaultVer
    }

    kotlinTargets.orEmpty().filterIsInstance<KotlinJvmTarget>().forEach { target ->
        when (target.attributes.getAttribute(KotlinPlatformType.attribute)) { // mirai does magic, don't use target.platformType
            KotlinPlatformType.androidJvm -> {
                target.compilations.all {
                    /*
                     * Kotlin JVM compiler generates Long.hashCode witch is available since API 26 when targeting JVM 1.8 while IR prefer member function hashCode always.
                     */
                    // kotlinOptions.useIR = true

                    // IR cannot compile mirai. We'll wait for Kotlin 1.5 for stable IR release.
                }
            }
            else -> {
            }
        }
        target.testRuns["test"].executionTask.configure { useJUnitPlatform() }
    }
}

fun Project.configureEncoding() {
    tasks.withType(JavaCompile::class.java) {
        options.encoding = "UTF8"
    }
}

fun Project.configureKotlinTestSettings() {
    tasks.withType(Test::class) {
        useJUnitPlatform()
    }
    val b = "Auto-set for project '${project.path}'. (configureKotlinTestSettings)"
    when {
        isKotlinJvmProject -> {
            dependencies {
                "testImplementation"(kotlin("test-junit5"))?.because(b)

                "testApi"(`junit-jupiter-api`)?.because(b)
                "testRuntimeOnly"(`junit-jupiter-engine`)?.because(b)
            }
        }
        isKotlinMpp -> {
            kotlinSourceSets?.forEach { sourceSet ->
                fun configureJvmTest(sourceSet: KotlinSourceSet) {
                    sourceSet.dependencies {
                        implementation(kotlin("test-junit5"))?.because(b)

                        implementation(`junit-jupiter-api`)?.because(b)
                        runtimeOnly(`junit-jupiter-engine`)?.because(b)
                    }
                }

                val target = kotlinTargets.orEmpty()
                    .find { it.name == sourceSet.name.substringBeforeLast("Main").substringBeforeLast("Test") }

                when {
                    sourceSet.name == "commonTest" -> {
                        if (isJvmLikePlatform(target)) {
                            configureJvmTest(sourceSet)
                        } else {
                            sourceSet.dependencies {
                                implementation(kotlin("test"))?.because(b)
                                implementation(kotlin("test-annotations-common"))?.because(b)
                            }
                        }
                    }
                    sourceSet.name.contains("test", ignoreCase = true) -> {
                        if (isJvmLikePlatform(target)) {
                            configureJvmTest(sourceSet)
                        }
                    }
                }
            }
        }
    }
}

private fun isJvmLikePlatform(target: KotlinTarget?) =
    target?.platformType == KotlinPlatformType.jvm || target?.platformType == KotlinPlatformType.androidJvm

val testExperimentalAnnotations = arrayOf(
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.time.ExperimentalTime",
    "io.ktor.util.KtorExperimentalAPI",
    "kotlin.io.path.ExperimentalPathApi"
)

val experimentalAnnotations = arrayOf(
    "kotlin.RequiresOptIn",
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlin.ExperimentalUnsignedTypes",

    "kotlinx.serialization.ExperimentalSerializationApi",

    "net.mamoe.mirai.utils.MiraiInternalApi",
    "net.mamoe.mirai.utils.MiraiExperimentalApi",
    "net.mamoe.mirai.LowLevelApi",

    "net.mamoe.mirai.console.ConsoleFrontEndImplementation",
    "net.mamoe.mirai.console.util.ConsoleInternalApi",
    "net.mamoe.mirai.console.util.ConsoleExperimentalApi",

    "io.ktor.utils.io.core.internal.DangerousInternalIoApi"
)

fun Project.configureKotlinExperimentalUsages() {
    val sourceSets = kotlinSourceSets ?: return

    for (target in sourceSets) {
        target.configureKotlinExperimentalUsages()
    }
}

fun KotlinSourceSet.configureKotlinExperimentalUsages() {
    languageSettings.progressiveMode = true
    experimentalAnnotations.forEach { a ->
        languageSettings.optIn(a)
    }
    if (name.contains("test", ignoreCase = true)) {
        testExperimentalAnnotations.forEach { a ->
            languageSettings.optIn(a)
        }
    }
}

fun Project.configureFlattenSourceSets() {
    val flatten = extra.runCatching { get("flatten.sourceset") }.getOrNull()?.toString()?.toBoolean() ?: true
    if (!flatten) return
    sourceSets {
        findByName("main")?.apply {
            resources.setSrcDirs(listOf(projectDir.resolve("resources")))
            java.setSrcDirs(listOf(projectDir.resolve("src")))
        }
        findByName("test")?.apply {
            resources.setSrcDirs(listOf(projectDir.resolve("resources")))
            java.setSrcDirs(listOf(projectDir.resolve("test")))
        }
    }
}

fun Project.configureJarManifest() {
    this.tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Vendor" to "Mamoe Technologies",
                "Implementation-Title" to this@configureJarManifest.name.toString(),
                "Implementation-Version" to this@configureJarManifest.version.toString()
            )
        }
    }
}

inline fun <reified T> Any?.safeAs(): T? {
    return this as? T
}

val Project.kotlinSourceSets get() = extensions.findByName("kotlin").safeAs<KotlinProjectExtension>()?.sourceSets

val Project.kotlinTargets
    get() =
        extensions.findByName("kotlin").safeAs<KotlinSingleTargetExtension>()?.target?.let { listOf(it) }
            ?: extensions.findByName("kotlin").safeAs<KotlinMultiplatformExtension>()?.targets

val Project.isKotlinJvmProject: Boolean get() = extensions.findByName("kotlin") is KotlinJvmProjectExtension
val Project.isKotlinMpp: Boolean get() = extensions.findByName("kotlin") is KotlinMultiplatformExtension

val Project.kotlinCompilations
    get() = kotlinTargets?.flatMap { it.compilations }