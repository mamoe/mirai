/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.capabilities.Capability
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.internal.artifacts.ivyservice.DefaultLenientConfiguration
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactVisitor
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvableArtifact
import org.gradle.api.internal.file.FileCollectionInternal
import org.gradle.api.internal.file.FileCollectionStructureVisitor
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.internal.DisplayName
import org.gradle.internal.component.external.model.ModuleComponentArtifactIdentifier
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import java.io.File
import javax.inject.Inject

@Suppress("RedundantLambdaArrow", "RemoveExplicitTypeArguments")
public open class BuildMiraiPluginV2 : Jar() {

    // @get:Internal
    private lateinit var metadataTask: GenMetadataTask

    internal open class GenMetadataTask
    @Inject internal constructor(
        @JvmField internal val orgTask: BuildMiraiPluginV2,
    ) : DefaultTask() {
        companion object {
            val miraiDependencies = mutableSetOf(
                "net.mamoe:mirai-core-api",
                "net.mamoe:mirai-core-api-jvm",
                "net.mamoe:mirai-core-api-android",
                "net.mamoe:mirai-core",
                "net.mamoe:mirai-core-jvm",
                "net.mamoe:mirai-core-android",
                "net.mamoe:mirai-core-utils",
                "net.mamoe:mirai-core-utils-jvm",
                "net.mamoe:mirai-core-utils-android",
                "net.mamoe:mirai-console",
                "net.mamoe:mirai-console-terminal",
            )
        }
        @TaskAction
        internal fun run() {
            val runtime = mutableSetOf<String>()
            val api = mutableSetOf<String>()
            val linkedDependencies = mutableSetOf<String>()
            val linkToApi = mutableSetOf<String>()
            val shadowedFiles = mutableSetOf<File>()
            val shadowedDependencies = mutableSetOf<String>()

            project.configurations.findByName(MiraiConsoleGradlePlugin.MIRAI_SHADOW_CONF_NAME)?.allDependencies?.forEach { dep ->
                if (dep is ExternalModuleDependency) {
                    val artId = "${dep.group}:${dep.name}"
                    shadowedDependencies.add(artId)
                }
            }
            project.configurations.findByName("apiElements")?.allDependencies?.forEach { dep ->
                if (dep is ExternalModuleDependency) {
                    val artId = "${dep.group}:${dep.name}"
                    linkedDependencies.add(artId)
                    linkToApi.add(artId)
                }
            }
            project.configurations.findByName("implementation")?.allDependencies?.forEach { dep ->
                if (dep is ExternalModuleDependency) {
                    linkedDependencies.add("${dep.group}:${dep.name}")
                }
            }
            linkedDependencies.removeAll(shadowedDependencies)
            linkToApi.removeAll(shadowedDependencies)
            linkedDependencies.addAll(miraiDependencies)

            fun ResolvedDependency.depId(): String = "$moduleGroup:$moduleName"

            val runtimeClasspath = project.configurations["runtimeClasspath"].resolvedConfiguration
            fun markAsResolved(resolvedDependency: ResolvedDependency) {
                val depId = resolvedDependency.depId()
                linkedDependencies.add(depId)
                resolvedDependency.children.forEach { markAsResolved(it) }
            }

            fun linkDependencyTo(resolvedDependency: ResolvedDependency, dependencies: MutableCollection<String>) {
                dependencies.add(resolvedDependency.module.toString())
                resolvedDependency.children.forEach { linkDependencyTo(it, dependencies) }
            }

            fun resolveDependency(resolvedDependency: ResolvedDependency) {
                val depId = resolvedDependency.depId()
                if (depId in linkedDependencies) {
                    markAsResolved(resolvedDependency)
                    linkDependencyTo(resolvedDependency, runtime)
                    if (depId in linkToApi) {
                        linkDependencyTo(resolvedDependency, api)
                    }
                    return
                }
            }
            runtimeClasspath.firstLevelModuleDependencies.forEach { resolveDependency(it) }

            logger.info { "linkedDependencies: $linkedDependencies" }
            logger.info { "linkToAPi         : $linkToApi" }
            logger.info { "api               : $api" }
            logger.info { "runtime           : $runtime" }

            val lenientConfiguration = runtimeClasspath.lenientConfiguration
            if (lenientConfiguration is DefaultLenientConfiguration) {
                val resolvedArtifacts = mutableSetOf<ResolvedArtifact>()
                lenientConfiguration.select().visitArtifacts(object : ArtifactVisitor {
                    override fun prepareForVisit(source: FileCollectionInternal.Source): FileCollectionStructureVisitor.VisitType {
                        return FileCollectionStructureVisitor.VisitType.Visit
                    }

                    override fun visitArtifact(
                        variantName: DisplayName,
                        variantAttributes: AttributeContainer,
                        capabilities: MutableList<out Capability>,
                        artifact: ResolvableArtifact
                    ) {
                        resolvedArtifacts.add(artifact.toPublicView())
                    }

                    override fun requireArtifactFiles(): Boolean = false
                    override fun visitFailure(failure: Throwable) {}
                }, false)
                resolvedArtifacts
            } else {
                runtimeClasspath.resolvedArtifacts
            }.forEach { artifact ->
                val artId = artifact.id
                if (artId is ModuleComponentArtifactIdentifier) {
                    val cid = artId.componentIdentifier
                    if ("${cid.group}:${cid.module}" in linkedDependencies) {
                        return@forEach
                    }
                }
                logger.info { "  `- $artId - ${artId.javaClass}" }
                shadowedFiles.add(artifact.file)
            }

            shadowedFiles.forEach { file ->
                if (file.isDirectory) {
                    orgTask.from(file)
                } else if (file.extension == "jar") {
                    orgTask.from(project.zipTree(file))
                } else {
                    orgTask.from(file)
                }
            }

            temporaryDir.also {
                it.mkdirs()
            }.let { tmpDir ->
                tmpDir.resolve("api.txt").writeText(api.sorted().joinToString("\n"))
                tmpDir.resolve("runtime.txt").writeText(runtime.sorted().joinToString("\n"))
                orgTask.from(tmpDir.resolve("api.txt")) { copy ->
                    copy.into("META-INF/mirai-console-plugin")
                    copy.rename { "dependencies-shared.txt" }
                }
                orgTask.from(tmpDir.resolve("runtime.txt")) { copy ->
                    copy.into("META-INF/mirai-console-plugin")
                    copy.rename { "dependencies-private.txt" }
                }
            }
        }
    }

    internal fun registerMetadataTask(tasks: TaskContainer, metadataTaskName: String) {
        metadataTask = tasks.create<GenMetadataTask>(metadataTaskName, this)
    }

    internal fun init(target: KotlinTarget) {
        dependsOn(metadataTask)
        archiveExtension.set("mirai.jar")
        duplicatesStrategy = DuplicatesStrategy.WARN

        val compilations = target.compilations.filter { it.name == KotlinCompilation.MAIN_COMPILATION_NAME }
        compilations.forEach {
            dependsOn(it.compileKotlinTask)
            from(it.output.allOutputs)
            metadataTask.dependsOn(it.compileKotlinTask)
        }
        exclude { elm ->
            elm.path.startsWith("META-INF/") && elm.name.endsWith(".sf", ignoreCase = true)
        }
    }
}