/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 从服务器收到的包解析之后的结构化数据.
 * 它是一个数据包工厂的处理的返回值.
 *
 * **InternalAPI**: 这是内部 API, 它随时都有可能被修改
 */
@MiraiInternalApi
public interface Packet {
    /**
     * 实现这个接口的包将不会被记录到日志中
     */
    @MiraiInternalApi
    public interface NoLog

    /**
     * 实现这个接口的 [Event] 不会被作为事件记录到日志中
     */
    @MiraiInternalApi
    public interface NoEventLog
}
