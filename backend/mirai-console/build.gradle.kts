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
    implementation("net.mamoe:mirai-core:${Versions.core}")

    implementation(kotlinx("serialization-core", Versions.serialization))
    implementation(kotlin("reflect"))

    api("net.mamoe.yamlkt:yamlkt:${Versions.yamlkt}")
    implementation("org.jetbrains.kotlinx:atomicfu:${Versions.atomicFU}")
    api("org.jetbrains:annotations:19.0.0")
    api(kotlinx("coroutines-jdk8", Versions.coroutines))

    api("com.vdurmont:semver4j:3.1.0")

    //api(kotlinx("collections-immutable", Versions.collectionsImmutable))

    testApi(kotlinx("serialization-core", Versions.serialization))
    testApi("net.mamoe:mirai-core-qqandroid:${Versions.core}")
    testApi(kotlin("stdlib-jdk8"))
    testApi(kotlin("test"))
    testApi(kotlin("test-junit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")


//    val autoService = "1.0-rc7"
//    kapt("com.google.auto.service", "auto-service", autoService)
//    compileOnly("com.google.auto.service", "auto-service-annotations", autoService)
}

ext.apply {
    // 傻逼 compileAndRuntime 没 exclude 掉
    // 傻逼 gradle 第二次配置 task 会覆盖掉第一次的配置
    val x: com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.() -> Unit = {
        dependencyFilter.exclude {
            when ("${it.moduleGroup}:${it.moduleName}") {
                "net.mamoe:mirai-core" -> true
                "org.jetbrains.kotlin:kotlin-stdlib" -> true
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8" -> true
                "net.mamoe:mirai-core-qqandroid" -> true
                else -> false
            }
        }
    }
    set("shadowJar", x)
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
                                    Instant.now().getEpochSecond()
                                })"""
                            }
                            .replace(
                                Regex("""val version: Semver = Semver\(".*", Semver.SemverType.LOOSE\)""")
                            ) { """val version: Semver = Semver("${project.version}", Semver.SemverType.LOOSE)""" }
                    )
            }
        }
    }
}

// region PUBLISHING

setupPublishing("mirai-console")

// endregion