/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.mock.MockBotFactory
import net.mamoe.mirai.mock.utils.randomImageContent
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class ImageUploadTest {
    internal val bot = MockBotFactory.newMockBotBuilder()
        .id(1234567890)
        .nick("Sakura")
        .create()

    @AfterEach
    internal fun botDestroy() {
        bot.close()
    }

    @Test
    fun testImageUpload() = runBlocking<Unit> {
        val data = Image.randomImageContent()
        val img = bot.asFriend.uploadImage(
            data.toExternalResource().toAutoCloseable()
        )
        println(img.imageId)
        assertTrue {
            data.contentEquals(URL(img.queryUrl()).readBytes())
        }
    }

    @Test
    fun testSameImageMultiUpload() = runBlocking<Unit> {
        Image.randomImageContent().toExternalResource().use { imgData ->
            val img1 = bot.asFriend.uploadImage(imgData)
            val img2 = bot.asFriend.uploadImage(imgData)
            assertEquals(img1, img2)
        }
    }
}
