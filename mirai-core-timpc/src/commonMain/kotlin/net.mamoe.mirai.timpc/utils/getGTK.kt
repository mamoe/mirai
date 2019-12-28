package net.mamoe.mirai.timpc.utils

import kotlinx.io.core.toByteArray

private const val GTK_BASE_VALUE: Int = 5381

fun getGTK(sKey: String): Int {
    var value = GTK_BASE_VALUE
    for (c in sKey.toByteArray()) {
        value += (value shl 5) + c.toInt()
    }

    value = value and Int.MAX_VALUE
    return value
}
