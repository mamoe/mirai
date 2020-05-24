/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.plugins.builtin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.console.plugins.FilePluginDescription
import net.mamoe.mirai.console.plugins.PluginDependency
import net.mamoe.mirai.console.plugins.PluginDescription
import net.mamoe.mirai.console.plugins.PluginKind
import java.io.File

@Serializable
class JvmPluginDescription internal constructor(
    override val kind: PluginKind,
    override val name: String,
    @SerialName("main")
    val mainClassName: String,
    override val author: String = "",
    override val version: String,
    override val info: String = "",
    @SerialName("depends")
    override val dependencies: List<PluginDependency>
) : PluginDescription, FilePluginDescription {

    /**
     * 在手动实现时使用这个构造器.
     */
    @Suppress("unused")
    constructor(
        kind: PluginKind, name: String, mainClassName: String, author: String,
        version: String, info: String, depends: List<PluginDependency>,
        file: File
    ) : this(kind, name, mainClassName, author, version, info, depends) {
        this._file = file
    }

    override val file: File
        get() = _file ?: error("Internal error: JvmPluginDescription(name=$name)._file == null")


    @Suppress("PropertyName")
    @Transient
    @JvmField
    internal var _file: File? = null

    override fun toString(): String {
        return "JvmPluginDescription(kind=$kind, name='$name', mainClassName='$mainClassName', author='$author', version='$version', info='$info', dependencies=$dependencies, _file=$_file)"
    }
}