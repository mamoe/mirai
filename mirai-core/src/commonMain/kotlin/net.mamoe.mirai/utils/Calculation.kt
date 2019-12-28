package net.mamoe.mirai.utils

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.toByteArray
import kotlinx.io.core.writeFully
import net.mamoe.mirai.utils.io.getRandomByteArray

fun md5(str: String): ByteArray = md5(str.toByteArray())