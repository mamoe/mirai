/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnstableApiUsage", "UNUSED_VARIABLE", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

buildscript {
    repositories {
        mavenLocal()
        // maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        mavenCentral()
        jcenter()
        google()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.androidGradlePlugin}")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:${Versions.atomicFU}")
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:${Versions.binaryValidator}")
    }
}

plugins {
    kotlin("jvm") version Versions.kotlinCompiler
    kotlin("plugin.serialization") version Versions.kotlinCompiler
    id("org.jetbrains.dokka") version Versions.dokka
    id("net.mamoe.kotlin-jvm-blocking-bridge") version Versions.blockingBridge
    id("com.jfrog.bintray") // version Versions.bintray
}

// https://github.com/kotlin/binary-compatibility-validator
apply(plugin = "binary-compatibility-validator")

configure<kotlinx.validation.ApiValidationExtension> {
    ignoredProjects.add("mirai-core")
    ignoredProjects.add("mirai-core-api")
    ignoredProjects.add("mirai-core-utils")
    ignoredProjects.add("mirai-core-all")
    ignoredProjects.add("mirai")

    ignoredPackages.add("net.mamoe.mirai.internal")
    nonPublicMarkers.add("net.mamoe.mirai.MiraiInternalApi")
    nonPublicMarkers.add("net.mamoe.mirai.MiraiExperimentalApi")
}

project.ext.set("isAndroidSDKAvailable", false)

// until
// https://youtrack.jetbrains.com/issue/KT-37152,
// are fixed.

/*
runCatching {
    val keyProps = Properties().apply {
        file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    if (keyProps.getProperty("sdk.dir", "").isNotEmpty()) {
        project.ext.set("isAndroidSDKAvailable", true)
    } else {
        project.ext.set("isAndroidSDKAvailable", false)
    }
}.exceptionOrNull()?.run {
    project.ext.set("isAndroidSDKAvailable", false)
}*/

allprojects {
    group = "net.mamoe"
    version = Versions.project

    repositories {
        // mavenLocal() // cheching issue cause compiler exception
        // maven(url = "https://mirrors.huaweicloud.com/repository/maven")
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://kotlin.bintray.com/kotlinx")
        google()
        mavenCentral()
        maven(url = "https://dl.bintray.com/karlatemp/misc")
    }

    afterEvaluate {
        configureJvmTarget()
        configureMppShadow()
        configureEncoding()
        configureKotlinTestSettings()
        configureKotlinCompilerSettings()
        configureKotlinExperimentalUsages()

        blockingBridge {
            unitCoercion = net.mamoe.kjbb.compiler.UnitCoercion.COMPATIBILITY
        }

        //  useIr()

        if (isKotlinJvmProject) {
            configureFlattenSourceSets()
        }
    }
}
subprojects {
    afterEvaluate {
        if (project.name == "mirai-core-api") configureDokka()
    }
}

fun Project.useIr() {
    kotlinCompilations?.forEach { kotlinCompilation ->
        kotlinCompilation.kotlinOptions.freeCompilerArgs += "-Xuse-ir"
    }
}

fun Project.configureDokka() {
    apply(plugin = "org.jetbrains.dokka")
    tasks {
        val dokkaHtml by getting(DokkaTask::class) {
            outputDirectory.set(buildDir.resolve("dokka"))
        }
        val dokkaGfm by getting(DokkaTask::class) {
            outputDirectory.set(buildDir.resolve("dokka-gfm"))
        }
    }
    tasks.withType<DokkaTask>().configureEach {
        dokkaSourceSets.configureEach {
            perPackageOption {
                matchingRegex.set("net\\.mamoe\\.mirai\\.*")
                skipDeprecated.set(true)
            }

            for (suppressedPackage in arrayOf(
                """net.mamoe.mirai.internal""",
                """net.mamoe.mirai.internal.message""",
                """net.mamoe.mirai.internal.network"""
            )) {
                perPackageOption {
                    matchingRegex.set(suppressedPackage.replace(".", "\\."))
                    suppress.set(true)
                }
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE") // or error
fun Project.configureJvmTarget() {
    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions.jvmTarget = "1.8"
    }

    kotlinTargets.orEmpty().filterIsInstance<KotlinJvmTarget>().forEach { target ->
        target.compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.languageVersion = "1.4"
        }
        target.testRuns["test"].executionTask.configure { useJUnitPlatform() }
    }

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

fun Project.configureMppShadow() {
    val kotlin =
        runCatching {
            (this as ExtensionAware).extensions.getByName("kotlin") as? KotlinMultiplatformExtension
        }.getOrNull() ?: return

    val shadowJvmJar by tasks.creating(ShadowJar::class) sd@{
        group = "mirai"
        archiveClassifier.set("-all")

        val compilations =
            kotlin.targets.filter { it.platformType == KotlinPlatformType.jvm }
                .map { it.compilations["main"] }

        compilations.forEach {
            dependsOn(it.compileKotlinTask)
            from(it.output)
        }

        println(project.configurations.joinToString())

        from(project.configurations.getByName("jvmRuntimeClasspath"))

        this.exclude { file ->
            file.name.endsWith(".sf", ignoreCase = true)
        }

        /*
        this.manifest {
            this.attributes(
                "Manifest-Version" to 1,
                "Implementation-Vendor" to "Mamoe Technologies",
                "Implementation-Title" to this.name.toString(),
                "Implementation-Version" to this.version.toString()
            )
        }*/
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
    when {
        isKotlinJvmProject -> {
            dependencies {
                testImplementation(kotlin("test-junit5"))

                testApi("org.junit.jupiter:junit-jupiter-api:5.2.0")
                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
            }
        }
        isKotlinMpp -> {
            kotlinSourceSets?.forEach { sourceSet ->
                if (sourceSet.name == "common") {
                    sourceSet.dependencies {
                        implementation(kotlin("test"))
                        implementation(kotlin("test-annotations-common"))
                    }
                } else {
                    sourceSet.dependencies {
                        implementation(kotlin("test-junit5"))

                        implementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
                        implementation("org.junit.jupiter:junit-jupiter-engine:5.2.0")
                    }
                }
            }
        }
    }
}

fun Project.configureKotlinCompilerSettings() {
    val kotlinCompilations = kotlinCompilations ?: return
    for (kotlinCompilation in kotlinCompilations) with(kotlinCompilation) {
        if (isKotlinJvmProject) {
            @Suppress("UNCHECKED_CAST")
            this as KotlinCompilation<KotlinJvmOptions>
        }
        kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
    }
}

val experimentalAnnotations = arrayOf(
    "kotlin.RequiresOptIn",
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.time.ExperimentalTime",

    "kotlinx.serialization.ExperimentalSerializationApi",

    "net.mamoe.mirai.utils.MiraiInternalApi",
    "net.mamoe.mirai.utils.MiraiExperimentalApi",
    "net.mamoe.mirai.LowLevelApi",
    "net.mamoe.mirai.utils.UnstableExternalImage",

    "net.mamoe.mirai.message.data.ExperimentalMessageKey"
)

fun Project.configureKotlinExperimentalUsages() {
    val sourceSets = kotlinSourceSets ?: return

    for (target in sourceSets) {
        target.languageSettings.progressiveMode = true
        target.languageSettings.enableLanguageFeature("InlineClasses")
        experimentalAnnotations.forEach { a ->
            target.languageSettings.useExperimentalAnnotation(a)
        }
    }
}

fun Project.configureFlattenSourceSets() {
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

val Project.kotlinSourceSets get() = extensions.findByName("kotlin").safeAs<KotlinProjectExtension>()?.sourceSets

val Project.kotlinTargets
    get() =
        extensions.findByName("kotlin").safeAs<KotlinSingleTargetExtension>()?.target?.let { listOf(it) }
            ?: extensions.findByName("kotlin").safeAs<KotlinMultiplatformExtension>()?.targets

val Project.isKotlinJvmProject: Boolean get() = extensions.findByName("kotlin") is KotlinJvmProjectExtension
val Project.isKotlinMpp: Boolean get() = extensions.findByName("kotlin") is KotlinMultiplatformExtension

val Project.kotlinCompilations
    get() = kotlinTargets?.flatMap { it.compilations }