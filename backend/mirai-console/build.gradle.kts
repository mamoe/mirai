@file:Suppress("UnusedImport")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.Instant

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
}

version = Versions.console
description = "Console backend for mirai"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

kotlin {
    explicitApiWarning()

    sourceSets.all {
        target.compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
                //useIR = true
            }
        }
        languageSettings.apply {
            enableLanguageFeature("InlineClasses")
            progressiveMode = true

            useExperimentalAnnotation("kotlin.Experimental")
            useExperimentalAnnotation("kotlin.RequiresOptIn")

            useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
            useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiExperimentalAPI")
            useExperimentalAnnotation("net.mamoe.mirai.console.ConsoleFrontEndImplementation")
            useExperimentalAnnotation("net.mamoe.mirai.console.util.ConsoleExperimentalApi")
            useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
            useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
            useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            useExperimentalAnnotation("net.mamoe.mirai.console.util.ConsoleInternalApi")
        }
    }
}

dependencies {
    compileAndTestRuntime("net.mamoe:mirai-core:${Versions.core}")
    compileAndTestRuntime(kotlin("stdlib", Versions.kotlinStdlib))
    compileAndTestRuntime(kotlin("stdlib-jdk8", Versions.kotlinStdlib))

    compileAndTestRuntime("org.jetbrains.kotlinx:atomicfu:${Versions.atomicFU}")
    compileAndTestRuntime(kotlinx("coroutines-core", Versions.coroutines))
    compileAndTestRuntime(kotlinx("serialization-core", Versions.serialization))
    compileAndTestRuntime(kotlinx("serialization-json", Versions.serialization))
    compileAndTestRuntime(kotlin("reflect"))

    smartImplementation("net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}")
    smartImplementation("org.jetbrains:annotations:19.0.0")
    smartApi(kotlinx("coroutines-jdk8", Versions.coroutines))

    testApi("net.mamoe:mirai-core-qqandroid:${Versions.core}")
    testApi(kotlin("stdlib-jdk8"))
    testApi(kotlin("test"))
    testApi(kotlin("test-junit5"))

    testApi("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }

    val compileKotlin by getting {}

    val fillBuildConstants by registering {
        group = "mirai"
        doLast {
            (compileKotlin as KotlinCompile).source.filter { it.name == "MiraiConsoleBuildConstants.kt" }.single()
                .let { file ->
                    file.writeText(
                        file.readText()
                            .replace(
                                Regex("""val buildDate: Instant = Instant.ofEpochSecond\(.*\)""")
                            ) {
                                """val buildDate: Instant = Instant.ofEpochSecond(${
                                    Instant.now().epochSecond
                                })"""
                            }
                            .replace(
                                Regex("""const val versionConst:\s+String\s+=\s+".*"""")
                            ) { """const val versionConst: String = "${project.version}"""" }
                    )
                }
        }
    }
}

// region PUBLISHING

setupPublishing("mirai-console")

// endregion