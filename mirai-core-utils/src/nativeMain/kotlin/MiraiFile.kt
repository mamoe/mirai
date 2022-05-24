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
public actual interface MiraiFile {
    public actual val name: String
    public actual val parent: MiraiFile?
    public actual val absolutePath: String
    public actual val length: Long
    public actual val isFile: Boolean
    public actual val isDirectory: Boolean
    public actual fun exists(): Boolean
    public actual fun resolve(path: String): MiraiFile
    public actual fun resolve(file: MiraiFile): MiraiFile
    public actual fun createNewFile(): Boolean
    public actual fun delete(): Boolean
    public actual fun mkdir(): Boolean
    public actual fun mkdirs(): Boolean
    public actual fun input(): Input
    public actual fun output(): Output

    public actual companion object {
        public actual fun create(absolutePath: String): MiraiFile {
            TODO("Not yet implemented")
        }
    }
}