package net.mamoe.mirai.qqandroid.network.io

import kotlinx.io.core.BytePacketBuilder

abstract class JceStruct {
    abstract fun writeTo(builder: JceOutput)
}