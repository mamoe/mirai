/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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
    google()
    mavenCentral()
}

kotlin {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.Experimental")
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
    }
}


fun version(name: String): String {
    val versions = project.projectDir.resolve("src/main/kotlin/Versions.kt").readText()

    return versions.lineSequence()
        .map { it.trim() }
        .single { it.startsWith("const val $name") }
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
    api("org.jetbrains.kotlin", "kotlin-compiler-embeddable", version("kotlinCompiler"))
    api("com.android.tools.build", "gradle", version("androidGradlePlugin"))
    api(asm("tree"))
    api(asm("util"))
    api(asm("commons"))

    api(gradleApi())
    api("com.googlecode.java-diff-utils:diffutils:" + version("difflib"))
}