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

import net.mamoe.mirai.internal.utils.JdkLoggerAdapter
import net.mamoe.mirai.internal.utils.Log4jLoggerAdapter
import net.mamoe.mirai.internal.utils.MARKER_MIRAI
import net.mamoe.mirai.internal.utils.Slf4jLoggerAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager

/**
 * [SLF4J][org.slf4j.Logger], [LOG4J][org.apache.logging.log4j.Logger] 或 [JUL][java.util.logging.Logger] 到 [MiraiLogger] 的转换器.
 */
public object LoggerAdapters {
    /**
     * 使用 [LOG4J2][org.apache.logging.log4j.Logger] 接管全局 Mirai 日志系统. 请在调用 Mirai API 任何其他 API 前调用该方法.
     *
     * 注意, 若已经通过 service 方式提供 [MiraiLogger.Factory] 来接管日志系统, 则本方法无效.
     *
     * @since 2.7
     */
    @JvmStatic
    public fun useLog4j2() {
        MiraiLoggerFactoryImplementationBridge.wrapCurrent {
            object : MiraiLogger.Factory {
                override fun create(requester: Class<*>, identity: String?): MiraiLogger {
                    val logger = LogManager.getLogger(requester)
                    return Log4jLoggerAdapter(
                        logger,
                        MarkerManager.getMarker(identity ?: logger.name).addParents(MARKER_MIRAI)
                    )
                }
            }
        }
    }


    /**
     * 将 [java.util.logging.Logger] 转换作为 [MiraiLogger] 使用.
     */
    @JvmStatic
    public fun java.util.logging.Logger.asMiraiLogger(): MiraiLogger {
        return JdkLoggerAdapter(this)
    }

    /**
     * 将 [org.apache.logging.log4j.Logger] 转换作为 [MiraiLogger] 使用.
     */
    @JvmStatic
    public fun org.apache.logging.log4j.Logger.asMiraiLogger(): MiraiLogger {
        return Log4jLoggerAdapter(this, null)
    }

    /**
     * 将 [org.slf4j.Logger] 转换作为 [MiraiLogger] 使用.
     */
    @JvmStatic
    public fun org.slf4j.Logger.asMiraiLogger(): MiraiLogger {
        return Slf4jLoggerAdapter(this, null)
    }

    /**
     * 将 [org.apache.logging.log4j.Logger] 转换作为 [MiraiLogger] 使用.
     *
     * @since 2.7
     */
    @JvmStatic
    public fun org.apache.logging.log4j.Logger.asMiraiLogger(marker: Marker): MiraiLogger {
        return Log4jLoggerAdapter(this, marker)
    }
}