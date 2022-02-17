/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.data.ReadOnlyPluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.logging.AbstractLoggerController
import net.mamoe.mirai.utils.MiraiExperimentalApi

@ConsoleFrontEndImplementation
@MiraiExperimentalApi
public class LoggerConfig : ReadOnlyPluginConfig("Logger") {
    @ValueDescription(
        """
        日志输出等级 可选值: ALL, VERBOSE, DEBUG, INFO, WARNING, ERROR, NONE
    """
    )
    public val defaultPriority: AbstractLoggerController.LogPriority by value(AbstractLoggerController.LogPriority.INFO)

    @ValueDescription(
        """
        特定日志记录器输出等级
    """
    )
    public val loggers: Map<String, AbstractLoggerController.LogPriority> by value(
        mapOf(
            "example.logger" to AbstractLoggerController.LogPriority.NONE,
            "console.debug" to AbstractLoggerController.LogPriority.NONE,
            "Bot" to AbstractLoggerController.LogPriority.ALL,
        )
    )

}
