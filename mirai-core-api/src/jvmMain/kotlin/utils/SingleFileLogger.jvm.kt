/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


@file:Suppress("unused")

package net.mamoe.mirai.utils

import java.io.File


/**
 * 将日志写入('append')到特定文件.
 *
 * @see PlatformLogger 查看格式信息
 */
@OptIn(MiraiInternalApi::class)
public actual class SingleFileLogger actual constructor(
    identity: String,
    file: File
) : MiraiLogger, PlatformLogger(identity, { file.appendText(it + "\n") }) {
    // Implementation notes v2.5.0:
    // Extending `PlatformLogger` for binary compatibility for JVM target only.
    // See actual declaration in androidMain for a better impl (implements `MiraiLogger` only)

    public actual constructor(identity: String) : this(identity, File("$identity-${getCurrentDate()}.log"))

    init {
        file.createNewFile()
        require(file.isFile) { "Log file must be a file: $file" }
        require(file.canWrite()) { "Log file must be write: $file" }
    }
}
