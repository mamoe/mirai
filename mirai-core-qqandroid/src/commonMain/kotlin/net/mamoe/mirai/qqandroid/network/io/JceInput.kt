package net.mamoe.mirai.qqandroid.network.io

import kotlinx.io.core.Input

@UseExperimental(ExperimentalUnsignedTypes::class)
inline class JceHead(private val value: Long) {
    val tag: Int get() = (value ushr 32).toInt()
    val type: Int get() = value.toUInt().toInt()
}

class JceInput(
    private val input: Input
): Input by input {

    private fun readHead(): JceHead {

    }
}