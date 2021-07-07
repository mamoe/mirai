/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("UnusedImport")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:${Versions.proguard}")
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    id("net.mamoe.kotlin-jvm-blocking-bridge")
}

version = Versions.project
description = "Mirai core shadowed"

dependencies {
    api(project(":mirai-core"))
    api(project(":mirai-core-api"))
    api(project(":mirai-core-utils"))
}

configurePublishing("mirai-core-all")

afterEvaluate {
    tasks.register<proguard.gradle.ProGuardTask>("proguard") {
        group = "mirai"

        verbose()

        injars(tasks.getByName<ShadowJar>("shadowJar"))

        kotlin.runCatching {
            file("build/libs/${project.name}-${project.version}-all-min.jar").delete()
        }.exceptionOrNull()?.printStackTrace()
        outjars("build/libs/${project.name}-${project.version}-all-min.jar")

        val kotlinLibraries = kotlin.target.compilations["main"].compileDependencyFiles
        //        .plus(kotlin.target.compilations["main"].runtimeDependencyFiles)

        kotlinLibraries.distinctBy { it.normalize().name }.forEach { file ->
            if (file.name.contains("-common")) return@forEach
            if (file.name.contains("-metadata")) return@forEach
            if (file.extension == "jar") libraryjars(file)
        }

        val javaHome = System.getProperty("java.home")
        // Automatically handle the Java version of this build.
        if (System.getProperty("java.version").startsWith("1.")) {
            // Before Java 9, the runtime classes were packaged in a single jar file.
            libraryjars("$javaHome/lib/rt.jar")
        } else {
            File(javaHome, "jmods").listFiles().orEmpty().forEach { file ->
                libraryjars(
                    mapOf(
                        "jarfilter" to "!**.jar",
                        "filter" to "!module-info.class"
                    ), file
                )
            }
//        // As of Java 9, the runtime classes are packaged in modular jmod files.
//        libraryjars(
//            // filters must be specified first, as a map
//            mapOf("jarfilter" to "!**.jar",
//                "filter" to "!module-info.class"),
//            "$javaHome/jmods/java.base.jmod"
//        )
        }

        configuration("mirai.pro")
        configuration("kotlinx-serialization.pro")

        dontobfuscate()
        // keepattributes("*Annotation*,synthetic")
    }

}
