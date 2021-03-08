/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.message.data

import kotlinx.serialization.SerialName
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.RemoteFile
import net.mamoe.mirai.utils.safeCast

/**
 * 文件消息.
 *
 * @since 2.5
 * @suppress 文件消息不稳定, 可能在未来版本有不兼容变更.
 */
@SerialName(FileMessage.SERIAL_NAME)
@MiraiExperimentalApi
public interface FileMessage : MessageContent, ConstrainSingle {
    public val name: String
    public val id: String
    public val size: Long

    override fun contentToString(): String = "[文件]$name" // orthodox

    /**
     * 获取一个对应的 [RemoteFile]. 当目标群或好友不存在这个文件时返回 `null`.
     */
    @MiraiExperimentalApi
    @JvmBlockingBridge
    public suspend fun toRemoteFile(contact: FileSupported): RemoteFile? {
        return contact.filesRoot.resolveById(id)
    }

    override val key: Key get() = Key

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, ForwardMessage>(MessageContent, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "FileMessage"
    }
}