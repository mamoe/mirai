/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.ExternalResource
import javax.imageio.ImageIO

internal val isSupportJavax = runCatching { Class.forName("javax.imageio.ImageIO") }.isSuccess
internal actual fun ExternalResource.getImageInfo(): ImageInfo {
    //Preload
    val imageType = ImageType.match(formatName)
    if (!isSupportJavax) {
        Image.logger.error("Failed to find java.desktop module use for reading images, return zero width and height instead.")
        return ImageInfo(imageType = imageType)
    }
    //Save previous value
    val previousValue = ImageIO.getUseCache()
    //We don't need to use cache since we won't load the whole file
    ImageIO.setUseCache(false)
    inputStream().use {
        val imageInputStream = ImageIO.createImageInputStream(it)
        return imageInputStream.use { stream ->
            val readers = ImageIO.getImageReaders(stream)
            if (readers.hasNext()) {
                val imageReader = readers.next()
                try {
                    imageReader.input = stream
                    ImageInfo(
                        height = imageReader.getHeight(0),
                        width = imageReader.getWidth(0),
                        imageType = imageType
                    )
                } finally {
                    imageReader.dispose()
                }
            } else {
                Image.logger.warning("Failed to find image readers for ExternalResource $this, return zero width and height instead.")
                ImageInfo(imageType = imageType)
            }
        }.also {
            //Set it back in case other code using this
            ImageIO.setUseCache(previousValue)
        }
    }
}