@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("jvm")
    java
}

description = "Binary and source compatibility validator for mirai-core and mirai-core-qqandroid"

val kotlinVersion: String by rootProject.ext
val coroutinesVersion: String by rootProject.ext

fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }

        main {
            dependencies {
                api(kotlin("stdlib"))
                api(project(":mirai-core-qqandroid"))
                api(kotlinx("coroutines-core", coroutinesVersion))
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}