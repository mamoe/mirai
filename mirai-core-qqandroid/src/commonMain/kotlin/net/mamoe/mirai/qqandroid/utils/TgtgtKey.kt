package net.mamoe.mirai.qqandroid.utils

import net.mamoe.mirai.utils.io.getRandomByteArray
import net.mamoe.mirai.utils.md5

fun generateTgtgtKey(guid: ByteArray): ByteArray =
    md5(getRandomByteArray(16) + guid)