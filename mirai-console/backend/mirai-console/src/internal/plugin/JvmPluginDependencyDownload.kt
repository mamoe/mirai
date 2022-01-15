/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.internal.MiraiConsoleBuildDependencies
import net.mamoe.mirai.console.internal.data.builtins.PluginDependenciesConfig
import net.mamoe.mirai.console.plugin.PluginManager
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.verbose
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.graph.DependencyFilter
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.resolution.DependencyResult
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.spi.locator.ServiceLocator
import org.eclipse.aether.transfer.AbstractTransferListener
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transport.http.HttpTransporterFactory


@Suppress("DEPRECATION", "MemberVisibilityCanBePrivate")
internal class JvmPluginDependencyDownloader(
    val logger: MiraiLogger,
) {
    val repositories: MutableList<RemoteRepository>
    val session: RepositorySystemSession
    val locator: ServiceLocator
    val repository: RepositorySystem
    val dependencyFilter: DependencyFilter = DependencyFilter { node, parents ->
        if (node == null || node.artifact == null) return@DependencyFilter true

        val artGroup = node.artifact.groupId
        val artId = node.artifact.artifactId

        // mirai used netty-all
        if (artGroup == "io.netty") return@DependencyFilter false

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
                )
            ) return@DependencyFilter false
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
            override fun transferStarted(event: TransferEvent) {
                logger.verbose {
                    "Downloading ${event.resource?.repositoryUrl}${event.resource?.resourceName}"
                }
            }

            override fun transferFailed(event: TransferEvent) {
                logger.warning(event.exception)
            }
        }
        session.setReadOnly()
        repositories = repository.newResolutionRepositories(
            session,
//            listOf(RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build())
//            listOf(RemoteRepository.Builder("central", "default", "https://maven.aliyun.com/repository/public").build())
            listOf(RemoteRepository.Builder("central", "default", PluginDependenciesConfig.repoLoc).build())
        )
        logger.debug { "Remote server: " + PluginDependenciesConfig.repoLoc }
    }

    public fun resolveDependencies(deps: Collection<String>, vararg filters: DependencyFilter): DependencyResult {

        val dependencies: MutableList<Dependency> = ArrayList()
        for (library in deps) {
            val defaultArtifact = DefaultArtifact(library)
            val dependency = Dependency(defaultArtifact, null)
            dependencies.add(dependency)
        }
        return repository.resolveDependencies(
            session as RepositorySystemSession?, DependencyRequest(
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
