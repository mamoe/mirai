/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.JavaVersion
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectList
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

private fun Project.jvmVersion(): JavaVersion {
    return if (project.path.endsWith("mirai-console-intellij")) {
        JavaVersion.VERSION_17
    } else {
        JavaVersion.VERSION_1_8
    }
}

fun Project.optInForAllTargets(qualifiedClassname: String) {
    tasks.withType(org.jetbrains.kotlin.gradle.dsl.KotlinCompile::class) {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=$qualifiedClassname"
    }
}

fun Project.enableLanguageFeatureForAllSourceSets(qualifiedClassname: String) {
    kotlinSourceSets!!.all {
        languageSettings {
            this.enableLanguageFeature(qualifiedClassname)
        }
    }
}

fun Project.enableLanguageFeatureForTestSourceSets(name: String) {
    allTestSourceSets {
        languageSettings {
            this.enableLanguageFeature(name)
        }
    }
}

fun Project.allTestSourceSets(action: KotlinSourceSet.() -> Unit) {
    kotlinSourceSets!!.all {
        if (this.name.contains("test", ignoreCase = true)) {
            action()
        }
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

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = defaultVer
        targetCompatibility = defaultVer
    }

    allKotlinTargets().all {
        if (this !is KotlinJvmTarget) return@all
        when (this.attributes.getAttribute(KotlinPlatformType.attribute)) { // mirai does magic, don't use target.platformType
            KotlinPlatformType.androidJvm -> {
                this.compilations.all {
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
        this.testRuns["test"].executionTask.configure { useJUnitPlatform() }
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
            kotlinSourceSets?.all {
                val sourceSet = this
                fun configureJvmTest(sourceSet: KotlinSourceSet) {
                    sourceSet.dependencies {
                        implementation(kotlin("test-junit5"))?.because(b)

                        implementation(`junit-jupiter-api`)?.because(b)
                        runtimeOnly(`junit-jupiter-engine`)?.because(b)
                    }
                }

                val target = allKotlinTargets()
                    .find { it.name == sourceSet.name.substringBeforeLast("Main").substringBeforeLast("Test") }

                when {
                    sourceSet.name == "commonTest" -> {
                        if (target?.platformType == KotlinPlatformType.jvm &&
                            target.attributes.getAttribute(MIRAI_PLATFORM_INTERMEDIATE) != true
                        ) {
                            configureJvmTest(sourceSet)
                        } else {
                            sourceSet.dependencies {
                                implementation(kotlin("test"))?.because(b)
                                implementation(kotlin("test-annotations-common"))?.because(b)
                            }
                        }
                    }

                    sourceSet.name.contains("test", ignoreCase = true) -> {
                        if (target?.platformType == KotlinPlatformType.jvm &&
                            target.attributes.getAttribute(MIRAI_PLATFORM_INTERMEDIATE) != true
                        ) {
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
    "kotlin.io.path.ExperimentalPathApi",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "net.mamoe.mirai.utils.TestOnly",
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

val testLanguageFeatures = listOf(
    "ContextReceivers"
)

fun Project.configureKotlinOptIns() {
    val sourceSets = kotlinSourceSets ?: return
    sourceSets.all {
        configureKotlinOptIns()
    }

    for (name in testLanguageFeatures) {
        enableLanguageFeatureForTestSourceSets(name)
    }

    allTestSourceSets {
        languageSettings.languageVersion = Versions.kotlinLanguageVersionForTests
    }
}

fun KotlinSourceSet.configureKotlinOptIns() {
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
            resources.setSrcDirs(listOf(projectDir.resolve("testResources")))
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

fun Project.allKotlinTargets(): NamedDomainObjectCollection<KotlinTarget> {
    return extensions.findByName("kotlin")?.safeAs<KotlinSingleTargetExtension<*>>()
        ?.target?.let { namedDomainObjectListOf(it) }
        ?: extensions.findByName("kotlin")?.safeAs<KotlinMultiplatformExtension>()?.targets
        ?: namedDomainObjectListOf()
}

private inline fun <reified T> Project.namedDomainObjectListOf(vararg values: T): NamedDomainObjectList<T> {
    return objects.namedDomainObjectList(T::class.java).apply { addAll(values) }
}

val Project.isKotlinJvmProject: Boolean get() = extensions.findByName("kotlin") is KotlinJvmProjectExtension
val Project.isKotlinMpp: Boolean get() = extensions.findByName("kotlin") is KotlinMultiplatformExtension

fun Project.allKotlinCompilations(action: (KotlinCompilation<KotlinCommonOptions>) -> Unit) {
    allKotlinTargets().all {
        compilations.all(action)
    }
}