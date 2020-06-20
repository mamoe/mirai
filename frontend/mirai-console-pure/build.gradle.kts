import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
}

apply(plugin = "com.github.johnrengelman.shadow")

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
    import0("net.mamoe:mirai-core:${Versions.Mirai.core}")
    import0(kotlin("stdlib")) // embedded by core

    testApi("net.mamoe:mirai-core-qqandroid:${Versions.Mirai.core}")
    testApi(project(":mirai-console"))
}

version = Versions.Mirai.consolePure

description = "Console Pure CLI frontend for mirai"

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}