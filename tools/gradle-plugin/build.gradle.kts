@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish")
    id("java")
    //signing
    `maven-publish`
    id("com.jfrog.bintray")

    id("com.github.johnrengelman.shadow")
}

dependencies {
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin-api").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }
    compileOnly(kotlin("gradle-plugin").toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
    }

    compileOnly(kotlin("stdlib"))

    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
    api("org.jetbrains:annotations:19.0.0")
}

dependencies {
    testApi(kotlin("test"))
    testApi(kotlin("test-junit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

version = Versions.console
description = "Gradle plugin for Mirai Console"

pluginBundle {
    website = "https://github.com/mamoe/mirai-console"
    vcsUrl = "https://github.com/mamoe/mirai-console"
    tags = listOf("framework", "kotlin", "mirai")
}

gradlePlugin {
    plugins {
        create("miraiConsole") {
            id = "net.mamoe.mirai-console"
            displayName = "Mirai Console"
            description = project.description
            implementationClass = "net.mamoe.mirai.console.gradle.MiraiConsoleGradlePlugin"
        }
    }
}

kotlin {
    sourceSets.all {
        target.compilations.all {
            kotlinOptions {
                apiVersion = "1.3"
                languageVersion = "1.3"
                jvmTarget = "1.8"
                freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all"
            }
        }
        languageSettings.apply {
            progressiveMode = true

            useExperimentalAnnotation("kotlin.RequiresOptIn")
            useExperimentalAnnotation("kotlin.ExperimentalUnsignedTypes")
            useExperimentalAnnotation("kotlin.experimental.ExperimentalTypeInference")
            useExperimentalAnnotation("kotlin.contracts.ExperimentalContracts")
        }
    }
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }

    val compileKotlin by getting {}

    @Suppress("UNUSED_VARIABLE")
    val fillBuildConstants by registering {
        group = "mirai"
        doLast {
            (compileKotlin as org.jetbrains.kotlin.gradle.tasks.KotlinCompile).source.filter { it.name == "VersionConstants.kt" }.single()
                .let { file ->
                    file.writeText(
                        file.readText()
                            .replace(
                                Regex("""const val CONSOLE_VERSION = ".*"""")
                            ) {
                                """const val CONSOLE_VERSION = "${Versions.console}""""
                            }
                            .replace(
                                Regex("""const val CORE_VERSION = ".*"""")
                            ) { """const val CORE_VERSION = "${Versions.core}"""" }
                    )
                }
        }
    }

    compileKotlin.dependsOn(fillBuildConstants)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

setupPublishing("mirai-console-gradle")