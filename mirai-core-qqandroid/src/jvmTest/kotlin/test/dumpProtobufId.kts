@file:Suppress("EXPERIMENTAL_API_USAGE")

package test

import net.mamoe.mirai.utils.cryptor.protoFieldNumber
import net.mamoe.mirai.utils.cryptor.protoType

intArrayOf(
    8, 16, 24, 32, 40, 48, 56, 64, 74, 82
).forEach {
    println(protoFieldNumber(it.toUInt()).toString() + " -> " + protoType(it.toUInt()))
}