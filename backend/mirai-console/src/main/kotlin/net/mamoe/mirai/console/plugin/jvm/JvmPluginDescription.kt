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

import com.vdurmont.semver4j.Semver
import net.mamoe.mirai.console.compiler.common.ResolveContext
import net.mamoe.mirai.console.compiler.common.ResolveContext.Kind.*
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import net.mamoe.mirai.console.plugin.description.VersionRequirement

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
        @Suppress("DEPRECATION_ERROR")
        @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
        @JvmSynthetic
        public operator fun invoke(
            /**
             * @see [PluginDescription.id]
             */
            @ResolveContext(PLUGIN_ID) id: String,
            /**
             * @see [PluginDescription.version]
             */
            @ResolveContext(PLUGIN_VERSION) version: Semver,
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
public class JvmPluginDescriptionBuilder
@Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
constructor(
    private var id: String,
    private var version: Semver,
) {
    @Suppress("DEPRECATION_ERROR")
    public constructor(
        @ResolveContext(PLUGIN_NAME) id: String,
        @ResolveContext(PLUGIN_VERSION) version: String,
    ) : this(id, Semver(version, Semver.SemverType.LOOSE))

    private var name: String = id
    private var author: String = ""
    private var info: String = ""
    private var dependencies: MutableSet<PluginDependency> = mutableSetOf()

    @ILoveKuriyamaMiraiForever
    public fun name(@ResolveContext(PLUGIN_NAME) value: String): JvmPluginDescriptionBuilder =
        apply { this.name = value.trim() }

    @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
    @ILoveKuriyamaMiraiForever
    public fun version(@ResolveContext(PLUGIN_VERSION) value: String): JvmPluginDescriptionBuilder =
        apply { this.version = Semver(value, Semver.SemverType.LOOSE) }

    @Deprecated("Semver 将会在 1.0-RC 被替换为 Console 自己实现的版本。请临时使用 String。", level = DeprecationLevel.ERROR)
    @ILoveKuriyamaMiraiForever
    public fun version(@ResolveContext(PLUGIN_VERSION) value: Semver): JvmPluginDescriptionBuilder =
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
     * @see PluginDependency
     */
    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        @ResolveContext(PLUGIN_ID) pluginId: String,
        isOptional: Boolean = false,
        versionRequirement: VersionRequirement,
    ): JvmPluginDescriptionBuilder = apply {
        this.dependencies.add(PluginDependency(pluginId, versionRequirement, isOptional))
    }

    /**
     * isOptional = false
     *
     * @see PluginDependency
     */
    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        @ResolveContext(PLUGIN_ID) pluginId: String,
        versionRequirement: VersionRequirement,
    ): JvmPluginDescriptionBuilder = apply {
        this.dependencies.add(PluginDependency(pluginId, versionRequirement, false))
    }

    /**
     * 无版本要求
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

    /**
     * 示例:
     *
     * ```
     * dependsOn("org.example.test-plugin") { "1.0.0".."1.2.0" }
     * dependsOn("org.example.test-plugin") { npmPattern("1.x || >=2.5.0 || 5.0.0 - 7.2.3") }
     * dependsOn("org.example.test-plugin") { ivyPattern("[1.0,2.0[") }
     * dependsOn("org.example.test-plugin") { custom { it.toString() == "1.0.0" } }
     * ```
     *
     * @see PluginDependency
     * @see VersionRequirement.Builder
     */
    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        @ResolveContext(PLUGIN_ID) pluginId: String,
        isOptional: Boolean = false,
        versionRequirement: VersionRequirement.Builder.() -> VersionRequirement,
    ): JvmPluginDescriptionBuilder =
        apply {
            this.dependencies.add(PluginDependency(pluginId,
                VersionRequirement.Builder().run(versionRequirement),
                isOptional))
        }


    @Suppress("DEPRECATION_ERROR")
    public fun build(): JvmPluginDescription =
        SimpleJvmPluginDescription(name, version, id, author, info, dependencies)

    /**
     * 标注一个 [JvmPluginDescription] DSL
     */
    @Suppress("SpellCheckingInspection")
    @Retention(AnnotationRetention.SOURCE)
    @DslMarker
    internal annotation class ILoveKuriyamaMiraiForever // https://zh.moegirl.org.cn/zh-cn/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5
}

/**
 * @constructor 推荐使用带名称的参数, 而不要按位置摆放.
 *
 * @see JvmPluginDescription
 */
@Deprecated(
    """
    将在 1.0-RC 删除. 请使用 JvmPluginDescription.
""",
    replaceWith = ReplaceWith(
        "JvmPluginDescription",
        "net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription"
    ),
    level = DeprecationLevel.ERROR
)
public data class SimpleJvmPluginDescription
@Deprecated(
    """
    构造器不稳定, 将在 1.0-RC 删除. 请使用 JvmPluginDescriptionBuilder.
""",
    replaceWith = ReplaceWith(
        "JvmPluginDescription(name, version) {}",
        "net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription.Companion.invoke"
    ),
    level = DeprecationLevel.ERROR
)
@JvmOverloads public constructor(
    public override val name: String,
    public override val version: Semver,
    public override val id: String = name,
    public override val author: String = "",
    public override val info: String = "",
    public override val dependencies: Set<PluginDependency> = setOf(),
) : JvmPluginDescription {

    @Deprecated(
        """
    构造器不稳定, 将在 1.0-RC 删除. 请使用 JvmPluginDescriptionBuilder.
""",
        replaceWith = ReplaceWith(
            "JvmPluginDescription.invoke(name, version) {}",
            "net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription.Companion.invoke"
        ),
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    @JvmOverloads
    public constructor(
        name: String,
        version: String,
        id: String = name,
        author: String = "",
        info: String = "",
        dependencies: Set<PluginDependency> = setOf(),
    ) : this(name, Semver(version, Semver.SemverType.LOOSE), id, author, info, dependencies)

    init {
        require(!name.contains(':')) { "':' is forbidden in plugin name" }
    }
}