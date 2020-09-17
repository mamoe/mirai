@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
}

repositories {
    maven("http://maven.aliyun.com/nexus/content/groups/public/")
}

version = Versions.console
description = "Mirai Console compiler common"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
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