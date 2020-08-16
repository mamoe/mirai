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
import net.mamoe.mirai.console.internal.setting.SemverAsStringSerializerLoose
import net.mamoe.mirai.console.plugin.FilePluginDescription
import net.mamoe.mirai.console.plugin.PluginDependency
import net.mamoe.mirai.console.plugin.PluginDescription
import net.mamoe.mirai.console.plugin.PluginKind
import java.io.File

@Serializable
public class JvmPluginDescription internal constructor(
    public override val kind: PluginKind = PluginKind.NORMAL,
    public override val name: String,
    @SerialName("main")
    public val mainClassName: String,
    public override val author: String = "",
    public override val version: @Serializable(with = SemverAsStringSerializerLoose::class) Semver,
    public override val info: String = "",
    @SerialName("depends")
    public override val dependencies: List<@Serializable(with = PluginDependency.SmartSerializer::class) PluginDependency> = listOf()
) : PluginDescription, FilePluginDescription {

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

    public override val file: File
        get() = _file ?: error("Internal error: JvmPluginDescription(name=$name)._file == null")


    @Suppress("PropertyName")
    @Transient
    @JvmField
    internal var _file: File? = null

    public override fun toString(): String {
        return "JvmPluginDescription(kind=$kind, name='$name', mainClassName='$mainClassName', author='$author', version='$version', info='$info', dependencies=$dependencies, _file=$_file)"
    }
}