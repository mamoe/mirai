package net.mamoe.mirai.qqandroid.network

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 从服务器收到的包解析之后的结构化数据.
 * 它是一个数据包工厂的处理的返回值.
 *
 * **InternalAPI**: 这是内部 API, 它随时都有可能被修改
 */
@MiraiInternalAPI
interface Packet {
    /**
     * 实现这个接口的包将不会被记录到日志中
     */
    interface NoLog

    /**
     * 实现这个接口的 [Event] 不会被作为事件记录到日志中
     */
    interface NoEventLog
}
