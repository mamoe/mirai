/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

public interface File {
    public val name: String
    public val path: String
    public val absolutePath: String

    public val length: Long

    public val parent: File?

    public val isFile: Boolean
    public val isDirectory: Boolean

    public fun exists(): Boolean

    public fun resolve(path: String): File
    public fun resolve(file: File): File

    public fun createNewFile()
    public fun delete()
    public fun mkdirs()

    public fun readText(): String
    public fun readBytes(): ByteArray

    public fun writeBytes(data: ByteArray)
    public fun writeText(text: String)

    public companion object {
        public fun create(path: String): File {
            TODO("")
        }
    }

}

public fun File.createFileIfNotExists() {
    if (!this.exists()) {
        this.parent?.mkdirs()
        this.createNewFile()
    }
}

public fun File.resolveCreateFile(relative: String): File = this.resolve(relative).apply { createFileIfNotExists() }
public fun File.resolveCreateFile(relative: File): File = this.resolve(relative).apply { createFileIfNotExists() }

public fun File.resolveMkdir(relative: String): File = this.resolve(relative).apply { mkdirs() }
public fun File.resolveMkdir(relative: File): File = this.resolve(relative).apply { mkdirs() }

public fun File.touch(): File = apply {
    parent?.mkdirs()
    createNewFile()
}
