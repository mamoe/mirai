/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.plugin.jvm.ExportManager
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.debug
import net.mamoe.mirai.utils.verbose
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.graph.DependencyFilter
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.zip.ZipFile

/*
Class resolving:

|
`- Resolve standard classes: by super class loader.
`- Resolve classes in shared libraries (Shared in all plugins)
|
|-===== SANDBOX =====
|
`- Resolve classes in plugin dependency shared libraries (Shared by depend-ed plugins)
`- Resolve classes in independent libraries (Can only be loaded by current plugin)
`- Resolve classes in current jar.
`- Resolve classes from other plugin jar

 */

internal class JvmPluginsLoadingCtx(
    val sharedLibrariesLoader: DynLibClassLoader,
    val pluginClassLoaders: MutableList<JvmPluginClassLoaderN>,
    val downloader: JvmPluginDependencyDownloader,
) {
    val sharedLibrariesDependencies = HashSet<String>()
    val sharedLibrariesFilter: DependencyFilter = DependencyFilter { node, _ ->
        return@DependencyFilter node.artifact.depId() !in sharedLibrariesDependencies
    }
}

internal class DynLibClassLoader(
    parent: ClassLoader?,
) : URLClassLoader(arrayOf(), parent) {
    companion object {
        init {
            ClassLoader.registerAsParallelCapable()
        }
    }

    internal fun addLib(url: URL) {
        addURL(url)
    }

    internal fun addLib(file: File) {
        addURL(file.toURI().toURL())
    }

    override fun toString(): String {
        return "DynLibClassLoader@" + hashCode()
    }
}

@Suppress("JoinDeclarationAndAssignment")
internal class JvmPluginClassLoaderN : URLClassLoader {
    val file: File
    val ctx: JvmPluginsLoadingCtx

    val dependencies: MutableCollection<JvmPluginClassLoaderN> = hashSetOf()

    lateinit var pluginSharedCL: DynLibClassLoader
    lateinit var pluginIndependentCL: DynLibClassLoader


    private constructor(file: File, ctx: JvmPluginsLoadingCtx, unused: Unit) : super(
        arrayOf(), ctx.sharedLibrariesLoader
    ) {
        this.file = file
        this.ctx = ctx
        init0()
    }

    private constructor(file: File, ctx: JvmPluginsLoadingCtx) : super(
        file.name,
        arrayOf(), ctx.sharedLibrariesLoader
    ) {
        this.file = file
        this.ctx = ctx
        init0()
    }

    private fun init0() {
        ZipFile(file).use { zipFile ->
            zipFile.entries().asSequence()
                .filter { it.name.endsWith(".class") }
                .map { it.name.substringBeforeLast('.') }
                .map { it.removePrefix("/").replace('/', '.') }
                .map { it.substringBeforeLast('.') }
                .forEach { pkg ->
                    pluginMainPackages.add(pkg)
                }
        }
        pluginSharedCL = DynLibClassLoader(ctx.sharedLibrariesLoader)
        pluginIndependentCL = DynLibClassLoader(pluginSharedCL)
        addURL(file.toURI().toURL())
    }

    private val pluginMainPackages: MutableSet<String> = HashSet()
    internal var declaredFilter: ExportManager? = null

    val sharedClLoadedDependencies = mutableSetOf<String>()
    internal fun containsSharedDependency(
        dependency: String
    ): Boolean {
        if (dependency in sharedClLoadedDependencies) return true
        return dependencies.any { it.containsSharedDependency(dependency) }
    }

    internal fun linkPluginSharedLibraries(logger: MiraiLogger, dependencies: Collection<String>) {
        linkLibraries(logger, dependencies, true)
    }

    internal fun linkPluginPrivateLibraries(logger: MiraiLogger, dependencies: Collection<String>) {
        linkLibraries(logger, dependencies, false)
    }

    private fun linkLibraries(logger: MiraiLogger, dependencies: Collection<String>, shared: Boolean) {
        if (dependencies.isEmpty()) return
        val results = ctx.downloader.resolveDependencies(
            dependencies, ctx.sharedLibrariesFilter,
            DependencyFilter { node, _ ->
                return@DependencyFilter !containsSharedDependency(node.artifact.depId())
            })
        val files = results.artifactResults.mapNotNull { result ->
            result.artifact?.let { it to it.file }
        }
        val linkType = if (shared) "(shared)" else "(private)"
        files.forEach { (artifact, lib) ->
            logger.verbose { "Linking $lib $linkType" }
            if (shared) {
                pluginSharedCL.addLib(lib)
                sharedClLoadedDependencies.add(artifact.depId())
            } else {
                pluginIndependentCL.addLib(lib)
            }
            logger.debug { "Linked $artifact $linkType" }
        }
    }

    companion object {
        private val java9: Boolean

        init {
            ClassLoader.registerAsParallelCapable()
            java9 = kotlin.runCatching { Class.forName("java.lang.Module") }.isSuccess
        }

        fun newLoader(file: File, ctx: JvmPluginsLoadingCtx): JvmPluginClassLoaderN {
            return when {
                java9 -> JvmPluginClassLoaderN(file, ctx)
                else -> JvmPluginClassLoaderN(file, ctx, Unit)
            }
        }
    }

    internal fun resolvePluginSharedLibAndPluginClass(name: String): Class<*>? {
        return try {
            pluginSharedCL.loadClass(name)
        } catch (e: ClassNotFoundException) {
            resolvePluginPublicClass(name)
        }
    }

    internal fun resolvePluginPublicClass(name: String): Class<*>? {
        if (pluginMainPackages.contains(name.pkgName())) {
            if (declaredFilter?.isExported(name) == false) return null
            return loadClass(name)
        }
        return null
    }

    override fun findClass(name: String): Class<*> {
        // Search dependencies first
        dependencies.forEach { dependency ->
            dependency.resolvePluginSharedLibAndPluginClass(name)?.let { return it }
        }
        // Search in independent class loader
        // @context: pluginIndependentCL.parent = pluinSharedCL
        try {
            return pluginIndependentCL.loadClass(name)
        } catch (ignored: ClassNotFoundException) {
        }

        try {
            return super.findClass(name)
        } catch (error: ClassNotFoundException) {
            // Finally, try search from other plugins
            ctx.pluginClassLoaders.forEach { other ->
                if (other !== this) {
                    other.resolvePluginPublicClass(name)?.let { return it }
                }
            }
            throw error
        }
    }

    internal fun loadedClass(name: String): Class<*>? = super.findLoadedClass(name)

    //// 只允许插件 getResource 时获取插件自身资源, https://github.com/mamoe/mirai-console/issues/205
    override fun getResources(name: String?): Enumeration<URL> = findResources(name)
    override fun getResource(name: String?): URL? = findResource(name)
    // getResourceAsStream 在 URLClassLoader 中通过 getResource 确定资源
    //      因此无需 override getResourceAsStream

    override fun toString(): String {
        return "JvmPluginClassLoader{${file.name}}"
    }
}

private fun String.pkgName(): String = substringBeforeLast('.', "")
internal fun Artifact.depId(): String = "$groupId:$artifactId"
