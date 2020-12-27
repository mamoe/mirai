/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

/**
 * 文件头和文件类型列表
 */
public val FILE_TYPES: MutableMap<String, String> = mutableMapOf(
    "FFD8FF" to "jpg",
    "89504E47" to "png",
    "47494638" to "gif",
    "49492A00" to "tif",
    "424D" to "bmp",
    "57415645" to "wav",


    "2321414D52" to "amr",
    "02232153494C4B5F5633" to "silk",
)

/*

        startsWith("FFD8") -> "jpg"
        startsWith("89504E47") -> "png"
        startsWith("47494638") -> "gif"
        startsWith("424D") -> "bmp"
 */

/**
 * 根据文件头获取文件类型
 */
public fun getFileType(fileHeader: ByteArray): String? {
    val hex = fileHeader.toUHexString("")
    FILE_TYPES.forEach { (k, v) ->
        if (hex.startsWith(k)) {
            return v
        }
    }
    return null
}