/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "INVISIBLE_REFERENCE", "INVISIBLE_member")

package net.mamoe.mirai.console.plugin.jvm

import com.vdurmont.semver4j.Semver
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.description.PluginDescription
import kotlin.internal.LowPriorityInOverloadResolution

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
            name: String,
            version: String,
            block: JvmPluginDescriptionBuilder.() -> Unit = {}
        ): JvmPluginDescription = JvmPluginDescriptionBuilder(name, version).apply(block).build()

        /**
         * 构建 [JvmPluginDescription]
         * @see JvmPluginDescriptionBuilder
         */
        @JvmSynthetic
        public operator fun invoke(
            name: String,
            version: Semver,
            block: JvmPluginDescriptionBuilder.() -> Unit = {}
        ): JvmPluginDescription = JvmPluginDescriptionBuilder(name, version).apply(block).build()
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
    private var id: String,
    private var version: Semver,
) {
    public constructor(name: String, version: String) : this(name, Semver(version, Semver.SemverType.LOOSE))

    private var name: String = id
    private var author: String = ""
    private var info: String = ""
    private var dependencies: MutableSet<PluginDependency> = mutableSetOf()

    @ILoveKuriyamaMiraiForever
    public fun name(value: String): JvmPluginDescriptionBuilder = apply { this.name = value.trim() }

    @ILoveKuriyamaMiraiForever
    public fun version(value: String): JvmPluginDescriptionBuilder =
        apply { this.version = Semver(value, Semver.SemverType.LOOSE) }

    @ILoveKuriyamaMiraiForever
    public fun version(value: Semver): JvmPluginDescriptionBuilder = apply { this.version = value }

    @ILoveKuriyamaMiraiForever
    public fun id(value: String): JvmPluginDescriptionBuilder = apply { this.id = value.trim() }

    @ILoveKuriyamaMiraiForever
    public fun author(value: String): JvmPluginDescriptionBuilder = apply { this.author = value.trim() }

    @ILoveKuriyamaMiraiForever
    public fun info(value: String): JvmPluginDescriptionBuilder = apply { this.info = value.trimIndent() }

    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        pluginId: String,
        version: String? = null,
        isOptional: Boolean = false
    ): JvmPluginDescriptionBuilder = apply {
        if (version == null) this.dependencies.add(PluginDependency(pluginId, version, isOptional))
        else this.dependencies.add(PluginDependency(pluginId, version, isOptional))
    }

    @ILoveKuriyamaMiraiForever
    public fun setDependencies(
        value: Set<PluginDependency>
    ): JvmPluginDescriptionBuilder = apply {
        this.dependencies = value.toMutableSet()
    }

    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        vararg dependencies: PluginDependency
    ): JvmPluginDescriptionBuilder = apply {
        for (dependency in dependencies) {
            this.dependencies.add(dependency)
        }
    }

    @ILoveKuriyamaMiraiForever
    public fun dependsOn(
        pluginId: String,
        version: Semver? = null,
        isOptional: Boolean = false
    ): JvmPluginDescriptionBuilder = apply { this.dependencies.add(PluginDependency(pluginId, version, isOptional)) }


    @Suppress("DEPRECATION_ERROR")
    public fun build(): JvmPluginDescription =
        SimpleJvmPluginDescription(name, version, id, author, info, dependencies)

    @Retention(AnnotationRetention.SOURCE)
    @DslMarker
    private annotation class ILoveKuriyamaMiraiForever // https://zh.moegirl.org.cn/zh-cn/%E6%A0%97%E5%B1%B1%E6%9C%AA%E6%9D%A5
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


@Deprecated(
    "JvmPluginDescription 没有构造器. 请使用 SimpleJvmPluginDescription.",
    replaceWith = ReplaceWith(
        "SimpleJvmPluginDescription(name, version, author, info, dependencies, kind)",
        "net.mamoe.mirai.console.plugin.jvm.SimpleJvmPluginDescription"
    ),
    level = DeprecationLevel.WARNING
)
@LowPriorityInOverloadResolution
@Suppress("DEPRECATION_ERROR", "FunctionName")
public fun JvmPluginDescription(
    name: String,
    version: Semver,
    id: String = name,
    author: String = "",
    info: String = "",
    dependencies: Set<PluginDependency> = setOf(),
): JvmPluginDescription = SimpleJvmPluginDescription(name, version, id, author, info, dependencies)

@Deprecated(
    "JvmPluginDescription 没有构造器. 请使用 SimpleJvmPluginDescription.",
    replaceWith = ReplaceWith(
        "SimpleJvmPluginDescription(name, version, author, info, dependencies, kind)",
        "net.mamoe.mirai.console.plugin.jvm.SimpleJvmPluginDescription"
    ),
    level = DeprecationLevel.WARNING
)
@LowPriorityInOverloadResolution
@Suppress("DEPRECATION_ERROR", "FunctionName")
public fun JvmPluginDescription(
    name: String,
    version: String,
    id: String = name,
    author: String = "",
    info: String = "",
    dependencies: Set<PluginDependency> = setOf(),
): JvmPluginDescription = SimpleJvmPluginDescription(name, version, id, author, info, dependencies)
