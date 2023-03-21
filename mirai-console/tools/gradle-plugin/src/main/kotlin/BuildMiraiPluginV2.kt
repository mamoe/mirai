/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
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
    public companion object {
        public const val FILE_SUFFIX: String = "mirai2.jar"
    }

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

        @Suppress("LocalVariableName")
        @TaskAction
        internal fun run() {
            val runtime = mutableSetOf<String>()
            val api = mutableSetOf<String>()
            val linkedDependencies = mutableSetOf<String>()
            val linkToApi = mutableSetOf<String>()
            val shadowedFiles = mutableSetOf<File>()
            val shadowedDependencies = mutableSetOf<String>()
            val subprojects = mutableSetOf<String>()
            val subprojects_fullpath = mutableSetOf<String>()
            // val subprojects_unlinked_fullpath = mutableSetOf<String>()
            val subprojects_linked_fullpath = mutableSetOf<String>()

            project.configurations.findByName(MiraiConsoleGradlePlugin.MIRAI_SHADOW_CONF_NAME)?.allDependencies?.forEach { dep ->
                if (dep is ExternalModuleDependency) {
                    val artId = "${dep.group}:${dep.name}"
                    shadowedDependencies.add(artId)
                }
            }
            project.configurations.findByName(MiraiConsoleGradlePlugin.MIRAI_AS_NORMAL_DEP_CONF_NAME)?.allDependencies?.forEach { dep1 ->
                fun resolve0(dep: Dependency) {
                    if (dep is ProjectDependency) {
                        linkedDependencies.add("${dep.group}:${dep.name}")
                        subprojects_linked_fullpath.add(dep.dependencyProject.path)
                        dep.dependencyProject.configurations.findByName("apiElements")?.allDependencies?.forEach {
                            resolve0(it)
                        }
                        dep.dependencyProject.configurations.findByName("implementation")?.allDependencies?.forEach {
                            resolve0(it)
                        }
                    }
                }
                resolve0(dep1)
            }

            fun deepForeachDependencies(conf: Configuration?, action: (Dependency) -> Unit) {
                (conf ?: return).allDependencies.forEach { dep ->
                    action(dep)
                    if (dep is ProjectDependency) {
                        subprojects.add("${dep.group}:${dep.name}")
                        deepForeachDependencies(dep.dependencyProject.configurations.findByName(conf.name), action)
                    }
                }

            }

            fun resolveProject(project: Project, doResolveApi: Boolean) {
                deepForeachDependencies(project.configurations.findByName("apiElements")) { dep ->
                    if (dep is ExternalModuleDependency) {
                        val artId = "${dep.group}:${dep.name}"
                        linkedDependencies.add(artId)
                        if (doResolveApi) {
                            linkToApi.add(artId)
                        }
                    }
                    if (dep is ProjectDependency) {
                        subprojects_fullpath.add(dep.dependencyProject.path)
                        subprojects.add("${dep.group}:${dep.name}")
                        resolveProject(dep.dependencyProject, doResolveApi)
                    }
                }

                project.configurations.findByName("implementation")?.allDependencies?.forEach { dep ->
                    if (dep is ExternalModuleDependency) {
                        linkedDependencies.add("${dep.group}:${dep.name}")
                    }
                    if (dep is ProjectDependency) {
                        subprojects_fullpath.add(dep.dependencyProject.path)
                        subprojects.add("${dep.group}:${dep.name}")
                        resolveProject(dep.dependencyProject, false)
                    }
                }
            }

            resolveProject(project, true)
            linkedDependencies.removeAll(shadowedDependencies)
            linkToApi.removeAll(shadowedDependencies)
            linkedDependencies.addAll(miraiDependencies)

            fun ResolvedDependency.depId(): String = "$moduleGroup:$moduleName"

            val runtimeClasspath = project.configurations["runtimeClasspath"].resolvedConfiguration
            fun markAsResolved(resolvedDependency: ResolvedDependency) {
                val depId = resolvedDependency.depId()
                if (depId !in shadowedDependencies) {
                    linkedDependencies.add(depId)
                }
                resolvedDependency.children.forEach { markAsResolved(it) }
            }

            fun linkDependencyTo(resolvedDependency: ResolvedDependency, dependencies: MutableCollection<String>) {

                // bom files
                if (resolvedDependency.allModuleArtifacts.any { it.extension == "jar" }) kotlin.run link@{
                    if (resolvedDependency.depId() in shadowedDependencies) return@link

                    dependencies.add(resolvedDependency.module.toString())
                }

                resolvedDependency.children.forEach { linkDependencyTo(it, dependencies) }
            }

            fun resolveDependency(resolvedDependency: ResolvedDependency) {
                val depId = resolvedDependency.depId()
                logger.info { "resolving         : $depId" }
                if (depId in linkedDependencies) {
                    markAsResolved(resolvedDependency)

                    linkDependencyTo(resolvedDependency, runtime)
                    if (depId in linkToApi) {
                        linkDependencyTo(resolvedDependency, api)
                    }
                    return
                }
                if (depId in subprojects) {
                    resolvedDependency.children.forEach { resolveDependency(it) }
                    return
                }
            }
            runtimeClasspath.firstLevelModuleDependencies.forEach { resolveDependency(it) }

            /*subprojects_fullpath.forEach { usedProject ->
                val subProj = project.project(usedProject)
                if ("${subProj.group}:${subProj.name}" !in linkedDependencies) {
                    subprojects_unlinked_fullpath.add(usedProject)
                }
            }*/

            logger.info { "linkedDependencies: $linkedDependencies" }
            logger.info { "linkToAPi         : $linkToApi" }
            logger.info { "api               : $api" }
            logger.info { "runtime           : $runtime" }
            logger.info { "subprojects       : $subprojects" }
            logger.info { "subprojects_linked: $subprojects_linked_fullpath" }
            // logger.info { "subprojects_unlink: $subprojects_unlinked_fullpath" }

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
                val cid = artId.componentIdentifier
                if (cid is ProjectComponentIdentifier) {
                    if (cid.projectPath in subprojects_linked_fullpath) {
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
        archiveExtension.set(FILE_SUFFIX)
        duplicatesStrategy = DuplicatesStrategy.WARN

        val compilations = target.compilations.filter { it.name == KotlinCompilation.MAIN_COMPILATION_NAME }
        @Suppress("DEPRECATION") // New API requires Kotlin 1.8.0, but we must support lower versions
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