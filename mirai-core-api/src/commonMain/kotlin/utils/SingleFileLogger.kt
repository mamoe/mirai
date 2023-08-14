/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import java.io.File

/**
 * 将日志写入('append')到特定文件.
 *
 * @see PlatformLogger 查看格式信息
 */
public expect class SingleFileLogger : MiraiLogger {
    public constructor(identity: String)
    public constructor(identity: String, file: File = File("$identity-${getCurrentDate()}.log"))

    // Implementation notes v2.5.0:
    // default argument `file` to produce synthetic constructor with `DefaultConstructorMarker` for binary compatibility
    // dedicated constructor with single parameter `identity` for the same reason.
}