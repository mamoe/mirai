/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
import keys.SecretKeys
import kotlinx.validation.sourceSets
import java.io.ByteArrayOutputStream

plugins {
    id("io.codearte.nexus-staging") version "0.22.0"
    kotlin("jvm")
}

tasks.register<JavaExec>("runcihelper") {
    this.classpath = sourceSets["main"].runtimeClasspath
    this.mainClass.set("cihelper.CiHelperKt")

    fun Project.findPublishingExt(): PublishingExtension? {
        val exts = (this@findPublishingExt as ExtensionAware).extensions
        return exts.findByName("publishing") as PublishingExtension?
    }


    doFirst {
        @Suppress("USELESS_CAST")
        environment("PROJ_VERSION", (project.version as Any?).toString())
        rootProject.allprojects.asSequence()
            .mapNotNull { it.findPublishingExt() }
            .flatMap { it.publications.asSequence() }
            .mapNotNull { it as? MavenPublication }
            .map { it.artifactId }
            .joinToString("|")
            .let { environment("PROJ_ARTIFACTS", it) }

        rootProject.allprojects.asSequence()
            .mapNotNull { it.findPublishingExt() }
            .flatMap { it.repositories.asSequence() }
            .mapNotNull { it as? MavenArtifactRepository }
            .filter { it.name == "MiraiStageRepo" }
            .first().url
            .let { environment("PROJ_MiraiStageRepo", it.toString()) }

        val additionProperties = rootProject.properties.asSequence()
            .filter { (k, _) -> k.startsWith("cihelper.") }
            .map { (k, v) -> "-D$k=$v" }
            .toList()
        if (additionProperties.isNotEmpty()) {
            val currentJvmArgs = jvmArgs ?: emptyList()
            jvmArgs = currentJvmArgs + additionProperties
        }
    }
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

    doLast {
        setProjectVersionForFutureBuilds(snapshotVersion)
    }
}

tasks.register("publishSnapshotPage") {
    doLast {
        UpdateSnapshotPage.run(project, getSha())
    }
}


val snapshotVersion by lazy { getSnapshotVersionImpl() }

fun getSnapshotVersionImpl(): String {
    val branch = System.getenv("CURRENT_BRANCH_NAME")
    logger.info("Current branch name is '$branch'")
    val sha = getSha().trim().take(8)
    return "${Versions.project}-$branch-${sha}".also {
        logger.info("Snapshot version is '$it'")
    }
}

//tasks.register("createTagOnGitHub") {
//    group = "mirai"
//    dependsOn(gradle.includedBuild("snapshots-publishing").task(":check"))
//
//    doLast {
//        val token = System.getenv("MAMOE_TOKEN")
//        require(!token.isNullOrBlank()) { "" }
//
//        val out = ByteArrayOutputStream()
//        exec {
//            commandLine("git")
//            args("rev-parse", "HEAD")
//            standardOutput = out
//            workingDir = rootProject.projectDir
//        }
//        val sha = out.toString()
//        logger.info("Current sha is $sha")
//
//        runBlocking {
//            val resp = HttpClient().post<String>("https://api.github.com/repos/mamoe/mirai/git/refs") {
//                header("Authorization", "token $token")
//                header("Accept", "application/vnd.github.v3+json")
//                body = Gson().toJson(
//                    mapOf(
//                        "ref" to "refs/tags/build-$nextVersion",
//                        "sha" to sha,
//                    )
//                )
//            }
//            logger.info(resp)
//        }
//    }
//}

fun getSha(): String {
    val out = ByteArrayOutputStream()
    exec {
        commandLine("git")
        args("rev-parse", "HEAD")
        standardOutput = out
        workingDir = rootProject.projectDir
    }
    val sha = out.toString()
    logger.info("Current commit sha is '$sha'")
    return sha
}