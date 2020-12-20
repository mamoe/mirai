/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "NOTHING_TO_INLINE", "RemoveRedundantBackticks")

import com.github.jengelman.gradle.plugins.shadow.ShadowPlugin
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
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
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


inline fun Project.configurePublishing(
    artifactId: String,
    bintrayRepo: String = "mirai",
    bintrayPkgName: String = artifactId,
    addPrefixAll: Boolean = true,
    vcs: String = "https://github.com/mamoe/mirai"
) {

    tasks.register("ensureBintrayAvailable") {
        doLast {
            if (!Bintray.isBintrayAvailable(project)) {
                error("bintray isn't available. ")
            }
        }
    }
    apply<ShadowPlugin>()

    // afterEvaluate {

    /*
    val shadowJar = tasks.filterIsInstance<ShadowJar>().firstOrNull() ?: return//@afterEvaluate

    tasks.register("shadowJarMd5") {
        dependsOn(shadowJar)

        val outFiles = shadowJar.outputs.files.associateWith { file ->
            File(file.parentFile, file.name.removeSuffix(".jar").removeSuffix("-all") + "-all.jar.md5")
        }

        outFiles.forEach { (_, output) ->
            output.createNewFile()
            outputs.files(output)
        }

        doLast {
            for ((origin, output) in outFiles) {
                output
                    .writeText(origin.inputStream().md5().toUHexString().trim(Char::isWhitespace))
            }
        }

        tasks.getByName("publish").dependsOn(this)
        tasks.getByName("bintrayUpload").dependsOn(this)
    }
    */

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
                    /*
                    afterEvaluate {
                        for (file in tasks.getByName("shadowJarMd5").outputs.files) {
                            artifact(provider { file })
                        }
                    }
                    */

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
    //}

}