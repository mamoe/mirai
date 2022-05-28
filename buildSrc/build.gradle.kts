/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    gradlePluginPortal()
}

kotlin {
    sourceSets.all {
        languageSettings.optIn("kotlin.Experimental")
        languageSettings.optIn("kotlin.RequiresOptIn")
    }
}


private val versionsText = project.projectDir.resolve("src/main/kotlin/Versions.kt").readText()
fun version(name: String): String {

    return versionsText.lineSequence()
        .map { it.trim() }
        .single { it.startsWith("const val $name ") }
        .substringAfter('"', "")
        .substringBefore('"', "")
        .also {
            check(it.isNotBlank())
            logger.debug("$name=$it")
        }
}

dependencies {
    val asmVersion = version("asm")
    fun asm(module: String) = "org.ow2.asm:asm-$module:$asmVersion"

    fun kotlinx(id: String, version: String) = "org.jetbrains.kotlinx:kotlinx-$id:$version"
    fun ktor(id: String, version: String) = "io.ktor:ktor-$id:$version"

    // compileOnly(kotlin("gradle-plugin-api", "1.3.72")) // Gradle's Kotlin is 1.3.72

    api("com.github.jengelman.gradle.plugins", "shadow", version("shadow"))
    api("org.jetbrains.kotlin", "kotlin-gradle-plugin", version("kotlinCompiler"))
//    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", version("kotlinCompiler"))
//    api(ktor("client-okhttp", "1.4.3"))
    api("com.android.tools.build", "gradle", version("androidGradlePlugin"))
    api(asm("tree"))
    api(asm("util"))
    api(asm("commons"))

    api("gradle.plugin.com.google.gradle:osdetector-gradle-plugin:1.7.0")

    api(gradleApi())
}