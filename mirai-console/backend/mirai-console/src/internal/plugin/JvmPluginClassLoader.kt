/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.console.plugin.jvm.ExportManager
import net.mamoe.mirai.console.plugin.jvm.JvmPluginClasspath
import net.mamoe.mirai.utils.*
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.graph.DependencyFilter
import java.io.File
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipFile

/*
Class resolving:

|
`- Resolve standard classes: hard linked by console (@see AllDependenciesClassesHolder)
`- Resolve classes in shared libraries (Shared in all plugins)
|
|-===== SANDBOX =====
|
`- Resolve classes in plugin dependency shared libraries (Shared by depend-ed plugins)
`- Resolve classes in independent libraries (Can only be loaded by current plugin)
`- Resolve classes in current jar.
`- Resolve classes from other plugin jar
`- Resolve by AppClassLoader

 */

internal class JvmPluginsLoadingCtx(
    val consoleClassLoader: ClassLoader, // plugin system -> mirai-console classloader WRAPPER
    val sharedLibrariesLoader: DynLibClassLoader,
    val pluginClassLoaders: MutableList<JvmPluginClassLoaderN>,
    val downloader: JvmPluginDependencyDownloader,
) {
    val sharedLibrariesDependencies = HashSet<String>()
    val sharedLibrariesFilter: DependencyFilter = DependencyFilter { node, _ ->
        return@DependencyFilter node.artifact.depId() !in sharedLibrariesDependencies
    }
}

internal open class DynamicClasspathClassLoader : URLClassLoader {
    internal constructor(urls: Array<URL>, parent: ClassLoader?) : super(urls, parent)

    @Suppress("Since15")
    internal constructor(urls: Array<URL>, parent: ClassLoader?, vmName: String?) : super(vmName, urls, parent)

    internal fun addLib(url: URL) {
        addURL(url)
    }

    internal fun addLib(file: File) {
        addURL(file.toURI().toURL())
    }


    companion object {
        internal val java9: Boolean

        init {
            ClassLoader.registerAsParallelCapable()
            java9 = kotlin.runCatching { Class.forName("java.lang.Module") }.isSuccess
        }
    }
}

internal class LegacyCompatibilityLayerClassLoader : DynamicClasspathClassLoader {
    private constructor(parent: ClassLoader?) : super(arrayOf(), parent)
    private constructor(parent: ClassLoader?, vmName: String?) : super(arrayOf(), parent, vmName)

    override fun toString(): String {
        return "LegacyCompatibilityLayerClassLoader@" + hashCode()
    }

    companion object {
        init {
            ClassLoader.registerAsParallelCapable()
        }

        fun newInstance(parent: ClassLoader?): LegacyCompatibilityLayerClassLoader {
            return if (java9) {
                LegacyCompatibilityLayerClassLoader(parent, "legacy-compatibility-layer")
            } else {
                LegacyCompatibilityLayerClassLoader(parent)
            }
        }
    }
}


internal class DynLibClassLoader : DynamicClasspathClassLoader {
    private val clName: String?
    internal var dependencies: List<DynLibClassLoader> = emptyList()

    private constructor(parent: ClassLoader?, clName: String?) : super(arrayOf(), parent) {
        this.clName = clName
    }

    private constructor(parent: ClassLoader?, clName: String?, vmName: String?) : super(arrayOf(), parent, vmName) {
        this.clName = clName
    }


    companion object {
        fun newInstance(parent: ClassLoader?, clName: String?, vmName: String?): DynLibClassLoader {
            return when {
                java9 -> DynLibClassLoader(parent, clName, vmName)
                else -> DynLibClassLoader(parent, clName)
            }
        }

        fun tryFastOrStrictResolve(name: String): Class<*>? {
            if (name.startsWith("java.")) return Class.forName(name, false, JavaSystemPlatformClassLoader)

            // All mirai-core hard-linked should use same version to avoid errors (ClassCastException).
            if (name in AllDependenciesClassesHolder.allclasses) {
                return AllDependenciesClassesHolder.appClassLoader.loadClass(name)
            }
            if (
                name.startsWith("net.mamoe.mirai.")
                || name.startsWith("kotlin.")
                || name.startsWith("kotlinx.")
                || name.startsWith("org.slf4j.")
            ) { // Avoid plugin classing cheating
                try {
                    return AllDependenciesClassesHolder.appClassLoader.loadClass(name)
                } catch (ignored: ClassNotFoundException) {
                }
            }
            try {
                return Class.forName(name, false, JavaSystemPlatformClassLoader)
            } catch (ignored: ClassNotFoundException) {
            }
            return null
        }
    }

    internal fun loadClassInThisClassLoader(name: String): Class<*>? {
        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }
            try {
                return findClass(name)
            } catch (ignored: ClassNotFoundException) {
            }
        }
        return null
    }

    override fun toString(): String {
        clName?.let { return "DynLibClassLoader{$it}" }
        return "DynLibClassLoader@" + hashCode()
    }

    override fun getResource(name: String?): URL? {
        if (name == null) return null
        findResource(name)?.let { return it }
        if (parent is DynLibClassLoader) {
            return parent.getResource(name)
        }
        return null
    }

    override fun getResources(name: String?): Enumeration<URL> {
        if (name == null) return Collections.emptyEnumeration()
        val res = findResources(name)
        return if (parent is DynLibClassLoader) {
            res + parent.getResources(name)
        } else {
            res
        }
    }

    internal fun findButNoSystem(name: String): Class<*>? = findButNoSystem(name, mutableListOf())
    private fun findButNoSystem(name: String, track: MutableList<DynLibClassLoader>): Class<*>? {
        if (name.startsWith("java.")) return null

        // Skip duplicated searching, for faster speed.
        if (this in track) return null
        track.add(this)

        val pt = this.parent
        if (pt is DynLibClassLoader) {
            pt.findButNoSystem(name, track)?.let { return it }
        }
        dependencies.forEach { dep ->
            dep.findButNoSystem(name, track)?.let { return it }
        }

        synchronized(getClassLoadingLock(name)) {
            findLoadedClass(name)?.let { return it }
            try {
                findClass(name)?.let { return it }
            } catch (ignored: ClassNotFoundException) {
            }
        }
        return null
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        tryFastOrStrictResolve(name)?.let { return it }

        findButNoSystem(name)?.let { return it }

        val topParent = generateSequence<ClassLoader>(this) { it.parent }.firstOrNull { it !is DynLibClassLoader }
        return Class.forName(name, false, topParent)
    }
}

internal class JvmPluginClassLoaderN : URLClassLoader {
    val openaccess: JvmPluginClasspath = OpenAccess()
    val file: File
    val ctx: JvmPluginsLoadingCtx
    val sharedLibrariesLogger: DynLibClassLoader

    val dependencies: MutableCollection<JvmPluginClassLoaderN> = hashSetOf()

    lateinit var pluginSharedCL: DynLibClassLoader
    lateinit var pluginIndependentCL: DynLibClassLoader

    @Suppress("PrivatePropertyName")
    private val file_: File
        get() = file

    var linkedLogger by lateinitMutableProperty {
        MiraiLogger.Factory.create(
            JvmPluginClassLoaderN::class,
            "JvmPlugin[" + file_.name + "]"
        )
    }
    val undefinedDependencies = mutableSetOf<String>()

    @Suppress("UNUSED_PARAMETER")
    private constructor(file: File, ctx: JvmPluginsLoadingCtx, unused: Unit) : super(
        arrayOf(), ctx.sharedLibrariesLoader
    ) {
        this.sharedLibrariesLogger = ctx.sharedLibrariesLoader
        this.file = file
        this.ctx = ctx
        init1()
    }

    @Suppress("Since15")
    private constructor(file: File, ctx: JvmPluginsLoadingCtx) : super(
        file.name,
        arrayOf(), ctx.sharedLibrariesLoader
    ) {
        this.sharedLibrariesLogger = ctx.sharedLibrariesLoader
        this.file = file
        this.ctx = ctx
        init1()
    }

    private fun init1() {
        try {
            init0()
        } catch (e: Throwable) {
            e.addSuppressed(RuntimeException("Failed to initialize new JvmPluginClassLoader, file=$file"))
            throw e
        }
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
        pluginSharedCL = DynLibClassLoader.newInstance(
            ctx.sharedLibrariesLoader, "SharedCL{${file.name}}", "${file.name}[shared]"
        )
        pluginIndependentCL = DynLibClassLoader.newInstance(
            pluginSharedCL, "IndependentCL{${file.name}}", "${file.name}[private]"
        )
        pluginSharedCL.dependencies = mutableListOf()
        addURL(file.toURI().toURL())
    }

    private val pluginMainPackages: MutableSet<String> = HashSet()
    internal var declaredFilter: ExportManager? = null

    val sharedClLoadedDependencies = mutableSetOf<String>()
    val privateClLoadedDependencies = mutableSetOf<String>()
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

    private val isPluginLibrariesLinked = AtomicBoolean(false)

    fun linkPluginLibraries(logger: MiraiLogger) {
        if (!isPluginLibrariesLinked.compareAndSet(false, true)) return

        // Link jar dependencies
        fun InputStream?.readDependencies(): Collection<String> {
            if (this == null) return emptyList()
            return bufferedReader().useLines { lines ->
                lines.filterNot { it.isBlank() }
                    .filterNot { it.startsWith('#') }
                    .map { it.trim() }
                    .toMutableList()
            }
        }
        linkPluginSharedLibraries(
            logger,
            getResourceAsStream("META-INF/mirai-console-plugin/dependencies-shared.txt").readDependencies()
        )
        linkPluginPrivateLibraries(
            logger,
            getResourceAsStream("META-INF/mirai-console-plugin/dependencies-private.txt").readDependencies()
        )
    }

    private fun linkLibraries(logger: MiraiLogger, dependencies: Collection<String>, shared: Boolean) {
        if (dependencies.isEmpty()) return
        val results = ctx.downloader.resolveDependencies(
            dependencies, ctx.sharedLibrariesFilter,
            DependencyFilter filter@{ node, _ ->
                val depid = node.artifact.depId()
                if (containsSharedDependency(depid)) return@filter false
                if (depid in privateClLoadedDependencies) return@filter false
                return@filter true
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
                privateClLoadedDependencies.add(artifact.depId())
            }
            logger.debug { "Linked $artifact $linkType <${if (shared) pluginSharedCL else pluginIndependentCL}>" }
        }
    }

    companion object {

        init {
            ClassLoader.registerAsParallelCapable()
        }

        fun newLoader(file: File, ctx: JvmPluginsLoadingCtx): JvmPluginClassLoaderN {
            return when {
                DynamicClasspathClassLoader.java9 -> JvmPluginClassLoaderN(file, ctx)
                else -> JvmPluginClassLoaderN(file, ctx, Unit)
            }
        }
    }

    internal fun resolvePluginSharedLibAndPluginClass(name: String): Class<*>? {
        return try {
            pluginSharedCL.findButNoSystem(name)
        } catch (e: ClassNotFoundException) {
            null
        } ?: resolvePluginPublicClass(name)
    }

    internal fun resolvePluginPublicClass(name: String): Class<*>? {
        if (pluginMainPackages.contains(name.pkgName())) {
            if (declaredFilter?.isExported(name) == false) return null
            synchronized(getClassLoadingLock(name)) {
                findLoadedClass(name)?.let { return it }
                try {
                    return super.findClass(name)
                } catch (ignored: ClassNotFoundException) {
                }
            }
        }
        return null
    }

    override fun loadClass(name: String, resolve: Boolean): Class<*> = loadClass(name)

    override fun loadClass(name: String): Class<*> {
        DynLibClassLoader.tryFastOrStrictResolve(name)?.let { return it }

        sharedLibrariesLogger.loadClassInThisClassLoader(name)?.let { return it }

        // Search dependencies first
        dependencies.forEach { dependency ->
            dependency.resolvePluginSharedLibAndPluginClass(name)?.let { return it }
        }
        // Search in independent class loader
        // @context: pluginIndependentCL.parent = pluinSharedCL
        try {
            pluginIndependentCL.findButNoSystem(name)?.let { return it }
        } catch (ignored: ClassNotFoundException) {
        }

        try {
            synchronized(getClassLoadingLock(name)) {
                findLoadedClass(name)?.let { return it }
                return super.findClass(name)
            }
        } catch (error: ClassNotFoundException) {
            // Finally, try search from other plugins and console system
            ctx.pluginClassLoaders.forEach { other ->
                if (other !== this && other !in dependencies) {
                    other.resolvePluginPublicClass(name)?.let {
                        if (undefinedDependencies.add(other.file.name)) {
                            linkedLogger.warning { "Linked class $name in ${other.file.name} but plugin not depend on it." }
                            linkedLogger.warning { "Class loading logic may change in feature." }
                        }
                        return it
                    }
                }
            }
            return ctx.consoleClassLoader.loadClass(name)
        }
    }

    internal fun loadedClass(name: String): Class<*>? = super.findLoadedClass(name)

    private fun getRes(name: String, shared: Boolean): Enumeration<URL> {
        val src = mutableListOf<Enumeration<URL>>(
            findResources(name),
        )
        if (dependencies.isEmpty()) {
            if (shared) {
                src.add(sharedLibrariesLogger.getResources(name))
            }
        } else {
            dependencies.forEach { dep ->
                src.add(dep.getRes(name, false))
            }
        }
        src.add(pluginIndependentCL.getResources(name))

        val resolved = mutableListOf<URL>()
        src.forEach { nested ->
            nested.iterator().forEach { url ->
                if (url !in resolved)
                    resolved.add(url)
            }
        }

        return Collections.enumeration(resolved)
    }

    override fun getResources(name: String?): Enumeration<URL> {
        name ?: return Collections.emptyEnumeration()

        if (name.startsWith("META-INF/mirai-console-plugin/"))
            return findResources(name)
        // Avoid loading duplicated mirai-console plugins
        if (name.startsWith("META-INF/services/net.mamoe.mirai.console.plugin."))
            return findResources(name)

        return getRes(name, true)
    }

    override fun getResource(name: String?): URL? {
        name ?: return null
        if (name.startsWith("META-INF/mirai-console-plugin/"))
            return findResource(name)
        // Avoid loading duplicated mirai-console plugins
        if (name.startsWith("META-INF/services/net.mamoe.mirai.console.plugin."))
            return findResource(name)

        findResource(name)?.let { return it }
        // parent: ctx.sharedLibrariesLoader
        sharedLibrariesLogger.getResource(name)?.let { return it }
        dependencies.forEach { dep ->
            dep.getResource(name)?.let { return it }
        }
        return pluginIndependentCL.getResource(name)
    }

    override fun toString(): String {
        return "JvmPluginClassLoader{${file.name}}"
    }

    inner class OpenAccess : JvmPluginClasspath {
        override val pluginFile: File
            get() = this@JvmPluginClassLoaderN.file

        override val pluginClassLoader: ClassLoader
            get() = this@JvmPluginClassLoaderN

        override val pluginSharedLibrariesClassLoader: ClassLoader
            get() = pluginSharedCL
        override val pluginIndependentLibrariesClassLoader: ClassLoader
            get() = pluginIndependentCL

        private val permitted by lazy {
            arrayOf(
                this@JvmPluginClassLoaderN,
                pluginSharedCL,
                pluginIndependentCL,
            )
        }

        override fun addToPath(classLoader: ClassLoader, file: File) {
            if (classLoader !in permitted) {
                throw IllegalArgumentException("Unsupported classloader or cross plugin accessing: $classLoader")
            }
            if (classLoader == this@JvmPluginClassLoaderN) {
                this@JvmPluginClassLoaderN.addURL(file.toURI().toURL())
                return
            }
            classLoader as DynLibClassLoader
            classLoader.addLib(file)
        }

        override fun downloadAndAddToPath(classLoader: ClassLoader, dependencies: Collection<String>) {
            if (classLoader !in permitted) {
                throw IllegalArgumentException("Unsupported classloader or cross plugin accessing: $classLoader")
            }
            if (classLoader === this@JvmPluginClassLoaderN) {
                throw IllegalArgumentException("Only support download dependencies to `plugin[Shared/Independent]LibrariesClassLoader`")
            }
            this@JvmPluginClassLoaderN.linkLibraries(
                linkedLogger, dependencies, classLoader === pluginSharedCL
            )
        }
    }
}

private val JavaSystemPlatformClassLoader: ClassLoader by lazy {
    kotlin.runCatching {
        ClassLoader::class.java.methods.asSequence().filter {
            it.name == "getPlatformClassLoader"
        }.filter {
            java.lang.reflect.Modifier.isStatic(it.modifiers)
        }.firstOrNull()?.invoke(null) as ClassLoader?
    }.getOrNull() ?: ClassLoader.getSystemClassLoader().parent
}

private fun String.pkgName(): String = substringBeforeLast('.', "")
internal fun Artifact.depId(): String = "$groupId:$artifactId"

private operator fun <E> Enumeration<E>.plus(next: Enumeration<E>): Enumeration<E> {
    return compoundEnumerations(listOf(this, next).iterator())
}

private fun <E> compoundEnumerations(iter: Iterator<Enumeration<E>>): Enumeration<E> {
    return object : Enumeration<E> {
        private lateinit var crt: Enumeration<E>

        private var hasMore: Boolean = false
        private var fetched: Boolean = false

        override tailrec fun hasMoreElements(): Boolean {
            if (fetched) return hasMore
            if (::crt.isInitialized) {
                hasMore = crt.hasMoreElements()
                if (hasMore) {
                    fetched = true
                    return true
                }
            }
            if (!iter.hasNext()) {
                fetched = true
                hasMore = false
                return false
            }
            crt = iter.next()
            return hasMoreElements()
        }

        override fun nextElement(): E {
            if (hasMoreElements()) {
                return crt.nextElement().also {
                    fetched = false
                }
            }
            throw NoSuchElementException()
        }
    }
}
