/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registering

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */


fun Project.configureBintray() {
    tasks.register("ensureBintrayAvailable") {
        doLast {
            if (!project.isBintrayAvailable()) {
                error("bintray isn't available. ")
            }
        }
    }

    if (isBintrayAvailable()) {
        publishing {
            repositories {
                maven {
                    setUrl("https://api.bintray.com/maven/him188moe/mirai/mirai-core/;publish=1;override=1")

                    credentials {
                        username = Bintray.getUser(project)
                        password = Bintray.getKey(project)
                    }
                }
            }
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Project.configurePublishing(
    artifactId: String,
    bintrayRepo: String = "mirai",
    bintrayPkgName: String = artifactId,
    vcs: String = "https://github.com/mamoe/mirai"
) {
    configureBintray()
    apply<ShadowPlugin>()

    if (!project.isBintrayAvailable()) {
        println("bintray isn't available. NO PUBLICATIONS WILL BE SET")
        return
    }

    bintray {
        user = Bintray.getUser(project)
        key = Bintray.getKey(project)

        setPublications("mavenJava")
        setConfigurations("archives")

        publish = true
        override = true

        pkg.apply {
            repo = bintrayRepo
            name = bintrayPkgName
            setLicenses("AGPLv3")
            publicDownloadNumbers = true
            vcsUrl = vcs
        }
    }

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    publishing {
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])

                groupId = rootProject.group.toString()
                setArtifactId(artifactId)
                version = project.version.toString()

                pom.withXml {
                    val root = asNode()
                    root.appendNode("description", description)
                    root.appendNode("name", project.name)
                    root.appendNode("url", vcs)
                    root.children().last()
                }

                artifact(sourcesJar.get())
            }
        }
    }
}