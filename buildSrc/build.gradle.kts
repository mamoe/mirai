/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    jcenter()
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
}

dependencies {
    fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
    fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

    api("org.jsoup:jsoup:1.12.1")

    api("com.google.code.gson:gson:2.8.6")
    api(kotlinx("coroutines-core", "1.3.3"))
    api(ktor("client-core", "1.3.2"))
    api(ktor("client-cio", "1.3.2"))
    api(ktor("client-json", "1.3.2"))
    compileOnly("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    api("com.github.jengelman.gradle.plugins:shadow:6.0.0")
}