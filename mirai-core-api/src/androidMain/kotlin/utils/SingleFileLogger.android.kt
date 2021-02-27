/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.utils

import net.mamoe.mirai.internal.utils.StdoutLogger
import java.io.File


/**
 * 将日志写入('append')到特定文件.
 *
 * @see PlatformLogger 查看格式信息
 */
public actual class SingleFileLogger actual constructor(
    identity: String,
    file: File
) : MiraiLogger by StdoutLogger(identity, { file.appendText(it + "\n") }) {
    public actual constructor(identity: String) : this(identity, File("$identity-${getCurrentDate()}.log"))

    init {
        file.createNewFile()
        require(file.isFile) { "Log file must be a file: $file" }
        require(file.canWrite()) { "Log file must be write: $file" }
    }
}
