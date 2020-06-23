plugins {
    kotlin("jvm") version Versions.kotlin
    kotlin("plugin.serialization") version Versions.kotlin
    id("java")
    `maven-publish`
    id("com.jfrog.bintray") version Versions.bintray
}

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")

            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.progressiveMode = true
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiExperimentalAPI")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            languageSettings.useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }
}

dependencies {
    implementation("org.jline:jline:3.15.0")
    implementation("org.fusesource.jansi:jansi:1.18")

    compileAndRuntime(project(":mirai-console"))
    compileAndRuntime("net.mamoe:mirai-core:${Versions.core}")
    compileAndRuntime(kotlin("stdlib")) // embedded by core

    testApi("net.mamoe:mirai-core-qqandroid:${Versions.core}")
    testApi(project(":mirai-console"))
}

version = Versions.consolePure

description = "Console Pure CLI frontend for mirai"