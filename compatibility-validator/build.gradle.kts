@file:Suppress("UNUSED_VARIABLE")

plugins {
    kotlin("jvm")
    java
}

description = "Binary and source compatibility validator for mirai-core and mirai-core-qqandroid"

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
                runtimeOnly(project(":mirai-core-qqandroid"))
                compileOnly("net.mamoe:mirai-core-qqandroid:0.38.0")
            }
        }

        test {
            dependencies {
                api(kotlin("stdlib", Versions.Kotlin.stdlib))
                api(kotlin("test"))
                api(kotlin("test-junit"))
                runtimeOnly(project(":mirai-core-qqandroid"))
                compileOnly("net.mamoe:mirai-core-qqandroid:0.38.0")
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