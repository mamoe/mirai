/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.Dispatchers
import kotlinx.io.core.Input
import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.future
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.uploadImage
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.OverFileSizeMaxException
import net.mamoe.mirai.utils.UnstableExternalImage
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.concurrent.Future

@JavaFriendlyAPI
@Suppress("INAPPLICABLE_JVM_NAME", "FunctionName", "unused")
internal actual interface ContactJavaFriendlyAPI {
    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     *
     * @return 消息回执. 可 [引用回复][MessageReceipt.quote]（仅群聊）或 [撤回][MessageReceipt.recall] 这条消息.
     */
    @Throws(EventCancelledException::class, IllegalStateException::class)
    @JvmName("sendMessage")
    open fun __sendMessageBlockingForJava__(message: Message): MessageReceipt<Contact> {
        return runBlocking { sendMessage(message) }
    }

    @Throws(EventCancelledException::class, IllegalStateException::class)
    @JvmName("sendMessage")
    open fun __sendMessageBlockingForJava__(message: String): MessageReceipt<Contact> {
        return runBlocking { sendMessage(message) }
    }

    /**
     * 上传一个图片以备发送.
     *
     * @see BeforeImageUploadEvent 图片发送前事件, cancellable
     * @see ImageUploadEvent 图片发送完成事件
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时. (最大大小约为 20 MB)
     */
    @UnstableExternalImage
    @Throws(OverFileSizeMaxException::class)
    @JvmName("uploadImage")
    open fun __uploadImageBlockingForJava__(image: ExternalImage): Image {
        return runBlocking { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片上传, 但不发送
     * @throws OverFileSizeMaxException
     */
    @Throws(OverFileSizeMaxException::class)
    @Suppress("DEPRECATION")
    @JvmName("uploadImage")
    open fun __uploadImageBlockingForJava__(image: URL): Image {
        return runBlocking { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片上传, 但不发送
     * @throws OverFileSizeMaxException
     */
    @Throws(OverFileSizeMaxException::class)
    @JvmName("uploadImage")
    open fun __uploadImageBlockingForJava__(image: InputStream): Image {
        return runBlocking { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片上传, 但不发送
     * @throws OverFileSizeMaxException
     */
    @Deprecated(
        "已弃用对 kotlinx.io 的支持",
        level = DeprecationLevel.ERROR
    )
    @Suppress("DEPRECATION_ERROR")
    @Throws(OverFileSizeMaxException::class)
    @JvmName("uploadImage")
    open fun __uploadImageBlockingForJava__(image: Input): Image {
        return runBlocking { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中将文件作为图片上传, 但不发送
     * @throws OverFileSizeMaxException
     */
    @Throws(OverFileSizeMaxException::class)
    @JvmName("uploadImage")
    open fun __uploadImageBlockingForJava__(image: File): Image {
        return runBlocking { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中将图片上传, 但不发送. 不会保存临时文件
     * @throws OverFileSizeMaxException
     */
    @Throws(OverFileSizeMaxException::class)
    @JvmName("uploadImage")
    open fun __uploadImageBlockingForJava__(image: BufferedImage): Image {
        return runBlocking { uploadImage(image) }
    }

    /**
     * 发送消息
     * @see Contact.sendMessage
     */
    @JvmName("sendMessageAsync")
    @Deprecated("已停止支持 Java async API", level = DeprecationLevel.WARNING)
    open fun __sendMessageAsyncForJava__(message: Message): Future<MessageReceipt<Contact>> {
        return future { sendMessage(message) }
    }

    /**
     * 发送消息
     * @see Contact.sendMessage
     */
    @JvmName("sendMessageAsync")
    @Deprecated("已停止支持 Java async API", level = DeprecationLevel.WARNING)
    open fun __sendMessageAsyncForJava__(message: String): Future<MessageReceipt<Contact>> {
        return future { sendMessage(message) }
    }

    /**
     * 上传一个图片以备发送.
     *
     * @see BeforeImageUploadEvent 图片发送前事件, cancellable
     * @see ImageUploadEvent 图片发送完成事件
     */
    @UnstableExternalImage
    @JvmName("uploadImageAsync")
    @Deprecated("已停止支持 Java async API", level = DeprecationLevel.WARNING)
    open fun __uploadImageAsyncForJava__(image: ExternalImage): Future<Image> {
        return future { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中下载 [URL] 到临时文件并将其作为图片上传, 但不发送
     */
    @Deprecated("已停止支持 Java async API", level = DeprecationLevel.WARNING)
    @Suppress("DEPRECATION")
    @JvmName("uploadImageAsync")
    open fun __uploadImageAsyncForJava__(image: URL): Future<Image> {
        return future { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中读取 [InputStream] 到临时文件并将其作为图片上传, 但不发送
     */
    @Deprecated("已停止支持 Java async API", level = DeprecationLevel.WARNING)
    @JvmName("uploadImageAsync")
    open fun __uploadImageAsyncForJava__(image: InputStream): Future<Image> {
        return future { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中读取 [Input] 到临时文件并将其作为图片上传, 但不发送
     */
    @Suppress("DEPRECATION_ERROR")
    @Deprecated(
        "已弃用对 kotlinx.io 的支持",
        level = DeprecationLevel.ERROR
    )
    @JvmName("uploadImageAsync")
    open fun __uploadImageAsyncForJava__(image: Input): Future<Image> {
        return future { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中将文件作为图片上传, 但不发送
     */
    @JvmName("uploadImageAsync")
    @Deprecated("已停止支持 Java async API", level = DeprecationLevel.WARNING)
    open fun __uploadImageAsyncForJava__(image: File): Future<Image> {
        return future { uploadImage(image) }
    }

    /**
     * 在 [Dispatchers.IO] 中将图片上传, 但不发送. 不会保存临时文件
     */
    @JvmName("uploadImageAsync")
    @Deprecated("已停止支持 Java async API", level = DeprecationLevel.WARNING)
    open fun __uploadImageAsyncForJava__(image: BufferedImage): Future<Image> {
        return future { uploadImage(image) }
    }
}

@JavaFriendlyAPI
private inline fun <R> ContactJavaFriendlyAPI.runBlocking(crossinline block: suspend Contact.() -> R): R {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return kotlinx.coroutines.runBlocking { block(this@runBlocking as Contact) }
}

@JavaFriendlyAPI
private inline fun <R> ContactJavaFriendlyAPI.future(crossinline block: suspend Contact.() -> R): Future<R> {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return (this as Contact).run { future { block() } }
}

@Suppress("INAPPLICABLE_JVM_NAME", "FunctionName", "unused", "unused", "DEPRECATION_ERROR")
@JavaFriendlyAPI
internal actual interface MemberJavaFriendlyAPI {


    /**
     * 禁言.
     *
     * QQ 中最小操作和显示的时间都是一分钟.
     * 机器人可以实现精确到秒, 会被客户端显示为 1 分钟但不影响实际禁言时间.
     *
     * 管理员可禁言成员, 群主可禁言管理员和群员.
     *
     * @param seconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 机器人无权限时返回 `false`
     *
     * @see Int.minutesToSeconds
     * @see Int.hoursToSeconds
     * @see Int.daysToSeconds
     *
     * @see MemberMuteEvent 成员被禁言事件
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("mute")
    open fun __muteBlockingForJava__(seconds: Int) {
        runBlocking { mute(seconds) }
    }

    /**
     * 解除禁言.
     *
     * 管理员可解除成员的禁言, 群主可解除管理员和群员的禁言.
     *
     * @see MemberUnmuteEvent 成员被取消禁言事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("unmute")
    open fun __unmuteBlockingForJava__() {
        runBlocking { unmute() }
    }

    /**
     * 踢出该成员.
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("kick")
    open fun __kickBlockingForJava__(message: String = "") {
        runBlocking { kick(message) }
    }

    /**
     * 踢出该成员.
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("kick")
    open fun __kickBlockingForJava__() = __kickBlockingForJava__("")


    /**
     * 禁言.
     *
     * QQ 中最小操作和显示的时间都是一分钟.
     * 机器人可以实现精确到秒, 会被客户端显示为 1 分钟但不影响实际禁言时间.
     *
     * 管理员可禁言成员, 群主可禁言管理员和群员.
     *
     * @param seconds 持续时间. 精确到秒. 范围区间表示为 `(0s, 30days]`. 超过范围则会抛出异常.
     * @return 机器人无权限时返回 `false`
     *
     * @see Int.minutesToSeconds
     * @see Int.hoursToSeconds
     * @see Int.daysToSeconds
     *
     * @see MemberMuteEvent 成员被禁言事件
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("muteAsync")
    open fun __muteAsyncForJava__(seconds: Int): Future<Unit> {
        return future { mute(seconds) }
    }

    /**
     * 解除禁言.
     *
     * 管理员可解除成员的禁言, 群主可解除管理员和群员的禁言.
     *
     * @see MemberUnmuteEvent 成员被取消禁言事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("unmuteAsync")
    open fun __unmuteAsyncForJava__(): Future<Unit> {
        return future { unmute() }
    }

    /**
     * 踢出该成员.
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("kickAsync")
    open fun __kickAsyncForJava__(message: String = ""): Future<Unit> {
        return future { kick(message) }
    }

    /**
     * 踢出该成员.
     *
     * 管理员可踢出成员, 群主可踢出管理员和群员.
     *
     * @see MemberLeaveEvent.Kick 成员被踢出事件.
     * @throws PermissionDeniedException 无权限修改时
     */
    @JvmName("kickAsync")
    open fun __kickAsyncForJava__(): Future<Unit> = __kickAsyncForJava__("")
}

@JavaFriendlyAPI
private inline fun <R> MemberJavaFriendlyAPI.future(crossinline block: suspend Member.() -> R): Future<R> {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return (this as Member).run { future { block() } }
}

@JavaFriendlyAPI
private inline fun <R> MemberJavaFriendlyAPI.runBlocking(crossinline block: suspend Member.() -> R): R {
    @Suppress("CAST_NEVER_SUCCEEDS")
    return kotlinx.coroutines.runBlocking { block(this@runBlocking as Member) }
}