@file:Suppress("EXPERIMENTAL_API_USAGE")

package test

import net.mamoe.mirai.utils.cryptor.protoFieldNumber
import net.mamoe.mirai.utils.cryptor.protoType

intArrayOf(8, 18, 26, 34, 80).forEach {
    println(protoFieldNumber(it.toUInt()).toString() + " -> " + protoType(it.toUInt()))
}