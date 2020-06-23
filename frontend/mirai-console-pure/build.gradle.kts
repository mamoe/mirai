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

ext {
    // 傻逼 compileAndRuntime 没 exclude 掉
    // 傻逼 gradle 第二次配置 task 会覆盖掉第一次的配置
    val x: com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.() -> Unit = {
        dependencyFilter.include {
            when ("${it.moduleGroup}:${it.moduleName}") {
                "org.jline:jline" -> true
                "org.fusesource.jansi:jansi" -> true
                else -> false
            }
        }
    }
    this.set("shadowJar", x)
}

version = Versions.consolePure

description = "Console Pure CLI frontend for mirai"