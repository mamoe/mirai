/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendStringAsMiraiCode
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*

/**
 * 文件消息.
 *
 * [name] 与 [size] 只供本地使用, 发送消息时只会使用 [id] 和 [internalId].
 *
 * 注: [FileMessage] 不可二次发送
 *
 * ### 文件操作
 * 要下载这个文件, 可通过 [toAbsoluteFile] 获取到 [AbsoluteFile] 然后操作.
 *
 * 要获取到 [FileMessage], 可以通过 [MessageEvent.message] 获取, 或通过 [AbsoluteFile.toMessage] 得到.
 *
 * @since 2.5
 * @suppress [FileMessage] 的使用是稳定的, 但自行实现不稳定.
 */
@Serializable(FileMessage.Serializer::class)
@SerialName(FileMessage.SERIAL_NAME)
@NotStableForInheritance
@JvmBlockingBridge
public interface FileMessage : MessageContent, ConstrainSingle, CodableMessage {
    /**
     * 服务器需要的某种 ID.
     */
    public val id: String

    /**
     * 服务器需要的某种 ID.
     */
    public val internalId: Int

    /**
     * 文件名
     */
    public val name: String

    /**
     * 文件大小 bytes
     */
    public val size: Long

    override fun contentToString(): String = "[文件]$name" // orthodox

    @MiraiExperimentalApi
    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:file:")
        builder.appendStringAsMiraiCode(id).append(",")
        builder.append(internalId).append(",")
        builder.appendStringAsMiraiCode(name).append(",")
        builder.append(size).append("]")
    }

    /**
     * 获取一个对应的 [RemoteFile]. 当目标群或好友不存在这个文件时返回 `null`.
     */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(
        "Please use toAbsoluteFile",
        ReplaceWith("this.toAbsoluteFile(contact)"),
        level = DeprecationLevel.ERROR
    ) // deprecated since 2.8.0-RC
    @DeprecatedSinceMirai(warningSince = "2.8", errorSince = "2.14")
    public suspend fun toRemoteFile(contact: FileSupported): RemoteFile? {
        return contact.filesRoot.resolveById(id)
    }

    /**
     * 获取一个对应的 [AbsoluteFile]. 当目标群或好友不存在这个文件时返回 `null`.
     *
     * @since 2.8
     */
    public suspend fun toAbsoluteFile(contact: FileSupported): AbsoluteFile?

    override val key: Key get() = Key

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitFileMessage(this, data)
    }

    /**
     * 注意, baseKey [MessageContent] 不稳定. 未来可能会有变更.
     */
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, FileMessage>(
            MessageContent, { it.safeCast() }) {

        public const val SERIAL_NAME: String = "FileMessage"

        /**
         * 构造 [FileMessage]
         * @since 2.5
         */
        @JvmStatic
        public fun create(id: String, internalId: Int, name: String, size: Long): FileMessage =
            Mirai.createFileMessage(id, internalId, name, size)
    }


    public object Serializer :
        KSerializer<FileMessage> by @OptIn(MiraiInternalApi::class) FallbackFileMessageSerializer()
}

@MiraiInternalApi
internal open class FallbackFileMessageSerializer :
    KSerializer<FileMessage> by Delegate.serializer().map(
        Delegate.serializer().descriptor,
        serialize = { Delegate(id, internalId, name, size) },
        deserialize = { Mirai.createFileMessage(id, internalId, name, size) },
    ) {
    @Suppress("ANNOTATION_ARGUMENT_MUST_BE_CONST")
    @SerialName(FileMessage.SERIAL_NAME)
    @Serializable
    data class Delegate(
        val id: String,
        val internalId: Int,
        val name: String,
        val size: Long,
    )
}

/**
 * 构造 [FileMessage]
 * @since 2.5
 */
@JvmSynthetic
public fun FileMessage(id: String, internalId: Int, name: String, size: Long): FileMessage =
    FileMessage.create(id, internalId, name, size)