/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package net.mamoe.mirai.mock.internal.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.message.DeferredOriginUrlAware
import net.mamoe.mirai.internal.message.OnlineAudioImpl
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockGroup
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.plusHttpSubpath
import net.mamoe.mirai.utils.toUHexString

internal fun Member.requireBotPermissionHigherThanThis(msg: String) {
    if (this.permission < this.group.botPermission) return

    throw PermissionDeniedException("bot current permission ${group.botPermission} can't modify $id($permission), $msg")
}

internal infix fun MessageSource.withMessage(msg: Message): MessageChain = buildMessageChain {
    add(this@withMessage)
    if (msg is MessageChain) {
        msg.forEach { sub ->
            if (sub !is MessageSource) {
                add(sub)
            }
        }
    } else if (msg !is MessageSource) {
        add(msg)
    }
}

internal suspend fun ExternalResource.mockUploadAudio(bot: MockBot) = inResource {
    OfflineAudio(
        filename = md5.toUHexString() + ".amr",
        fileMd5 = md5,
        fileSize = size,
        codec = AudioCodec.SILK,
        extraData = null,
    )
}

internal suspend fun ExternalResource.mockUploadVoice(bot: MockBot) = kotlin.run {
    val md5 = this.md5
    val size = this.size
    @Suppress("DEPRECATION")
    Voice(
        fileName = md5.toUHexString() + ".amr",
        md5 = md5,
        fileSize = size,
        _url = bot.tmpFsServer.uploadFileAndGetUrl(this)
    )
}

internal const val AQQ_RECALL_FAILED_MESSAGE: String = "No message meets the requirements"

internal val Group.mockUin: Long
    get() = when (this) {
        is MockGroup -> this.uin
        else -> this.uin
    }


internal suspend fun ExternalResource.mockImplUploadAudioAsOnline(bot: MockBot): OnlineAudio {
    val md5 = this.md5
    val size = this.size
    return OnlineAudioImpl(
        filename = md5.toUHexString() + ".amr",
        fileMd5 = md5,
        fileSize = size,
        codec = AudioCodec.SILK,
        url = bot.tmpFsServer.uploadFileAndGetUrl(this),
        length = size,
        originalPtt = null,
    )
}

internal class MockImage(
    override val imageId: String,
    private val urlPath: String,
) : GroupImage(), DeferredOriginUrlAware {
    override fun getUrl(bot: Bot): String {
        return bot.mock().tmpFsServer.httpRoot.plusHttpSubpath(urlPath)
    }
}
