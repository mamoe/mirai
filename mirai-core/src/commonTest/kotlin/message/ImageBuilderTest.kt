/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package message

import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ImageBuilderTest : AbstractTest() {
    companion object {
        private const val IMAGE_ID = "{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.jpg"
    }

    @Test
    fun create() {
        // five overloads

        Image(IMAGE_ID) {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, type)
            assertEquals(false, isEmoji)
        }

        Image.newBuilder(IMAGE_ID).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, type)
            assertEquals(false, isEmoji)
        }

        Image.Builder.newBuilder(IMAGE_ID).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, type)
            assertEquals(false, isEmoji)
        }

        Image.Builder.newBuilder(IMAGE_ID).build().run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, imageType)
            assertEquals(false, isEmoji)
        }
    }

    @Test
    fun legacyMethods() {
        // just make sure they work

        Mirai.createImage(IMAGE_ID).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, imageType)
            assertEquals(false, isEmoji)
        }
        Image.fromId(IMAGE_ID).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, imageType)
            assertEquals(false, isEmoji)
        }
        Image(IMAGE_ID).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, imageType)
            assertEquals(false, isEmoji)
        }
        Image(IMAGE_ID) {
            width = 1
            height = 2
            size = 3
            type = ImageType.GIF
            isEmoji = true
        }.run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(1, width)
            assertEquals(2, height)
            assertEquals(3, size)
            assertEquals(ImageType.GIF, imageType)
            assertEquals(true, isEmoji)
        }
    }
}