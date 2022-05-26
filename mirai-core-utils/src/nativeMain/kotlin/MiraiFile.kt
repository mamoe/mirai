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
actual interface MiraiFile {
    actual val name: String
    actual val parent: MiraiFile?
    actual val absolutePath: String
    actual val length: Long
    actual val isFile: Boolean
    actual val isDirectory: Boolean
    actual fun exists(): Boolean
    actual fun resolve(path: String): MiraiFile
    actual fun resolve(file: MiraiFile): MiraiFile
    actual fun createNewFile(): Boolean
    actual fun delete(): Boolean
    actual fun mkdir(): Boolean
    actual fun mkdirs(): Boolean
    actual fun input(): Input
    actual fun output(): Output

    actual companion object {
        actual fun create(absolutePath: String): MiraiFile {
            TODO("Not yet implemented")
        }
    }
}