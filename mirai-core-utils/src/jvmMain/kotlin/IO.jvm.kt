/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries

public val Path.isFile: Boolean get() = Files.exists(this) && !Files.isDirectory(this)

public inline fun Path.mkdir() {
    Files.createDirectory(this)
}

public inline fun Path.mkdirs() {
    Files.createDirectories(this)
}

public fun Path.mkParentDirs() {
    val current = parent ?: return
    if (current == this) return
    if (current.exists()) return
    current.mkParentDirs()
    current.mkdir()
}

public fun Path.deleteRecursivelyMirai(): Boolean { // Kotlin added `Path.deleteRecursively()` in 1.8.0 but was experimental
    if (isFile) return deleteIfExists()
    if (isDirectory()) {
        listDirectoryEntries().forEach { it.deleteRecursivelyMirai() }
        return deleteIfExists()
    }
    return false
}
