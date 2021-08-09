/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.logging

import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.MiraiLogger
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.MarkerManager

/**
 * 使用 Log4J 接管 mirai 日志系统.
 */
@MiraiInternalApi
public class MiraiLog4JFactory : MiraiLogger.Factory {
    override fun create(requester: Class<*>, identity: String?): MiraiLogger {
        val logger = LogManager.getLogger(requester)
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        return net.mamoe.mirai.internal.utils.Log4jLoggerAdapter(
            logger,
            MarkerManager.getMarker(identity ?: logger.name).addParents(net.mamoe.mirai.internal.utils.MARKER_MIRAI)
        )
    }
}