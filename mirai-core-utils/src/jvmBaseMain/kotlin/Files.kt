/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import java.io.File


public fun File.createFileIfNotExists() {
    if (!this.exists()) {
        this.parentFile?.mkdirs()
        this.createNewFile()
    }
}

public fun File.resolveCreateFile(relative: String): File = this.resolve(relative).apply { createFileIfNotExists() }
public fun File.resolveCreateFile(relative: File): File = this.resolve(relative).apply { createFileIfNotExists() }

public fun File.resolveMkdir(relative: String): File = this.resolve(relative).apply { mkdirs() }
public fun File.resolveMkdir(relative: File): File = this.resolve(relative).apply { mkdirs() }

public fun File.touch(): File = apply {
    parentFile?.mkdirs()
    createNewFile()
}
