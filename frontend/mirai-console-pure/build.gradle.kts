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
            languageSettings.useExperimentalAnnotation("kotlin.OptIn")
            languageSettings.progressiveMode = true
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            languageSettings.useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
            languageSettings.useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }
}

var debugging = true

dependencies {
    fun import0(dep: Any) {
        if (debugging) {
            implementation(dep)
        } else {
            compileOnly(dep)
        }
    }
    import0("org.jline:jline:3.15.0")
    import0("org.fusesource.jansi:jansi:1.18")

    import0(project(":mirai-console"))
    import0("net.mamoe:mirai-core:${Versions.core}")
    import0(kotlin("stdlib")) // embedded by core

    testApi("net.mamoe:mirai-core-qqandroid:${Versions.core}")
    testApi(project(":mirai-console"))
}

version = Versions.consolePure

description = "Console Pure CLI frontend for mirai"