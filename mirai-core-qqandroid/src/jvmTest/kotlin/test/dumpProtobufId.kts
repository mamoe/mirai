@file:Suppress("EXPERIMENTAL_API_USAGE")

package test

import net.mamoe.mirai.utils.cryptor.protoFieldNumber

intArrayOf(10, 18, 26, 34, 42, 50, 58, 66, 74).forEach {
    println(protoFieldNumber(it.toUInt()))
}