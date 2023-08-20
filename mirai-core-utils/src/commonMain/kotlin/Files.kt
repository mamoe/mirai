/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

private class FileType(
    signature: String,
    val requiredHeaderSize: Int,
    val formatName: String
) {
    val signatureRegex = Regex(signature, RegexOption.IGNORE_CASE)
}

/**
 * 文件头和文件类型列表
 */
private val FILE_TYPES: List<FileType> = listOf(
    FileType("^FFD8FF", 3, "jpg"),
    FileType("^89504E47", 4, "png"),
    FileType("^47494638", 4, "gif"),
    FileType("^424D", 3, "bmp"),
    FileType("^2321414D52", 5, "amr"),
    FileType("^02232153494C4B5F5633", 10, "silk"),
    FileType("^([a-zA-Z0-9]{8})66747970", 8, "mp4"),

    //"49492A00" to "tif", // client doesn't support
    //"52494646" to "webp", // pc client doesn't support
    // "57415645" to "wav", // server doesn't support
)

/**
 * 在 [getFileType] 需要的 [ByteArray] 长度
 */
public val COUNT_BYTES_USED_FOR_DETECTING_FILE_TYPE: Int by lazy { FILE_TYPES.maxOf { it.requiredHeaderSize } }

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
    val hex = fileHeader.toUHexString(
        "",
        length = COUNT_BYTES_USED_FOR_DETECTING_FILE_TYPE.coerceAtMost(fileHeader.size)
    )
    FILE_TYPES.forEach { t ->
        if (hex.contains(t.signatureRegex)) {
            return t.formatName
        }
    }
    return null
}