@file:Suppress("UnusedImport")

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
    id("net.mamoe.kotlin-jvm-blocking-bridge")
}

version = Versions.project
description = "Mirai core shadowed"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

kotlin {
    explicitApi()
}

dependencies {
    api(project(":mirai-core"))
    api(project(":mirai-core-api"))
}

configurePublishing("mirai-core-all")