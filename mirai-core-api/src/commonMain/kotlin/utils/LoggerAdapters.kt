/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.utils.internal.logging.JdkLogger
import net.mamoe.mirai.utils.internal.logging.Log4jLogger
import net.mamoe.mirai.utils.internal.logging.Slf4jLogger

public object LoggerAdapters {
    @JvmStatic
    public fun java.util.logging.Logger.asMiraiLogger(): MiraiLogger {
        return JdkLogger(this)
    }

    @JvmStatic
    public fun org.apache.logging.log4j.Logger.asMiraiLogger(): MiraiLogger {
        return Log4jLogger(this)
    }

    @JvmStatic
    public fun org.slf4j.Logger.asMiraiLogger(): MiraiLogger {
        return Slf4jLogger(this)
    }
}