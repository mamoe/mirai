/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

plugins {
    `java-platform`
    `maven-publish`
}

description = "Mirai BOM"

rootProject.subprojects
    .filter { it.path != project.path }
    .forEach { project.evaluationDependsOn(it.path) }

dependencies {
    constraints {
        rootProject.subprojects
            .filter { it.path != project.path }
            .filter { it.extensions.findByName("publishing") != null }
            .forEach { subProject ->
                subProject.publishing.publications
                    .withType<MavenPublication>()
                    .forEach {
                        this@constraints.api("${it.groupId}:${it.artifactId}:${it.version}")
                    }
            }
    }
}

configurePublishing(
    "mirai-bom",
    addProjectComponents = false,
)

publishing.publications.getByName<MavenPublication>("mavenJava") {
    from(components["javaPlatform"])
}
