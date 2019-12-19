package net.mamoe.mirai.timpc.utils

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.utils.io.toUHexString

internal fun ByteReadPacket.unsupportedFlag(name: String, flag: String): Nothing =
    error("Unsupported flag of $name. flag=$flag, remaining=${readBytes().toUHexString()}")

internal fun ByteReadPacket.unsupportedType(name: String, type: String): Nothing =
    error("Unsupported type of $name. type=$type, remaining=${readBytes().toUHexString()}")
