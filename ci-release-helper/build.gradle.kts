/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
import keys.SecretKeys
import org.gradle.kotlin.dsl.support.useToRun
import java.net.URL

plugins {
    id("io.codearte.nexus-staging") version "0.22.0"
}

description = "Mirai CI Methods for Releasing"

nexusStaging {
    packageGroup = rootProject.group.toString()
    val keys = SecretKeys.getCache(project).loadKey("sonatype")
    username = keys.user
    password = keys.password
}

tasks.register("updateSnapshotVersion") {
    group = "mirai"
    dependsOn(gradle.includedBuild("snapshots-publishing").task(":check"))

    doLast {
        var baseUrl = System.getenv("SNAPSHOTS_PUBLISHING_URL") ?: "https://repo.mirai.mamoe.net/snapshots/"
        if (!baseUrl.endsWith('/')) {
            baseUrl += "/"
        }
        baseUrl += "net/mamoe/mirai-core-utils/maven-metadata.xml"

        val content = URL(baseUrl).openConnection().getInputStream().useToRun {
            readBytes().decodeToString()
        }

        val branch = System.getenv("CURRENT_BRANCH_NAME")
        logger.info("Current branch name is '$branch'")

        val currentVersion = getLatestMiraiVersionForBranch(content, branch)

        logger.info("Current newest version for this branch is '$currentVersion'")

        val nextVersion = currentVersion.nextSnapshotVersion(branch).toString()

        logger.info("Next snapshot version will be '$nextVersion'")

        rootProject.file("buildSrc/src/main/kotlin/Versions.kt").run {
            var text = readText()
            check(text.indexOf("project = \"${project.version}\"") != -1) { "Cannot find \"project = \\\"${project.version}\\\"\"" }
            text = text.replace("project = \"${project.version}\"", "project = \"${nextVersion}\"")
            writeText(text)
        }
    }
}