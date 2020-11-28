/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering


private val Project.selfAndParentProjects: Sequence<Project>
    get() = generateSequence(this) { it.parent }

private fun Project.findPropertySmart(propName: String): String? {
    return findProperty(propName)?.toString()
        ?: System.getProperty(propName)
        ?: selfAndParentProjects.map { it.projectDir.resolve(propName) }.find { it.exists() }?.readText()
        ?: System.getenv(propName)
}

private fun Project.findPropertySmartOrFail(propName: String): String {
    return findPropertySmart(propName) ?: error("[Mirai Console] Cannot find property for publication: $propName. Please check your 'mirai' configuration.")
}

internal fun Project.registerPublishPluginTask() {
    val mirai = miraiExtension

    bintray {
        user = mirai.publishing.user ?: findPropertySmartOrFail("bintray.user")
        key = mirai.publishing.key ?: findPropertySmartOrFail("bintray.key")

        setPublications("mavenJava")
        setConfigurations("archives")

        publish = mirai.publishing.publish
        override = mirai.publishing.override

        pkg.apply {
            repo = mirai.publishing.repo ?: findPropertySmartOrFail("bintray.repo")
            name = mirai.publishing.packageName ?: findPropertySmartOrFail("bintray.package")
            userOrg = mirai.publishing.org ?: findPropertySmart("bintray.org")

            mirai.publishing.bintrayPackageConfigConfigs.forEach { it.invoke(this) }
        }

        mirai.publishing.bintrayConfigs.forEach { it.invoke(this) }
    }

    @Suppress("DEPRECATION")
    val sourcesJar by tasks.registering(Jar::class) {
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    publishing {
        /*
        repositories {
            maven {
                // change to point to your repo, e.g. http://my.org/repo
                url = uri("$buildDir/repo")
            }
        }*/
        publications.register("mavenJava", MavenPublication::class.java) { publication ->
            with(publication) {
                from(components["java"])

                this.groupId = mirai.publishing.groupId ?: project.group.toString()
                this.artifactId = mirai.publishing.artifactId ?: project.name
                this.version = mirai.publishing.version ?: project.version.toString()

                pom.withXml { xml ->
                    val root = xml.asNode()
                    root.appendNode("description", project.description)
                    root.appendNode("name", project.name)
                    // root.appendNode("url", vcs)
                    root.children().last()

                    mirai.publishing.mavenPomConfigs.forEach { it.invoke(xml) }
                }

                artifact(sourcesJar.get())

                // TODO: 2020/11/28 -miraip metadata artifact
                // TODO: 2020/11/28 -all shadowed artifact

                mirai.publishing.mavenPublicationConfigs.forEach { it.invoke(this) }
            }
        }
    }
}


/**
 * Configures the [bintray][com.jfrog.bintray.gradle.BintrayExtension] extension.
 */
@PublishedApi
internal fun Project.bintray(configure: com.jfrog.bintray.gradle.BintrayExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("bintray", configure)

@PublishedApi
internal val Project.sourceSets: org.gradle.api.tasks.SourceSetContainer
    get() = (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer

/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] extension.
 */
@PublishedApi
internal fun Project.publishing(configure: org.gradle.api.publish.PublishingExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("publishing", configure)
