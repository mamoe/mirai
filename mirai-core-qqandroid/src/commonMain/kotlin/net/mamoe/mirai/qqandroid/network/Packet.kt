/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network

/*

// moved to `mirai-core`

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
*/

/**
 * PacketFactory 可以一次解析多个包出来. 它们将会被分别广播.
 */
internal interface MultiPacket<out P : Packet> : Packet, Iterable<P>

internal open class MultiPacketByIterable<out P : Packet>(internal val delegate: Iterable<P>) : MultiPacket<P>,
    Iterable<P> by delegate {
    override fun toString(): String = "MultiPacketByIterable"
}

internal open class MultiPacketBySequence<out P : Packet>(internal val delegate: Sequence<P>) :
    MultiPacket<P> {
    override operator fun iterator(): Iterator<P> = delegate.iterator()

    override fun toString(): String = "MultiPacketBySequence"
}