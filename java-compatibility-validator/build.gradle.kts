@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("jvm")
    java
}

description = "Java compatibility validator for mirai-core and mirai-core-qqandroid"

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    sourceSets {
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        }

        main {
            dependencies {
                api(kotlin("stdlib", Versions.Kotlin.stdlib))
                api(project(":mirai-core-qqandroid"))
            }
        }

        test {
            dependencies {
                api(kotlin("stdlib", Versions.Kotlin.stdlib))
                api(kotlin("test"))
                api(kotlin("test-junit"))
                api(project(":mirai-core-qqandroid"))
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