/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugin.jvm

import com.vdurmont.semver4j.Semver
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.console.internal.data.SemverAsStringSerializerLoose
import net.mamoe.mirai.console.plugin.PluginDependency
import net.mamoe.mirai.console.plugin.PluginDescription
import net.mamoe.mirai.console.plugin.PluginKind
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import java.io.File

/**
 * @see KotlinMemoryPlugin 不需要 "plugin.yml", 不需要相关资源的在内存中加载的插件.
 */
@ConsoleExperimentalAPI
public data class JvmMemoryPluginDescription(
    public override val kind: PluginKind,
    public override val name: String,
    public override val author: String,
    public override val version: Semver,
    public override val info: String,
    public override val dependencies: List<PluginDependency>
) : JvmPluginDescription

/**
 * JVM 插件的描述. 通常作为 `plugin.yml`
 *
 *
 * ```yaml
 * # 必须. 插件名称, 允许空格, 允许中文, 不允许 ':'
 * name: "MyTestPlugin"
 *
 * # 必须. 插件主类, 即继承 KotlinPlugin 或 JavaPlugin 的类
 * main: org.example.MyPluginMain
 *
 * # 必须. 插件版本. 遵循《语义化版本 2.0.0》规范
 * version: 0.1.0
 *
 * # 可选. 插件种类.
 * # 'NORMAL': 表示普通插件
 * # 'LOADER': 表示提供扩展插件加载器的插件
 * kind: NORMAL
 *
 * # 可选. 插件描述
 * info: "这是一个测试插件"
 *
 * # 可选. 插件作者
 * author: "Mirai Example"
 *
 * # 可选. 插件依赖列表. 两种指定方式均可.
 * dependencies:
 * - name: "the"  # 依赖的插件名
 * version: null # 依赖的版本号, 支持 Apache Ivy 格式. 为 null 或不指定时不限制版本
 * isOptional: true # `true` 表示插件在找不到此依赖时也能正常加载
 * - "SamplePlugin" # 名称为 SamplePlugin 的插件, 不限制版本, isOptional=false
 * - "TestPlugin:1.0.0+" # 名称为 ExamplePlugin 的插件, 版本至少为 1.0.0, isOptional=false
 * - "ExamplePlugin:1.5.0+?" # 名称为 ExamplePlugin 的插件, 版本至少为 1.5.0, 末尾 `?` 表示 isOptional=true
 * - "Another test plugin:[1.0.0, 2.0.0)" # 名称为 Another test plugin 的插件, 版本要求大于等于 1.0.0, 小于 2.0.0, isOptional=false
 * ```
 */
public interface JvmPluginDescription : PluginDescription

@MiraiExperimentalAPI
@Serializable
public class JvmPluginDescriptionImpl internal constructor(
    public override val kind: PluginKind = PluginKind.NORMAL,
    public override val name: String,
    @SerialName("main")
    public val mainClassName: String,
    public override val author: String = "",
    public override val version: @Serializable(with = SemverAsStringSerializerLoose::class) Semver,
    public override val info: String = "",
    @SerialName("depends")
    public override val dependencies: List<@Serializable(with = PluginDependency.SmartSerializer::class) PluginDependency> = listOf()
) : JvmPluginDescription {

    /**
     * 在手动实现时使用这个构造器.
     */
    @Suppress("unused")
    public constructor(
        kind: PluginKind, name: String, mainClassName: String, author: String,
        version: Semver, info: String, depends: List<PluginDependency>,
        file: File
    ) : this(kind, name, mainClassName, author, version, info, depends) {
        this._file = file
    }

    public val file: File
        get() = _file ?: error("Internal error: JvmPluginDescription(name=$name)._file == null")


    @Suppress("PropertyName")
    @Transient
    @JvmField
    internal var _file: File? = null

    public override fun toString(): String {
        return "JvmPluginDescription(kind=$kind, name='$name', mainClassName='$mainClassName', author='$author', version='$version', info='$info', dependencies=$dependencies, _file=$_file)"
    }
}