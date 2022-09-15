/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.data.ChannelInfo
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.utils.ExternalResource
import kotlin.coroutines.CoroutineContext


internal expect class ChannelImpl constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    id: Long,
    channelInfo: ChannelInfo,
) : Channel, CommonChannelImpl {
    companion object
}


@Suppress("PropertyName")
internal abstract class CommonChannelImpl constructor(
    bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
    override val id: Long,
    channelInfo: ChannelInfo,
) : Channel, AbstractContact(bot, parentCoroutineContext) {


    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio {
        TODO("Not yet implemented")
    }

    override val name: String = channelInfo.name

    override suspend fun sendMessage(message: Message): MessageReceipt<Contact> {
        TODO("Not yet implemented")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        TODO("Not yet implemented")
    }

    override val files: RemoteFiles
        get() = TODO("Not yet implemented")
}