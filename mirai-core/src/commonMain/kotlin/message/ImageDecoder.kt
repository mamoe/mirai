/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.io.core.*
import kotlinx.io.streams.asInput
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.readString
import net.mamoe.mirai.utils.withUse
import java.io.IOException

//SOF0-SOF3 SOF5-SOF7 SOF9-SOF11 SOF13-SOF15 Segment
// (0xC4, 0xC8 and 0xCC not included due to is not an SOF)
private val JPG_SOF_RANGE = listOf(
    0xC0.toByte()..0xC3.toByte(),
    0xC5.toByte()..0xC7.toByte(),
    0xC9.toByte()..0xCB.toByte(),
    0xCD.toByte()..0xCF.toByte()
)

private fun Input.getJPGImageInfo(): ImageInfo {
    require(readBytes(2).contentEquals(byteArrayOf(0xFF.toByte(), 0xD8.toByte()))) {
        "It's not a valid jpg file"
    }
    //0xFF Segment Start
    while (readByte() == 0xFF.toByte()) {
        val type = readByte()
        //Find SOF
        if (JPG_SOF_RANGE.any { it.contains(type) }) {
            //Length
            discardExact(2)
            //Data precision
            discardExact(1)
            val height = readShort().toInt()
            val width = readShort().toInt()
            return ImageInfo(width = width, height = height, imageType = ImageType.JPG)
        } else {
            //SOS Segment, header is ended
            if (type == 0xDA.toByte()) {
                break
            }
            //Other segment, skip
            discardExact(
                //Skip size=segment length - 2 (length data itself)
                readShort().toInt() - 2
            )
        }
    }
    throw IllegalArgumentException("It's not a valid jpg file, failed to find an SOF segment")
}

private fun Input.getBMPImageInfo(): ImageInfo {
    require(readString(2) == "BM") {
        "It's not a valid bmp file"
    }
    //==========
    //FILE HEADER
    //==========
    //Size
    discardExact(4)
    //Reserve 2*2bytes
    discardExact(4)
    //Offset for image data
    discardExact(4)
    //==========
    //INFO HEADER
    //==========
    //Size
    discardExact(4)
    return ImageInfo(
        width = readIntLittleEndian(),
        height = readIntLittleEndian(),
        imageType = ImageType.BMP
    )
}

private fun Input.getPNGImageInfo(): ImageInfo {
    require(
        readBytes(8).contentEquals(
            byteArrayOf(
                0x89.toByte(),
                0x50,
                0x4e,
                0x47,
                0x0d,
                0x0a,
                0x1a,
                0x0a
            )
        )
    ) {
        "It's not a valid png file"
    }
    //Chunk length
    discardExact(4)
    //Chunk type
    var type = readString(4)
    //First chunk must be IHDR
    require(type == "IHDR") {
        "It's not a valid png file, First chunk must be IHDR"
    }
    val width = readInt()
    val height = readInt()
    //Skip to next chunk
    //Bit depth (1 byte) + color type (1 byte)
    // + compression method (1 byte) + filter method (1 byte)
    // + interlace method (1 byte) + CRC(4 bytes) = 9 bytes
    discardExact(9)

    //Chunk length
    discardExact(4)
    //Chunk type
    type = readString(4)

    return ImageInfo(
        width = width,
        height = height,
        //Correct the image type
        //If is apng, it has to be an acTL chunk
        imageType = if (type == "acTL") {
            ImageType.APNG
        } else {
            ImageType.PNG
        }
    )
}

private fun Input.getGIFImageInfo(): ImageInfo {

    require(readString(6).run { startsWith("GIF") && endsWith("a") }) {
        "It's not a valid gif file"
    }
    return ImageInfo(
        width = readShortLittleEndian().toInt(),
        height = readShortLittleEndian().toInt(),
        imageType = ImageType.GIF
    )
}

@Throws(IOException::class, IllegalArgumentException::class)
internal fun ExternalResource.calculateImageInfo(): ImageInfo {
    //Preload
    val imageType = ImageType.match(formatName)
    return inputStream().asInput().withUse {
        when (imageType) {
            ImageType.JPG -> getJPGImageInfo()
            ImageType.BMP -> getBMPImageInfo()
            ImageType.GIF -> getGIFImageInfo()
            ImageType.PNG, ImageType.APNG -> getPNGImageInfo()
            else -> {
                throw IllegalArgumentException("Unsupported image type for ExternalResource $this, considering use gif/png/bmp/jpg format.")
            }
        }
    }
}