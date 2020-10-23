/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_REFERENCE", "INVISIBLE_member", "DeprecatedCallableAddReplaceWith")

package net.mamoe.mirai.console.plugin.jvm

import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.*
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.util.SemVersion

/**
 * JVM 插件的描述. 通常作为 `plugin.yml`
 *
 * 请不要自行实现 [JvmPluginDescription] 接口. 它不具有继承稳定性.
 *
 * 要查看相关约束, 参考 [PluginDescription]
 *
 * @see SimpleJvmPluginDescription
 * @see JvmPluginDescriptionBuilder
 */
public interface JvmPluginDescription : PluginDescription {
    public companion object {
        /**
         * 构建 [JvmPluginDescription]
         * @see JvmPluginDescriptionBuilder
         */
        @JvmName("create")
        @JvmSynthetic
        public operator fun invoke(
            /**
             * @see [PluginDescription.id]
             */
            @ResolveContext(PLUGIN_ID) id: String,
            /**
             * @see [PluginDescription.version]
             */
            @ResolveContext(PLUGIN_VERSION) version: String,
            /**
             * @see [PluginDescription.name]
             */
            @ResolveContext(PLUGIN_NAME) name: String = id,
            block: JvmPluginDescriptionBuilder.() -> Unit = {},
        ): JvmPluginDescription = JvmPluginDescriptionBuilder(id, version).apply { name(name) }.apply(block).build()

        /**
         * 构建 [JvmPluginDescription]
         * @see JvmPluginDescriptionBuilder
         */
        @JvmName("create")
        @JvmSynthetic
        public operator fun invoke(
            /**
             * @see [PluginDescription.id]
             */
            @ResolveContext(PLUGIN_ID) id: String,
            /**
             * @see [PluginDescription.version]
             */
            @ResolveContext(PLUGIN_VERSION) version: SemVersion,
            /**
             * @see [PluginDescription.name]
             */
            @ResolveContext(PLUGIN_NAME) name: String = id,
            block: JvmPluginDescriptionBuilder.() -> Unit = {},
        ): JvmPluginDescription = JvmPluginDescriptionBuilder(id, version).apply { name(name) }.apply(block).build()
    }
}

/**
 * [JvmPluginDescription] 构建器.
 *
 * #### Kotlin Example
 * ```
 * val desc = JvmPluginDescription("org.example.example-plugin", "1.0.0") {
 *    info("This is an example plugin")
 *    dependsOn("org.example.another-plugin")
 * }
 * ```
 *
 * #### Java Example
 * ```
 * JvmPluginDescription desc = new JvmPluginDescriptionBuilder("org.example.example-plugin", "1.0.0")
 *    .info("This is an example plugin")
 *    .dependsOn("org.example.another-plugin")
 *    .build()
 * ```
 *
 * @see [JvmPluginDescription.invoke]
 */
public class JvmPluginDescriptionBuilder(
    @ResolveContext(PLUGIN_ID) private var id: String,
    private var version: SemVersion,
) {
    public constructor(
        @ResolveContext(PLUGIN_ID) id: String,
        @ResolveContext(PLUGIN_VERSION) version: String,
    ) : this(id, SemVersion(version))

    private var name: String = id
    private var author: String = ""
    private var info: String = ""
    private var dependencies: MutableSet<PluginDependency> = mutableSetOf()

    @ILoveKuriyamaMiraiForever
    public fun name(@ResolveContext(PLUGIN_NAME) value: String): JvmPluginDescriptionBuilder =
        apply { this.name = value.trim() }

    @ILoveKuriyamaMiraiForever
    public fun version(@ResolveContext(PLUGIN_VERSION) value: String): JvmPluginDescriptionBuilder =
        apply { this.version = SemVersion(value) }

    @ILoveKuriyamaMiraiForever
    public fun version(@ResolveContext(PLUGIN_VERSION) value: SemVersion): JvmPluginDescriptionBuilder =
        apply { this.version = value }

    @ILoveKuriyamaMiraiForever
    public fun id(@ResolveContext(PLUGIN_ID) value: String): JvmPluginDescriptionBuilder =
        apply { this.id = value.trim() }

    @ILoveKuriyamaMiraiForever
    public fun author(value: String): JvmPluginDescriptionBuilder = apply { this.author = value.trim() }

    @ILoveKuriyamaMiraiForever
    public fun info(value: String): JvmPluginDescriptionBuilder = apply { this.info = value.trimIndent() }

    @ILoveKuriyamaMiraiForever
    public fun setDependencies(
        value: Set<PluginDependency>,
    ): JvmPluginDescriptionBuilder = apply {
        this.dependencies = value.toMutableSet()
    }

    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        vararg dependencies: PluginDependency,
    ): JvmPluginDescriptionBuilder = apply {
        for (dependency in dependencies) {
            this.dependencies.add(dependency)
        }
    }

    /**
     * isOptional = false
     *
     * @see PluginDependency
     */
    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        @ResolveContext(PLUGIN_ID) pluginId: String,
        versionRequirement: SemVersion.Requirement,
        isOptional: Boolean = false,
    ): JvmPluginDescriptionBuilder = apply {
        this.dependencies.add(PluginDependency(pluginId, versionRequirement, isOptional))
    }

    /**
     * @see PluginDependency
     */
    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        @ResolveContext(PLUGIN_ID) pluginId: String,
        @ResolveContext(VERSION_REQUIREMENT) versionRequirement: String,
        isOptional: Boolean = false,
    ): JvmPluginDescriptionBuilder = apply {
        this.dependencies.add(PluginDependency(pluginId, SemVersion.parseRangeRequirement(versionRequirement), isOptional))
    }

    /**
     * 无版本要求
     *
     * @param isOptional [PluginDependency.isOptional]
     *
     * @see PluginDependency
     */
    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        @ResolveContext(PLUGIN_ID) pluginId: String,
        isOptional: Boolean = false,
    ): JvmPluginDescriptionBuilder = apply {
        this.dependencies.add(PluginDependency(pluginId, null, isOptional))
    }


    public fun build(): JvmPluginDescription =
        @Suppress("DEPRECATION_ERROR")
        SimpleJvmPluginDescription(name, version, id, author, info, dependencies)

    /**
     * 标注一个 [JvmPluginDescription] DSL
     */
    @Suppress("SpellCheckingInspection")
    @Retention(AnnotationRetention.BINARY)
    @DslMarker
    internal annotation class ILoveKuriyamaMiraiForever // https://zh.moegirl.org.cn/zh-cn/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5
}

/**
 * @constructor 推荐使用带名称的参数, 而不要按位置摆放.
 *
 * @see JvmPluginDescription
 */
internal data class SimpleJvmPluginDescription
@JvmOverloads constructor(
    override val name: String,
    override val version: SemVersion,
    override val id: String = name,
    override val author: String = "",
    override val info: String = "",
    override val dependencies: Set<PluginDependency> = setOf(),
) : JvmPluginDescription {

    @Suppress("DEPRECATION_ERROR")
    @JvmOverloads
    constructor(
        name: String,
        version: String,
        id: String = name,
        author: String = "",
        info: String = "",
        dependencies: Set<PluginDependency> = setOf(),
    ) : this(name, SemVersion(version), id, author, info, dependencies)

    init {
        require(!name.contains(':')) { "':' is forbidden in plugin name" }
    }
}