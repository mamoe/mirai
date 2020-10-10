@file:Suppress("UnstableApiUsage")
plugins {
    kotlin("jvm") version Versions.kotlinCompiler
    kotlin("plugin.serialization") version Versions.kotlinCompiler
    id("com.jfrog.bintray") version Versions.bintray apply false
    id("net.mamoe.kotlin-jvm-blocking-bridge") version Versions.blockingBridge apply false
    id("com.gradle.plugin-publish") version "0.12.0" apply false
    //id("com.bmuschko.nexus") version "2.3.1" apply false
    //id("io.codearte.nexus-staging") version "0.11.0" apply false
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

allprojects {
    group = "net.mamoe"

    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        jcenter()
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        apply<MiraiConsoleBuildPlugin>()

        setJavaCompileTarget()
    }
}