/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("UnusedImport")

import shadow.configureRelocatedShadowJarForJvmProject
import shadow.relocateImplementation

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    id("me.him188.kotlin-jvm-blocking-bridge")
}

version = Versions.project
description = "Mirai core shadowed"

dependencies {
    api(project(":mirai-core"))
    api(project(":mirai-core-api"))
    api(project(":mirai-core-utils"))
    implementation(`slf4j-api`) // Required by mirai-console

    relocateImplementation(project, `kt-bignum_relocated`)
    relocateImplementation(project, `ktor-client-core_relocated`)
    relocateImplementation(project, `ktor-client-okhttp_relocated`)
    relocateImplementation(project, `ktor-io_relocated`)
}

val shadow = configureRelocatedShadowJarForJvmProject(kotlin)

if (System.getenv("MIRAI_IS_SNAPSHOTS_PUBLISHING")?.toBoolean() != true) {
    // Do not publish `-all` jars to snapshot server since they are too large.

    configurePublishing("mirai-core-all", addShadowJar = false)

    publications {
        getByName("mavenJava", MavenPublication::class) {
            artifact(shadow)
        }
    }

    tasks.getByName("publishMavenJavaPublicationToMavenLocal").dependsOn(shadow)
    tasks.findByName("publishMavenJavaPublicationToMavenCentralRepository")?.dependsOn(shadow)
}

//
//// WARNING: You must also consider relocating transitive dependencies.
//// Otherwise, user will get NoClassDefFound error when using mirai as a classpath dependency. See #2263.
//
//val includeInRuntime = true
//relocateAllFromGroupId("io.ktor", includeInRuntime, "io.ktor")
//relocateAllFromGroupId("com.squareup.okhttp3", includeInRuntime, listOf("okhttp3"))
//relocateAllFromGroupId("com.squareup.okio", includeInRuntime, listOf("okio"))