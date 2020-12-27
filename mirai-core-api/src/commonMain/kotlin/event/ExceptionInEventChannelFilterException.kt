/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.event

/**
 * 包装 [EventChannel.filter] 的 `filter` lambda 抛出的异常并重新抛出.
 *
 * @see EventChannel.filter
 */
public class ExceptionInEventChannelFilterException(
    /**
     * 当时正在处理的事件
     */
    public val event: Event,
    public val eventChannel: EventChannel<*>,
    override val message: String = "Exception in EventHandler",
    /**
     * 原异常
     */
    override val cause: Throwable
) : IllegalStateException()