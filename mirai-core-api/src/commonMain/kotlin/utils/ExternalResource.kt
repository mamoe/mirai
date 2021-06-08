/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.utils.ExternalResourceImplByByteArray
import net.mamoe.mirai.internal.utils.ExternalResourceImplByFile
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Voice
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.RemoteFile.Companion.sendFile
import net.mamoe.mirai.utils.RemoteFile.Companion.uploadFile
import java.io.*


/**
 * 一个*不可变的*外部资源. 仅包含资源内容, 大小, 文件类型, 校验值而不包含文件名, 文件位置等. 外部资源有可能是一个文件, 也有可能只存在于内存, 或者以任意其他方式实现.
 *
 * [ExternalResource] 在创建之后就应该保持其属性的不变, 即任何时候获取其属性都应该得到相同结果, 任何时候打开流都得到的一样的数据.
 *
 * ## 创建
 * - [File.toExternalResource]
 * - [RandomAccessFile.toExternalResource]
 * - [ByteArray.toExternalResource]
 * - [InputStream.toExternalResource]
 *
 * ## 释放
 *
 * 当 [ExternalResource] 创建时就可能会打开一个文件 (如使用 [File.toExternalResource]).
 * 类似于 [InputStream], [ExternalResource] 需要被 [关闭][close].
 *
 * ## 实现 [ExternalResource]
 *
 * 可以自行实现 [ExternalResource]. 但通常上述创建方法已足够使用.
 *
 * 实现时需保持 [ExternalResource] 在构造后就不可变, 并且所有属性都总是返回一个固定值.
 *
 * @see ExternalResource.uploadAsImage 将资源作为图片上传, 得到 [Image]
 * @see ExternalResource.sendAsImageTo 将资源作为图片发送
 * @see Contact.uploadImage 上传一个资源作为图片, 得到 [Image]
 * @see Contact.sendImage 发送一个资源作为图片
 *
 * @see FileCacheStrategy
 */
public interface ExternalResource : Closeable {

    /**
     * 文件内容 MD5. 16 bytes
     */
    public val md5: ByteArray

    /**
     * 文件内容 SHA1. 16 bytes
     * @since 2.5
     */
    public val sha1: ByteArray
        get() =
            throw UnsupportedOperationException("ExternalResource.sha1 is not implemented by ${this::class.simpleName}")
    // 如果你要实现 [ExternalResource], 你也应该实现 [sha1].
    // 这里默认抛出 [UnsupportedOperationException] 是为了 (姑且) 兼容 2.5 以前的版本的实现.


    /**
     * 文件格式，如 "png", "amr". 当无法自动识别格式时为 [DEFAULT_FORMAT_NAME].
     *
     * 默认会从文件头识别, 支持的文件类型:
     * png, jpg, gif, tif, bmp, amr, silk
     *
     * @see net.mamoe.mirai.utils.getFileType
     * @see net.mamoe.mirai.utils.FILE_TYPES
     * @see DEFAULT_FORMAT_NAME
     */
    public val formatName: String

    /**
     * 文件大小 bytes
     */
    public val size: Long

    /**
     * 当 [close] 时会 [CompletableDeferred.complete] 的 [Deferred].
     */
    public val closed: Deferred<Unit>

    /**
     * 打开 [InputStream]. 在返回的 [InputStream] 被 [关闭][InputStream.close] 前无法再次打开流.
     *
     * 关闭此流不会关闭 [ExternalResource].
     * @throws IllegalStateException 当上一个流未关闭又尝试打开新的流时抛出
     */
    public fun inputStream(): InputStream

    @MiraiInternalApi
    public fun calculateResourceId(): String {
        return generateImageId(md5, formatName.ifEmpty { DEFAULT_FORMAT_NAME })
    }

    public companion object {
        /**
         * 在无法识别文件格式时使用的默认格式名. "mirai".
         *
         * @see ExternalResource.formatName
         */
        public const val DEFAULT_FORMAT_NAME: String = "mirai"

        /**
         * **打开文件**并创建 [ExternalResource].
         *
         * 将以只读模式打开这个文件 (因此文件会处于被占用状态), 直到 [ExternalResource.close].
         *
         * @param formatName 查看 [ExternalResource.formatName]
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("create")
        public fun File.toExternalResource(formatName: String? = null): ExternalResource =
            // although RandomAccessFile constructor throws IOException, actual performance influence is minor so not propagating IOException
            RandomAccessFile(this, "r").toExternalResource(formatName)

        /**
         * 创建 [ExternalResource].
         *
         * @see closeOriginalFileOnClose 若为 `true`, 在 [ExternalResource.close] 时将会同步关闭 [RandomAccessFile]. 否则不会.
         *
         * @param formatName 查看 [ExternalResource.formatName]
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("create")
        public fun RandomAccessFile.toExternalResource(
            formatName: String? = null,
            closeOriginalFileOnClose: Boolean = true
        ): ExternalResource =
            ExternalResourceImplByFile(this, formatName, closeOriginalFileOnClose)

        /**
         * 创建 [ExternalResource].
         *
         * @param formatName 查看 [ExternalResource.formatName]
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("create")
        public fun ByteArray.toExternalResource(formatName: String? = null): ExternalResource =
            ExternalResourceImplByByteArray(this, formatName)


        /**
         * 立即使用 [FileCacheStrategy] 缓存 [InputStream] 并创建 [ExternalResource].
         *
         * **注意**：本函数不会关闭流.
         *
         * @param formatName 查看 [ExternalResource.formatName]
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("create")
        @Throws(IOException::class) // not in BIO context so propagate IOException
        public fun InputStream.toExternalResource(formatName: String? = null): ExternalResource =
            Mirai.FileCacheStrategy.newCache(this, formatName)


        /**
         * 将图片作为单独的消息发送给指定联系人.
         *
         * **注意**：本函数不会关闭 [ExternalResource].
         *
         * @see Contact.uploadImage 上传图片
         * @see Contact.sendMessage 最终调用, 发送消息.
         *
         * @throws OverFileSizeMaxException
         */
        @JvmBlockingBridge
        @JvmStatic
        @JvmName("sendAsImage")
        public suspend fun <C : Contact> ExternalResource.sendAsImageTo(contact: C): MessageReceipt<C> =
            contact.uploadImage(this).sendTo(contact)

        /**
         * 读取 [InputStream] 到临时文件并将其作为图片发送到指定联系人.
         *
         * 注意：本函数不会关闭流.
         *
         * @param formatName 查看 [ExternalResource.formatName]
         * @throws OverFileSizeMaxException
         */
        @JvmStatic
        @JvmBlockingBridge
        @JvmName("sendAsImage")
        @JvmOverloads
        public suspend fun <C : Contact> InputStream.sendAsImageTo(
            contact: C,
            formatName: String? = null
        ): MessageReceipt<C> =
            runBIO {
                // toExternalResource throws IOException however we're in BIO context so not propagating IOException to sendAsImageTo
                toExternalResource(formatName)
            }.withUse { sendAsImageTo(contact) }

        /**
         * 将文件作为图片发送到指定联系人.
         * @param formatName 查看 [ExternalResource.formatName]
         * @throws OverFileSizeMaxException
         */
        @JvmStatic
        @JvmBlockingBridge
        @JvmName("sendAsImage")
        @JvmOverloads
        public suspend fun <C : Contact> File.sendAsImageTo(contact: C, formatName: String? = null): MessageReceipt<C> {
            require(this.exists() && this.canRead())
            return toExternalResource(formatName).withUse { sendAsImageTo(contact) }
        }

        /**
         * 上传图片并构造 [Image]. 这个函数可能需消耗一段时间.
         *
         * **注意**：本函数不会关闭 [ExternalResource].
         *
         * @param contact 图片上传对象. 由于好友图片与群图片不通用, 上传时必须提供目标联系人.
         *
         * @see Contact.uploadImage 最终调用, 上传图片.
         */
        @JvmStatic
        @JvmBlockingBridge
        public suspend fun ExternalResource.uploadAsImage(contact: Contact): Image = contact.uploadImage(this)

        /**
         * 读取 [InputStream] 到临时文件并将其作为图片上传后构造 [Image].
         *
         * 注意：本函数不会关闭流.
         *
         * @param formatName 查看 [ExternalResource.formatName]
         * @throws OverFileSizeMaxException
         */
        @JvmStatic
        @JvmBlockingBridge
        @JvmOverloads
        public suspend fun InputStream.uploadAsImage(contact: Contact, formatName: String? = null): Image =
            // toExternalResource throws IOException however we're in BIO context so not propagating IOException to sendAsImageTo
            runBIO { toExternalResource(formatName) }.withUse { uploadAsImage(contact) }

        /**
         * 将文件作为图片上传后构造 [Image].
         *
         * @param formatName 查看 [ExternalResource.formatName]
         * @throws OverFileSizeMaxException
         */
        @JvmStatic
        @JvmBlockingBridge
        @JvmOverloads
        public suspend fun File.uploadAsImage(contact: Contact, formatName: String? = null): Image =
            toExternalResource(formatName).withUse { uploadAsImage(contact) }

        /**
         * 上传文件并获取文件消息.
         *
         * 如果要上传的文件格式是图片或者语音, 也会将它们作为文件上传而不会调整消息类型.
         *
         * 需要调用方手动[关闭资源][ExternalResource.close].
         *
         * ## 已弃用
         * 查看 [RemoteFile.upload] 获取更多信息.
         *
         * @param path 远程路径. 起始字符为 '/'. 如 '/foo/bar.txt'
         * @since 2.5
         * @see RemoteFile.path
         * @see RemoteFile.upload
         */
        @Suppress("DEPRECATION")
        @JvmStatic
        @JvmBlockingBridge
        @JvmOverloads
        @Deprecated(
            "Use sendTo instead.",
            ReplaceWith(
                "this.sendTo(contact, path, callback)",
                "net.mamoe.mirai.utils.ExternalResource.Companion.sendTo"
            ),
            level = DeprecationLevel.WARNING
        ) // deprecated since 2.7-M1
        public suspend fun File.uploadTo(
            contact: FileSupported,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null
        ): FileMessage = toExternalResource().use { contact.uploadFile(path, it, callback) }

        /**
         * 上传文件并获取文件消息.
         *
         * 如果要上传的文件格式是图片或者语音, 也会将它们作为文件上传而不会调整消息类型.
         *
         * 需要调用方手动[关闭资源][ExternalResource.close].
         *
         * ## 已弃用
         * 查看 [RemoteFile.upload] 获取更多信息.
         *
         * @param path 远程路径. 起始字符为 '/'. 如 '/foo/bar.txt'
         * @since 2.5
         * @see RemoteFile.path
         * @see RemoteFile.upload
         */
        @Suppress("DEPRECATION")
        @JvmStatic
        @JvmBlockingBridge
        @JvmName("uploadAsFile")
        @JvmOverloads
        @Deprecated(
            "Use sendAsFileTo instead.",
            ReplaceWith(
                "this.sendAsFileTo(contact, path, callback)",
                "net.mamoe.mirai.utils.ExternalResource.Companion.sendAsFileTo"
            ),
            level = DeprecationLevel.WARNING
        ) // deprecated since 2.7-M1
        public suspend fun ExternalResource.uploadAsFile(
            contact: FileSupported,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null
        ): FileMessage = contact.uploadFile(path, this, callback)

        /**
         * 上传文件并发送文件消息.
         *
         * 如果要上传的文件格式是图片或者语音, 也会将它们作为文件上传而不会调整消息类型.
         *
         * @param path 远程路径. 起始字符为 '/'. 如 '/foo/bar.txt'
         * @since 2.5
         * @see RemoteFile.path
         * @see RemoteFile.uploadAndSend
         */
        @JvmStatic
        @JvmBlockingBridge
        @JvmOverloads
        public suspend fun <C : FileSupported> File.sendTo(
            contact: C,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null
        ): MessageReceipt<C> = toExternalResource().use { contact.sendFile(path, it, callback) }

        /**
         * 上传文件并发送件消息.  如果要上传的文件格式是图片或者语音, 也会将它们作为文件上传而不会调整消息类型.
         *
         * 需要调用方手动[关闭资源][ExternalResource.close].
         *
         * @param path 远程路径. 起始字符为 '/'. 如 '/foo/bar.txt'
         * @since 2.5
         * @see RemoteFile.path
         * @see RemoteFile.uploadAndSend
         */
        @JvmStatic
        @JvmBlockingBridge
        @JvmName("sendAsFile")
        @JvmOverloads
        public suspend fun <C : FileSupported> ExternalResource.sendAsFileTo(
            contact: C,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null
        ): MessageReceipt<C> = contact.sendFile(path, this, callback)

        /**
         * 将文件作为语音上传后构造 [Voice]. 上传后只会得到 [Voice] 实例, 而不会将语音发送到目标群或好友.
         *
         * **服务器仅支持音频格式 `silk` 或 `amr`**. 需要调用方手动[关闭资源][ExternalResource.close].
         *
         * 目前仅支持发送给群.
         *
         * @throws OverFileSizeMaxException
         */
        @JvmBlockingBridge
        @JvmStatic
        public suspend fun ExternalResource.uploadAsVoice(contact: Contact): Voice {
            if (contact is Group) return contact.uploadVoice(this)
            else throw UnsupportedOperationException("Uploading Voice is only supported for Group yet.")
        }
    }
}