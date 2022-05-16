/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.image

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.systemProp


internal data class ImageInfo(val width: Int = 0, val height: Int = 0, val imageType: ImageType = ImageType.UNKNOWN)


/*
 * ImgType:
 *  JPG:    1000
 *  PNG:    1001
 *  WEBP:   1002
 *  BMP:    1005
 *  GIG:    2000 // gig? gif?
 *  APNG:   2001
 *  SHARPP: 1004
 */

internal val UNKNOWN_IMAGE_TYPE_PROMPT_ENABLED = systemProp("mirai.unknown.image.type.logging", false)

internal fun getImageTypeById(id: Int): ImageType? {
    return if (id == 2001) {
        ImageType.APNG
    } else {
        ImageType.matchOrNull(getImageType(id))
    }
}

internal fun getIdByImageType(imageType: ImageType): Int {
    return when (imageType) {
        ImageType.JPG -> 1000
        ImageType.PNG -> 1001
        //ImageType.WEBP -> 1002 //Unsupported by pc client
        ImageType.BMP -> 1005
        ImageType.GIF -> 2000
        ImageType.APNG -> 2001
        //default to jpg
        else -> 1000
    }
}

internal fun getImageType(id: Int): String {
    return when (id) {
        1000 -> "jpg"
        1001 -> "png"
        //1002 -> "webp" //Unsupported by pc client
        1005 -> "bmp"
        2000, 3, 4 -> "gif"
        //apng
        2001 -> "png"
        else -> {
            if (UNKNOWN_IMAGE_TYPE_PROMPT_ENABLED) {
                Image.logger.debug(
                    "Unknown image id: $id. Stacktrace:",
                    Exception()
                )
            }
            ExternalResource.DEFAULT_FORMAT_NAME
        }
    }
}
