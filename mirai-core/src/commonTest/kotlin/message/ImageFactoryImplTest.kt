/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package message

import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.message.ImageFactoryImpl
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageType
import net.mamoe.mirai.utils.loadService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class ImageFactoryImplTest {
    companion object {
        private const val IMAGE_ID = "{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.jpg"
    }

    @Test
    fun serviceBinding() {
        assertIs<ImageFactoryImpl>(loadService(Image.Factory::class))
    }

    @Test
    fun create() {
        // five overloads

        Image.Factory.create(IMAGE_ID).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(0, size)
            assertEquals(ImageType.UNKNOWN, imageType)
            assertEquals(false, isEmoji)
        }

        Image.Factory.create(IMAGE_ID, 1).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(1, size)
            assertEquals(ImageType.UNKNOWN, imageType)
            assertEquals(false, isEmoji)
        }

        Image.Factory.create(IMAGE_ID, 1, ImageType.JPG).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(0, width)
            assertEquals(0, height)
            assertEquals(1, size)
            assertEquals(ImageType.JPG, imageType)
            assertEquals(false, isEmoji)
        }

        Image.Factory.create(IMAGE_ID, 1, ImageType.JPG, 2, 3).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(2, width)
            assertEquals(3, height)
            assertEquals(1, size)
            assertEquals(ImageType.JPG, imageType)
            assertEquals(false, isEmoji)
        }

        Image.Factory.create(IMAGE_ID, 1, ImageType.JPG, 2, 3, true).run {
            assertEquals(IMAGE_ID, imageId)
            assertEquals(2, width)
            assertEquals(3, height)
            assertEquals(1, size)
            assertEquals(ImageType.JPG, imageType)
            assertEquals(true, isEmoji)
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
    }
}