@file:Suppress("UnstableApiUsage")
plugins {
    id("com.jfrog.bintray") version Versions.bintray apply false
    id("net.mamoe.kotlin-jvm-blocking-bridge") version Versions.blockingBridge apply false
    kotlin("jvm") version Versions.kotlinCompiler
    kotlin("plugin.serialization") version Versions.kotlinCompiler
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