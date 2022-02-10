/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.utils

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.internal.contact.MockImage
import net.mamoe.mirai.utils.generateImageId
import net.mamoe.mirai.utils.generateUUID
import net.mamoe.mirai.utils.md5
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import net.mamoe.mirai.utils.randomImageContent as miraiutils_randomImageContent

// Make `randomImageContent` public
public fun randomImageContent(): ByteArray = miraiutils_randomImageContent()

// create a mockImage with random content
internal fun randomMockImage(bot: MockBot): MockImage {
    val text = randomImageContent()
    val uuid = generateUUID(text.md5())
    val f = bot.tmpFsServer.fsSystem.getPath("$uuid.png")
    bot.tmpFsServer.fsSystem.getPath(uuid).also {
        it.createFile()
    }.outputStream().use { fso ->
        fso.write(text)
    }
    runBlocking {
        bot.tmpFsServer.bindFile(uuid, f.toString())
    }
    return MockImage(generateImageId(text.md5()), f.toString())
}
