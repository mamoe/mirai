package net.mamoe.mirai.data

/**
 * 从服务器收到的包解析之后的结构化数据.
 */
interface Packet

/**
 * PacketFactory 可以一次解析多个包出来. 它们将会被分别广播.
 */
class MultiPacket<P : Packet>(delegate: List<P>) : List<P> by delegate, Packet {
    override fun toString(): String {
        return "MultiPacket<${this.firstOrNull()?.let { it::class.simpleName }?: "?"}>"
    }
}