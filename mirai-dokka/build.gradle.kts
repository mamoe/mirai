/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
}

dependencies {
    implementation(`kotlinx-serialization-core`)
    implementation(`kotlinx-serialization-json`)
}

fun Project.newExec(name: String, type: String, conf: JavaExec.() -> Unit) {
    tasks.create(name, JavaExec::class.java) {
        this.classpath = sourceSets["main"].runtimeClasspath
        this.mainClass.set("net.mamoe.mirai.dokka.${type}Kt")
        this.workingDir(rootProject.projectDir)
        this.environment("mirai_ver", rootProject.version.toString())
        conf()
    }
}

newExec("prepare", "Prepare") {
}

newExec("deployPages", "DeployToGitHub") {
}

newExec("update-vers", "BuildVersionList") {
}


