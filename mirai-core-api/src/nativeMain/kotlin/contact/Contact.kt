/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.OverFileSizeMaxException
import kotlin.coroutines.cancellation.CancellationException

/**
 * 联系对象, 即可以与 [Bot] 互动的对象. 包含 [用户][User], 和 [群][Group].
 */
@NotStableForInheritance
public actual interface Contact : ContactOrBot, CoroutineScope {
    /**
     * 这个联系对象所属 [Bot].
     */
    public actual override val bot: Bot

    /**
     * 可以是 QQ 号码或者群号码.
     *
     * @see User.id
     * @see Group.id
     */
    public actual override val id: Long

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see MessagePreSendEvent 发送消息前事件
     * @see MessagePostSendEvent 发送消息后事件
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可 [引用][MessageReceipt.quote] 或 [撤回][MessageReceipt.recall] 这条消息.
     */
    public actual suspend fun sendMessage(message: Message): MessageReceipt<Contact>

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    public actual suspend fun sendMessage(message: String): MessageReceipt<Contact> = sendMessage(message.toPlainText())

    /**
     * 上传一个 [资源][ExternalResource] 作为图片以备发送.
     *
     * **无论上传是否成功都不会关闭 [resource]. 需要调用方手动关闭资源**
     *
     * 也可以使用其他扩展: [ExternalResource.uploadAsImage] 使用 [Input] 等上传.
     *
     * @see Image 查看有关图片的更多信息, 如上传图片
     *
     * @see BeforeImageUploadEvent 图片发送前事件, 可拦截.
     * @see ImageUploadEvent 图片发送完成事件, 不可拦截.
     *
     * @see ExternalResource
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时抛出. (最大大小约为 20 MB, 但 mirai 限制的大小为 30 MB)
     */
    public actual suspend fun uploadImage(resource: ExternalResource): Image

    public actual companion object {
        /**
         * 将资源作为单独的图片消息发送给 [this]
         *
         * @see Contact.sendMessage 最终调用, 发送消息.
         */
        public actual suspend fun <C : Contact> C.sendImage(resource: ExternalResource): MessageReceipt<C> {
            return this.uploadImage(resource).sendTo(this)
        }

        /**
         * 将文件作为图片上传, 但不发送
         * @throws OverFileSizeMaxException
         */
        @kotlin.internal.LowPriorityInOverloadResolution
        @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "EXTENSION_SHADOWED_BY_MEMBER")
        @Throws(OverFileSizeMaxException::class, CancellationException::class)
        public actual suspend fun Contact.uploadImage(resource: ExternalResource): Image {
            return uploadImage(resource)
        }

    }

}