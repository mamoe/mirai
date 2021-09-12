/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:JvmName("ImagesImplKtJvm")

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.runBIO
import javax.imageio.ImageIO

internal actual suspend fun ExternalResource.getImageInfo(): ImageInfo {
    return runBIO {
        //Preload
        val imageType = ImageType.match(formatName)
        inputStream().use {
            ImageIO.createImageInputStream(it).use { stream ->
                ImageIO.getImageReaders(stream).forEach { imageReader ->
                    try {
                        imageReader.input = stream
                        return@runBIO ImageInfo(
                            height = imageReader.getHeight(0),
                            width = imageReader.getWidth(0),
                            imageType = imageType
                        )
                    } finally {
                        imageReader.dispose()
                    }

            }
        }
    }
    throw IllegalStateException("Failed to find image readers for ExternalResource $this")
}