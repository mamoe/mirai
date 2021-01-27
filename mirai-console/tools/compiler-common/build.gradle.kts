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

dependencies {
    api(`jetbrains-annotations`)
    // api(`kotlinx-coroutines-jdk8`)

    compileOnly(`kotlin-compiler`)
    testRuntimeOnly(`kotlin-compiler`)
}

setupPublishing("mirai-console-compiler-common")