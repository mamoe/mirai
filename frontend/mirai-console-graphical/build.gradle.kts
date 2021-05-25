/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

plugins {
    id("kotlinx-serialization")
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("kotlin")
    id("java")
    `maven-publish`
}

javafx {
    version = "13.0.2"
    modules = listOf("javafx.controls")
    //mainClassName = "Application"
}

apply(plugin = "com.github.johnrengelman.shadow")


/*
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    manifest {
        attributes["Main-Class"] = "net.mamoe.mirai.console.graphical.MiraiGraphicalLoader"
    }
}
 */

version = Versions.Mirai.consoleGraphical

description = "Graphical frontend for mirai-console"

dependencies {
    compileOnly("net.mamoe:mirai-core:${Versions.core}")
    implementation(project(":mirai-console"))

    api(group = "no.tornado", name = "tornadofx", version = "1.7.19")
    api(group = "com.jfoenix", name = "jfoenix", version = "9.0.8")

    testApi(project(":mirai-console"))
    testApi(kotlinx("coroutines-core", Versions.coroutines))
    testApi(group = "org.yaml", name = "snakeyaml", version = "1.25")
    testApi("net.mamoe:mirai-core:${Versions.core}")
    testApi("net.mamoe:mirai-core-qqandroid:${Versions.core}")
}

kotlin {
    sourceSets {
        all {

            languageSettings.useExperimentalAnnotation("kotlin.Experimental")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.progressiveMode = true
            languageSettings.useExperimentalAnnotation("net.mamoe.mirai.utils.MiraiInternalAPI")
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

@Suppress("DEPRECATION")
val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}