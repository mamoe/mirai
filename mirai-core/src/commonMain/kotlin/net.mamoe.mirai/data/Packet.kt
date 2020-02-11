/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data

/**
 * 从服务器收到的包解析之后的结构化数据.
 * 它是一个数据包工厂的处理的返回值.
 */
interface Packet {
    /**
     * 实现这个接口的包将不会被记录到日志中
     */
    interface NoLog
}

object NoPacket : Packet {
    override fun toString(): String {
        return "NoPacket"
    }
}

/**
 * PacketFactory 可以一次解析多个包出来. 它们将会被分别广播.
 */
open class MultiPacket<P : Packet>(internal val delegate: List<P>) : List<P> by delegate, Packet {
    override fun toString(): String {
        return "MultiPacket<${this.firstOrNull()?.let { it::class.simpleName } ?: "?"}>"
    }
}