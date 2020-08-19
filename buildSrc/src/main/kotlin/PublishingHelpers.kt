@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "NOTHING_TO_INLINE", "RemoveRedundantBackticks")

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import upload.Bintray
import java.util.*
import kotlin.reflect.KProperty

/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

/**
 * Configures the [bintray][com.jfrog.bintray.gradle.BintrayExtension] extension.
 */
@PublishedApi
internal fun org.gradle.api.Project.`bintray`(configure: com.jfrog.bintray.gradle.BintrayExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("bintray", configure)

@PublishedApi
internal operator fun <U : Task> RegisteringDomainObjectDelegateProviderWithTypeAndAction<out TaskContainer, U>.provideDelegate(
    receiver: Any?,
    property: KProperty<*>
) = ExistingDomainObjectDelegate.of(
    delegateProvider.register(property.name, type.java, action)
)

@PublishedApi
internal val org.gradle.api.Project.`sourceSets`: org.gradle.api.tasks.SourceSetContainer
    get() =
        (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer

@PublishedApi
internal operator fun <T> ExistingDomainObjectDelegate<out T>.getValue(receiver: Any?, property: KProperty<*>): T =
    delegate

/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] extension.
 */
@PublishedApi
internal fun org.gradle.api.Project.`publishing`(configure: org.gradle.api.publish.PublishingExtension.() -> Unit): Unit =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("publishing", configure)


inline fun Project.setupPublishing(
    artifactId: String,
    bintrayRepo: String = "mirai",
    bintrayPkgName: String = "mirai-console",
    vcs: String = "https://github.com/mamoe/mirai-console"
) {

    tasks.register("ensureBintrayAvailable") {
        doLast {
            if (!Bintray.isBintrayAvailable(project)) {
                error("bintray isn't available. ")
            }
        }
    }

    if (Bintray.isBintrayAvailable(project)) {
        bintray {
            val keyProps = Properties()
            val keyFile = file("../keys.properties")
            if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }
            if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }

            user = Bintray.getUser(project)
            key = Bintray.getKey(project)
            setPublications("mavenJava")
            setConfigurations("archives")

            pkg.apply {
                repo = bintrayRepo
                name = bintrayPkgName
                setLicenses("AGPLv3")
                publicDownloadNumbers = true
                vcsUrl = vcs
            }
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
            publications {
                register("mavenJava", MavenPublication::class) {
                    from(components["java"])

                    groupId = rootProject.group.toString()
                    this.artifactId = artifactId
                    version = version

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
    } else println("bintray isn't available. NO PUBLICATIONS WILL BE SET")

}