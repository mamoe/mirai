/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:JvmName("ImagesImplKtAndroid.kt")

package net.mamoe.mirai.internal.message

import android.graphics.BitmapFactory
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.runBIO

internal actual suspend fun ExternalResource.getImageInfo(): ImageInfo {
    return runBIO {
        //Preload
        val imageType = ImageType.match(formatName)
        inputStream().use {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(it, null, options)
            ImageInfo(
                width = options.outWidth,
                height = options.outHeight,
                imageType = imageType
            )
        }
    }

}