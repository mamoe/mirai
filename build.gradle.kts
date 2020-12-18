@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.utils.addToStdlib.safeAs

plugins {
    kotlin("jvm") version Versions.kotlinCompiler
    kotlin("plugin.serialization") version Versions.kotlinCompiler
    id("com.jfrog.bintray") version Versions.bintray apply false
    id("net.mamoe.kotlin-jvm-blocking-bridge") version Versions.blockingBridge apply false
    id("com.gradle.plugin-publish") version "0.12.0" apply false
    //id("com.bmuschko.nexus") version "2.3.1" apply false
    //id("io.codearte.nexus-staging") version "0.11.0" apply false
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

allprojects {
    group = "net.mamoe"

    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        jcenter()
        mavenCentral()
        maven(url = "https://dl.bintray.com/karlatemp/misc")
    }
}

subprojects {
    afterEvaluate {
        apply<MiraiConsoleBuildPlugin>()

        configureJvmTarget()
        configureEncoding()
        configureKotlinExperimentalUsages()
        configureKotlinCompilerSettings()
        configureKotlinTestSettings()
        configureSourceSets()
    }
}

val experimentalAnnotations = arrayOf(
    "kotlin.Experimental",
    "kotlin.RequiresOptIn",
    "kotlin.ExperimentalUnsignedTypes",
    "kotlin.ExperimentalStdlibApi",
    "kotlin.contracts.ExperimentalContracts",
    "kotlin.time.ExperimentalTime",
    "kotlin.experimental.ExperimentalTypeInference",
    "kotlinx.coroutines.ExperimentalCoroutinesApi",
    "kotlinx.serialization.ExperimentalSerializationApi",
    "kotlin.io.path.ExperimentalPathApi",
    "io.ktor.util.KtorExperimentalAPI",

    "net.mamoe.mirai.utils.MiraiInternalApi",
    "net.mamoe.mirai.utils.MiraiExperimentalApi",
    "net.mamoe.mirai.console.ConsoleFrontEndImplementation",
    "net.mamoe.mirai.console.util.ConsoleExperimentalApi",
    "net.mamoe.mirai.console.util.ConsoleInternalApi"
)

fun Project.configureJvmTarget() {
    tasks.withType(KotlinJvmCompile::class.java) {
        kotlinOptions.jvmTarget = "1.8"
    }

    extensions.findByType(JavaPluginExtension::class.java)?.run {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

fun Project.useIr() {
    tasks {
        withType(KotlinJvmCompile::class.java) {
            kotlinOptions.useIR = true
        }
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

fun Project.configureEncoding() {
    tasks.withType(JavaCompile::class.java) {
        options.encoding = "UTF8"
    }
}

fun Project.configureSourceSets() {
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

fun Project.configureKotlinExperimentalUsages() {
    val sourceSets = kotlinSourceSets ?: return

    for (target in sourceSets) target.languageSettings.run {
        enableLanguageFeature("InlineClasses")
        progressiveMode = true
        experimentalAnnotations.forEach { a ->
            useExperimentalAnnotation(a)
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