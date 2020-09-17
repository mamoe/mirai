@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")

    id("org.jetbrains.intellij") version "0.4.16"

}

repositories {
    maven("http://maven.aliyun.com/nexus/content/groups/public/")
}

version = Versions.console
description = "IntelliJ plugin for Mirai Console"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version = "2020.2.1"
    isDownloadSources = true
    updateSinceUntilBuild = false

    setPlugins(
        "org.jetbrains.kotlin:1.4.10-release-IJ2020.2-1@staging"
    )
}

tasks.getByName("publishPlugin", org.jetbrains.intellij.tasks.PublishTask::class) {
    val pluginKey = project.findProperty("jetbrains.hub.key")?.toString()
    if (pluginKey != null) {
        logger.info("Found jetbrains.hub.key")
        setToken(pluginKey)
    } else {
        logger.info("jetbrains.hub.key not found")
    }
}

tasks.withType<org.jetbrains.intellij.tasks.PatchPluginXmlTask> {
    sinceBuild("193.*")
    untilBuild("205.*")
    changeNotes("""
        Fix cancellation on analyzing augments
    """.trimIndent())
}

kotlin {
    sourceSets.all {
        target.compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
                //useIR = true
            }
        }
        languageSettings.apply {
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
    api("org.jetbrains:annotations:19.0.0")
    api(kotlinx("coroutines-jdk8", Versions.coroutines))

    compileOnly("org.jetbrains.kotlin:kotlin-compiler:${Versions.kotlinCompiler}")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:${Versions.kotlinCompiler}")
    compileOnly(files("libs/ide-common.jar"))

    testApi(kotlin("test"))
    testApi(kotlin("test-junit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }
}

// setupPublishing("mirai-console-intellij")