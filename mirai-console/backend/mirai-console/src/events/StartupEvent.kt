/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.events

import net.mamoe.mirai.console.extensions.PostStartupExtension
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.console.internal.*
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 在 Console 启动完成后广播的事件
 * @property timestamp 启动完成的时间戳
 * @see MiraiConsoleImplementationBridge.doStart
 * @see PostStartupExtension
 * @since 2.15
 */
public class StartupEvent @MiraiInternalApi constructor(
    public val timestamp: Long
) : ConsoleEvent, AbstractEvent()