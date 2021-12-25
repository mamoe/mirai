/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")
@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.utils

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

public fun randomImage_android(): Bitmap {
    val width = (500..800).random()
    val height = (500..800).random()
    val bitmap = Bitmap.createBitmap(
        width, height, Bitmap.Config.RGB_565
    )
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(
                x, y,
                (0..0xFFFFFF).random()
            )
        }
    }
    return bitmap
}

public fun Bitmap.saveToBytes(): ByteArray = ByteArrayOutputStream().also { output ->
    compress(Bitmap.CompressFormat.PNG, 100, output)
}.toByteArray()

public actual fun randomImageContent(): ByteArray {
    return randomImage_android().saveToBytes()
}

