/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleFrontEndImplementation::class)

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.MiraiConsoleImplementation.ConsoleDataScope.Companion.get
import net.mamoe.mirai.console.fontend.ProcessProgress
import net.mamoe.mirai.console.internal.MiraiConsoleBuildDependencies
import net.mamoe.mirai.console.internal.data.builtins.DataScope
import net.mamoe.mirai.console.internal.data.builtins.PluginDependenciesConfig
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.console.util.renderMemoryUsageNumber
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.verbose
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.codehaus.plexus.util.ReaderFactory
import org.codehaus.plexus.util.xml.pull.MXParser
import org.codehaus.plexus.util.xml.pull.XmlPullParser
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.WorkspaceReader
import org.eclipse.aether.repository.WorkspaceRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResult
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transfer.AbstractTransferListener
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transport.http.HttpTransporterFactory
import java.io.File
import java.util.concurrent.ConcurrentHashMap


@Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
internal class JvmPluginDependencyDownloader(
    val logger: MiraiLogger,
) {
    val repositories: MutableList<RemoteRepository>
    val session: RepositorySystemSession
    val locator: org.eclipse.aether.spi.locator.ServiceLocator
    val repository: RepositorySystem
    val dependencyFilter: DependencyFilter = DependencyFilter { node, _ ->
        if (node == null || node.artifact == null) return@DependencyFilter true

        val artGroup = node.artifact.groupId
        val artId = node.artifact.artifactId


        if (artGroup == "net.mamoe") {
            if (artId in listOf(
                    "mirai-core",
                    "mirai-core-jvm",
                    "mirai-core-android",
                    "mirai-core-api",
                    "mirai-core-api-jvm",
                    "mirai-core-api-android",
                    "mirai-core-utils",
                    "mirai-core-utils-jvm",
                    "mirai-core-utils-android",
                    "mirai-console",
                    "mirai-console-terminal",
                    "mirai-console-frontend-base",
                    "mirai-console-compiler-annotations",
                    "mirai-console-compiler-annotations-jvm",
                )
            ) return@DependencyFilter false
        }

        // Re-download slf4j-api is unnecessary since slf4j-api was bound by console
        if (artGroup == "org.slf4j" && artId == "slf4j-api") {
            return@DependencyFilter false
        }

        // Loaded by console system
        if ("$artGroup:$artId" in MiraiConsoleBuildDependencies.dependencies)
            return@DependencyFilter false

        // println("  `- filter: $node")
        true
    }

    init {
        locator = MavenRepositorySystemUtils.newServiceLocator()
        locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
        locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
        repository = locator.getService(RepositorySystem::class.java)
        session = MavenRepositorySystemUtils.newSession()
        session.checksumPolicy = "fail"
        session.localRepositoryManager = repository.newLocalRepositoryManager(
            session, LocalRepository(PluginManager.pluginLibrariesFolder)
        )
        session.transferListener = object : AbstractTransferListener() {
            private val dwnProgresses: MutableMap<File, ProcessProgress> = ConcurrentHashMap()

            override fun transferStarted(event: TransferEvent) {
                logger.verbose {
                    "Downloading ${event.resource?.repositoryUrl}${event.resource?.resourceName}"
                }
                val nw = MiraiConsoleImplementation.getInstance().createNewProcessProgress()
                dwnProgresses.put(
                    event.resource.file, nw
                )?.close()
                nw.setTotalSize(event.resource.contentLength)
                nw.updateText("Downloading ${event.resource.resourceName}....")
            }

            override fun transferSucceeded(event: TransferEvent) {
                dwnProgresses.remove(event.resource.file)?.let { dp ->
                    dp.updateText(buildString {
                        append("Downloaded  ")
                        append(event.resource.resourceName)
                        append(" (")
                        renderMemoryUsageNumber(this@buildString, event.resource.contentLength)
                        append(")")
                    })
                    dp.close()
                }
            }

            override fun transferProgressed(event: TransferEvent) {
                dwnProgresses[event.resource.file]?.let { pg ->
                    pg.update(event.transferredBytes)
                    pg.updateText(buildString bs@{
                        append("Downloading ")
                        append(event.resource.resourceName)
                        append(" (")
                        val sz = this@bs.length
                        renderMemoryUsageNumber(this@bs, event.transferredBytes)
                        repeat(kotlin.math.max(0, 7 - (this@bs.length - sz))) {
                            append(' ')
                        }

                        append(" / ")
                        renderMemoryUsageNumber(this@bs, event.resource.contentLength)
                        append(")")
                    })
                    pg.rerender()
                }
            }

            override fun transferFailed(event: TransferEvent) {
                logger.warning(event.exception)
                dwnProgresses.remove(event.resource.file)?.let {
                    it.markFailed()
                    it.close()
                }
            }
        }
        val userHome = System.getProperty("user.home")
        fun findMavenLocal(): File {
            val mavenHome = File(userHome, ".m2")
            fun findFromSettingsXml(): File? {
                val settings = File(mavenHome, "settings.xml")
                if (!settings.isFile) return null
                ReaderFactory.newXmlReader(settings).use { reader ->
                    val parser = MXParser()
                    parser.setInput(reader)

                    var eventType = parser.eventType
                    var joinedSettings = false
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        when (eventType) {
                            XmlPullParser.START_TAG -> {
                                if (!joinedSettings) {
                                    if (parser.name != "settings") {
                                        return null
                                    }
                                    joinedSettings = true
                                } else {
                                    if (parser.name == "localRepository") {
                                        val loc = File(parser.nextText())
                                        if (loc.isDirectory) return loc
                                        return null
                                    } else {
                                        parser.skipSubTree()
                                    }
                                }
                            }
                            // else -> parser.skipSubTree()
                        }
                        eventType = parser.next()
                    }
                }
                return null
            }
            return kotlin.runCatching {
                findFromSettingsXml()
            }.onFailure { error ->
                logger.warning(error)
            }.getOrNull() ?: File(mavenHome, "repository")
        }

        fun findGradleDepCache(): File {
            var gradleHome = File(userHome, ".gradle")
            val gradleEnvHome = System.getenv("GRADLE_USER_HOME").orEmpty()
            if (gradleEnvHome.isNotBlank()) {
                gradleHome = File(gradleEnvHome)
            }
            return File(gradleHome, "caches/modules-2/files-2.1")
        }

        val mavenLocRepo = findMavenLocal()
        val gradleLocRepo = findGradleDepCache()
        logger.debug { "Maven        local: $mavenLocRepo" }
        logger.debug { "Gradle cache local: $gradleLocRepo" }
        session.workspaceReader = object : WorkspaceReader {
            private val repository: WorkspaceRepository = WorkspaceRepository("default")
            override fun getRepository(): WorkspaceRepository = repository

            override fun findArtifact(artifact: Artifact): File? {
                // logger.debug { "Try resolve $artifact" }
                val path = session.localRepositoryManager.getPathForLocalArtifact(artifact)
                File(mavenLocRepo, path).takeIf { it.isFile }?.let { return it }
                val gradleDep = gradleLocRepo
                    .resolve(artifact.groupId)
                    .resolve(artifact.artifactId)
                    .resolve(artifact.baseVersion)
                if (gradleDep.isDirectory) {
                    val fileName = buildString {
                        append(artifact.artifactId)
                        append('-')
                        append(artifact.baseVersion)
                        artifact.classifier?.takeIf { it.isNotEmpty() }?.let { c ->
                            append('-').append(c)
                        }
                        append('.').append(artifact.extension)
                    }
                    gradleDep.walk().maxDepth(2)
                        .filter { it.isFile }
                        .firstOrNull { it.name == fileName }
                        ?.let { return it }
                }
                return null
            }

            override fun findVersions(artifact: Artifact?): MutableList<String> {
                return mutableListOf()
            }

        }
        session.setReadOnly()
        val config = DataScope.get<PluginDependenciesConfig>()
        repositories = repository.newResolutionRepositories(
            session,
            config.repoLoc.map { url ->
                RemoteRepository.Builder(url, "default", url).build()
            }
        )
        logger.debug { "Remote server: " + config.repoLoc }
    }

    fun resolveDependencies(deps: Iterable<String>, vararg filters: DependencyFilter): DependencyResult {

        val dependencies: MutableList<Dependency> = ArrayList()
        for (library in deps) {
            val defaultArtifact = DefaultArtifact(library)
            val dependency = Dependency(defaultArtifact, null)
            dependencies.add(dependency)
        }
        return repository.resolveDependencies(
            session, DependencyRequest(
                CollectRequest(
                    null as Dependency?, dependencies,
                    repositories
                ),
                when {
                    filters.isEmpty() -> dependencyFilter
                    else -> DependencyFilter { node, parents ->
                        if (node == null || node.artifact == null) return@DependencyFilter true
                        if (!dependencyFilter.accept(node, parents)) return@DependencyFilter false
                        filters.forEach { filter ->
                            if (!filter.accept(node, parents)) return@DependencyFilter false
                        }
                        return@DependencyFilter true
                    }
                }
            )
        )
    }
}
