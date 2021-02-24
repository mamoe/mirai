/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import org.gradle.api.Project
import org.gradle.api.internal.tasks.DefaultTaskDependency
import org.gradle.api.internal.tasks.TaskDependencyInternal
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenArtifact
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.internal.artifact.AbstractMavenArtifact
import org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication
import java.io.File

open class GPGSignMavenArtifact(
    private val delegate: MavenArtifact,
    private val tasks: TaskDependencyInternal = TaskDependencyInternal.EMPTY
) : AbstractMavenArtifact() {
    override fun getFile(): File {
        return File(delegate.file.path + ".asc")
    }

    override fun shouldBePublished(): Boolean = (delegate as? AbstractMavenArtifact)?.shouldBePublished() ?: true
    override fun getDefaultExtension(): String = delegate.extension + ".asc"
    override fun getDefaultClassifier(): String = delegate.classifier ?: ""
    override fun getDefaultBuildDependencies(): TaskDependencyInternal = tasks
}

class NameCounter(val name: String) {
    var counter = 0
    val nextName: String
        get() = name + if (counter == 0) {
            counter = 1; ""
        } else {
            counter++; counter
        }
}

object PublishingAccess {
    fun getMetadataArtifacts(publication: MavenPublication): Collection<MavenArtifact> {
        if (publication is DefaultMavenPublication) {
            return DefaultMavenPublication::class.java.getDeclaredField("metadataArtifacts")
                .also { it.isAccessible = true }
                .get(publication) as Collection<MavenArtifact>
        }
        return emptyList()
    }
}

fun PublishingExtension.configGpgSign(project: Project) {
    if (GpgSigner.signer === GpgSigner.NoopSigner) {
        return
    }
    val tasks = DefaultTaskDependency()
    val signArtifactsGPG = NameCounter("signArtifactsGPG")

    publications.forEach { publication ->
        if (publication is MavenPublication) {
            val artifacts0: Collection<Pair<Collection<MavenArtifact>, (MavenArtifact) -> Unit>> = listOf(
                publication.artifacts to { publication.artifact(it) }, // main artifacts
                PublishingAccess.getMetadataArtifacts(publication).let { artifacts -> // pom files
                    if (artifacts is MutableCollection<MavenArtifact>) {
                        artifacts to { artifacts.add(it) }
                    } else {
                        artifacts to { publication.artifact(it) }
                    }
                }
            )
            val allArtifacts = artifacts0.flatMap { it.first }.toList()

            if (allArtifacts.isNotEmpty()) {
                tasks.add(project.tasks.create(signArtifactsGPG.nextName) {
                    group = "publishing"
                    doLast {
                        allArtifacts.forEach { artifact ->
                            if ((artifact as? AbstractMavenArtifact)?.shouldBePublished() != false) {
                                GpgSigner.signer.doSign(artifact.file)
                            }
                        }
                    }

                    allArtifacts.forEach {
                        dependsOn(it.buildDependencies)
                    }
                })

                artifacts0.forEach { (artifacts, artifactsRegister) ->
                    artifacts.toList().forEach { artifact ->
                        logPublishing("gpg sign for artifact ${artifact.smartToString()}")
                        artifactsRegister(GPGSignMavenArtifact(artifact, tasks))
                    }
                }
            }
        }
    }

}
