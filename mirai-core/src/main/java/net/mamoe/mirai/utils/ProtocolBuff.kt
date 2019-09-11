package net.mamoe.mirai.utils

import java.io.DataOutputStream

/**
 * Google ProtocolBuff 的一些算法实现
 *
 * @author Him188moe
 */


/**
 * 128(10000000) -> 0x7F (10000000_10000001)
 *
 * TODO improve
 */
@ExperimentalUnsignedTypes
fun DataOutputStream.writeProtoFixedInt(int: Int) {
    if (int == 128) {
        this.writeShort(0x80_01)//unsigned//1000000010000001
        return
    }
    this.writeByte(int.rem(128) + 128)
    this.writeByte(int / 128)
}

/**
 * 127(1111111(7)) -> 0x7F (11111111(8))
 *
 * TODO improve
 */
@ExperimentalUnsignedTypes
fun DataOutputStream.writeProtoInt(int: Int) {
    if (int < 128) {
        this.writeByte(int and 0xFF)//10000000
        return
    }
    this.writeProtoFixedInt(int)
}


@ExperimentalUnsignedTypes
fun main() {
    println()
    println(lazyEncode {
        it.writeProtoInt(128)
    }.toUHexString())
}