/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*

/**
 * Multiplatform implementation of file operations.
 */
public expect interface MiraiFile {
    /**
     * Name of this file or directory. Can be '.' and '..' if created by
     */
    public val name: String

    /**
     * Parent of this file or directory.
     */
    public val parent: MiraiFile?

    /**
     * Input path from [create].
     */
    public val path: String

    /**
     * Normalized absolute [path].
     */
    public val absolutePath: String

    public val length: Long

    public val isFile: Boolean
    public val isDirectory: Boolean

    public fun exists(): Boolean

    /**
     * Resolves a [MiraiFile] representing the [path] based on this [MiraiFile]. Result path is not guaranteed to be normalized.
     */
    public fun resolve(path: String): MiraiFile
    public fun resolve(file: MiraiFile): MiraiFile

    public fun createNewFile(): Boolean
    public fun delete(): Boolean

    public fun mkdir(): Boolean
    public fun mkdirs(): Boolean

    public fun input(): Input
    public fun output(): Output

    public companion object {
        public fun create(path: String): MiraiFile

        public fun getWorkingDir(): MiraiFile
    }

}

public expect fun MiraiFile.deleteRecursively(): Boolean

public fun MiraiFile.writeBytes(data: ByteArray) {
    return output().use { it.writeFully(data) }
}

public fun MiraiFile.writeText(text: String) {
    return output().use { it.writeText(text) }
}

public fun MiraiFile.readText(): String {
    return input().use { it.readAllText() }
}

public fun MiraiFile.readBytes(): ByteArray {
    return input().use { it.readBytes() }
}


public fun MiraiFile.createFileIfNotExists() {
    if (!this.exists()) {
        this.parent?.mkdirs()
        this.createNewFile()
    }
}

public fun MiraiFile.resolveCreateFile(relative: String): MiraiFile =
    this.resolve(relative).apply { createFileIfNotExists() }

public fun MiraiFile.resolveCreateFile(relative: MiraiFile): MiraiFile =
    this.resolve(relative).apply { createFileIfNotExists() }

public fun MiraiFile.resolveMkdir(relative: String): MiraiFile = this.resolve(relative).apply { mkdirs() }
public fun MiraiFile.resolveMkdir(relative: MiraiFile): MiraiFile = this.resolve(relative).apply { mkdirs() }

public fun MiraiFile.touch(): MiraiFile = apply {
    parent?.mkdirs()
    createNewFile()
}
