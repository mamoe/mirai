package net.mamoe.mirai.message

/**
 * [Message] 在数据包中的 id([UByte])
 *
 * Java 调用方式:
 * MessageId.at
 *
 * @author Him188moe
 */
object MessageId {

    const val AT: Int = 0x06

    const val FACE: Int = 0x02

    const val TEXT: Int = 0x01

    const val IMAGE: Int = 0x03

    const val CHAIN: Int = 0xff//仅用于 equals. Packet 中不存在 Chain 概念
}