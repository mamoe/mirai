/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.utils.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.message.data.toVoice
import net.mamoe.mirai.utils.AbstractExternalResource.ResourceCleanCallback
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import java.io.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * 一个*不可变的*外部资源. 仅包含资源内容, 大小, 文件类型, 校验值而不包含文件名, 文件位置等. 外部资源有可能是一个文件, 也有可能只存在于内存, 或者以任意其他方式实现.
 *
 * [ExternalResource] 在创建之后就应该保持其属性的不变, 即任何时候获取其属性都应该得到相同结果, 任何时候打开流都得到的一样的数据.
 *
 * # 创建
 * - [File.toExternalResource]
 * - [RandomAccessFile.toExternalResource]
 * - [ByteArray.toExternalResource]
 * - [InputStream.toExternalResource]
 *
 * ## 在 Kotlin 获得和使用 [ExternalResource] 实例
 *
 * ```
 * file.toExternalResource().use { resource -> // 安全地使用资源
 *     contact.uploadImage(resource) // 用来上传图片
 *     contact.files.uploadNewFile("/foo/test.txt", file) // 或者用来上传文件
 * }
 * ```
 *
 * 注意, 若使用 [InputStream], 必须手动关闭 [InputStream]. 一种使用情况示例:
 *
 * ```
 * inputStream.use { input -> // 安全地使用 InputStream
 *     input.toExternalResource().use { resource -> // 安全地使用资源
 *         contact.uploadImage(resource) // 用来上传图片
 *         contact.files.uploadNewFile("/foo/test.txt", file) // 或者用来上传文件
 *     }
 * }
 * ```
 *
 * ## 在 Java 获得和使用 [ExternalResource] 实例
 *
 * ```
 * try (ExternalResource resource = ExternalResource.create(file)) { // 使用文件 file
 *     contact.uploadImage(resource); // 用来上传图片
 *     contact.files.uploadNewFile("/foo/test.txt", file); // 或者用来上传文件
 * }
 * ```
 *
 * 注意, 若使用 [InputStream], 必须手动关闭 [InputStream]. 一种使用情况示例:
 *
 * ```java
 * try (InputStream stream = ...) { // 安全地使用 InputStream
 *     try (ExternalResource resource = ExternalResource.create(stream)) { // 安全地使用资源
 *         contact.uploadImage(resource); // 用来上传图片
 *         contact.files.uploadNewFile("/foo/test.txt", file); // 或者用来上传文件
 *     }
 * }
 * ```
 *
 * # 释放
 *
 * 当 [ExternalResource] 创建时就可能会打开一个文件 (如使用 [File.toExternalResource]).
 * 类似于 [InputStream], [ExternalResource] 需要被 [关闭][close].
 *
 * ## 未释放资源的补救策略
 *
 * 自 2.7 起, 每个 mirai 内置的 [ExternalResource] 实现都有引用跟踪, 当 [ExternalResource] 被 GC 后会执行被动释放.
 * 这依赖于 JVM 垃圾收集策略, 因此不可靠, 资源仍然需要手动 close.
 *
 * ## 使用单次自动释放
 *
 * 若创建的资源仅需要*很快地*使用一次, 可使用 [toAutoCloseable] 获得在使用一次后就会自动关闭的资源.
 *
 * 示例:
 * ```java
 * contact.uploadImage(ExternalResource.create(file).toAutoCloseable()); // 创建并立即使用单次自动释放的资源
 * ```
 *
 * **注意**: 如果仅使用 [toAutoCloseable] 而不通过 [Contact.uploadImage] 等 mirai 内置方法使用资源, 资源仍然会处于打开状态且不会被自动关闭.
 * 最终资源会由上述*未释放资源的补救策略*关闭, 但这依赖于 JVM 垃圾收集策略而不可靠.
 * 因此建议在创建单次自动释放的资源后就尽快使用它, 否则仍然需要考虑在正确的时间及时关闭资源.
 *
 * # 实现 [ExternalResource]
 *
 * 可以自行实现 [ExternalResource]. 但通常上述创建方法已足够使用.
 *
 * 建议继承 [AbstractExternalResource], 这将支持上文提到的资源自动释放功能.
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
     * 是否在 _使用一次_ 后自动 [close].
     *
     * 该属性仅供调用方参考. 如 [Contact.uploadImage] 会在方法结束时关闭 [isAutoClose] 为 `true` 的 [ExternalResource], 无论上传图片是否成功.
     *
     * 所有 mirai 内置的上传图片, 上传语音等方法都支持该行为.
     *
     * @since 2.8
     */
    @MiraiExperimentalApi
    public val isAutoClose: Boolean
        get() = false

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

    /**
     * 该 [ExternalResource] 的数据来源, 可能有以下的返回
     *
     * - [File] 本地文件
     * - [java.nio.file.Path] 某个具体文件路径
     * - [java.nio.ByteBuffer] RAM
     * - [java.net.URI] uri
     * - [ByteArray] RAM
     * - Or more...
     *
     * implementation note:
     *
     * - 对于无法二次读取的数据来源 (如 [InputStream]), 返回 `null`
     * - 对于一个来自网络的资源, 请返回 [java.net.URI] (not URL, 或者其他库的 URI/URL 类型)
     * - 不要返回 [String], 没有约定 [String] 代表什么
     * - 数据源外漏会严重影响 [inputStream] 等的执行的可以返回 `null` (如 [RandomAccessFile])
     *
     * @since 2.8.0
     */
    public val origin: Any? get() = null

    /**
     * 创建一个在 _使用一次_ 后就会自动 [close] 的 [ExternalResource].
     *
     * @since 2.8.0
     */
    public fun toAutoCloseable(): ExternalResource {
        return if (isAutoClose) this else {
            val delegate = this
            object : ExternalResource by delegate {
                override val isAutoClose: Boolean get() = true
                override fun toString(): String = "ExternalResourceWithAutoClose(delegate=$delegate)"
                override fun toAutoCloseable(): ExternalResource {
                    return this
                }
            }
        }
    }


    public companion object {
        /**
         * 在无法识别文件格式时使用的默认格式名. "mirai".
         *
         * @see ExternalResource.formatName
         */
        public const val DEFAULT_FORMAT_NAME: String = "mirai"

        ///////////////////////////////////////////////////////////////////////////
        // region toExternalResource
        ///////////////////////////////////////////////////////////////////////////

        /**
         * **打开文件**并创建 [ExternalResource].
         * 注意, 返回的 [ExternalResource] 需要在使用完毕后调用 [ExternalResource.close] 关闭.
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
            RandomAccessFile(this, "r").toExternalResource(formatName).also {
                it.cast<ExternalResourceImplByFile>().origin = this@toExternalResource
            }

        /**
         * 创建 [ExternalResource].
         * 注意, 返回的 [ExternalResource] 需要在使用完毕后调用 [ExternalResource.close] 关闭, 届时将会关闭 [RandomAccessFile].
         *
         * **注意**：若关闭 [RandomAccessFile], 也会间接关闭 [ExternalResource].
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
            closeOriginalFileOnClose: Boolean = true,
        ): ExternalResource =
            ExternalResourceImplByFile(this, formatName, closeOriginalFileOnClose)

        /**
         * 创建 [ExternalResource]. 注意, 返回的 [ExternalResource] 需要在使用完毕后调用 [ExternalResource.close] 关闭.
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
         * 返回的 [ExternalResource] 需要在使用完毕后调用 [ExternalResource.close] 关闭.
         *
         * **注意**：本函数不会关闭流.
         *
         * ### 在 Java 获得和使用 [ExternalResource] 实例
         *
         * ```
         * try(ExternalResource resource = ExternalResource.create(file)) { // 使用文件 file
         *     contact.uploadImage(resource); // 用来上传图片
         *     contact.files.uploadNewFile("/foo/test.txt", file); // 或者用来上传文件
         * }
         * ```
         *
         * 注意, 若使用 [InputStream], 必须手动关闭 [InputStream]. 一种使用情况示例:
         *
         * ```
         * try(InputStream stream = ...) {
         *     try(ExternalResource resource = ExternalResource.create(stream)) {
         *         contact.uploadImage(resource); // 用来上传图片
         *         contact.files.uploadNewFile("/foo/test.txt", file); // 或者用来上传文件
         *     }
         * }
         * ```
         *
         *
         * @param formatName 查看 [ExternalResource.formatName]
         * @see ExternalResource
         */
        @JvmStatic
        @JvmOverloads
        @JvmName("create")
        @Throws(IOException::class) // not in BIO context so propagate IOException
        public fun InputStream.toExternalResource(formatName: String? = null): ExternalResource =
            Mirai.FileCacheStrategy.newCache(this, formatName)

        // endregion


        /* note:
        于 2.8.0-M1 添加 (#1392)

        于 2.8.0-RC 移动至 `toExternalResource`(#1588)
         */
        @JvmName("createAutoCloseable")
        @JvmStatic
        @Deprecated(
            level = DeprecationLevel.HIDDEN,
            message = "Moved to `toExternalResource()`",
            replaceWith = ReplaceWith("resource.toAutoCloseable()"),
        )
        @DeprecatedSinceMirai(errorSince = "2.8", hiddenSince = "2.10")
        public fun createAutoCloseable(resource: ExternalResource): ExternalResource {
            return resource.toAutoCloseable()
        }

        ///////////////////////////////////////////////////////////////////////////
        // region sendAsImageTo
        ///////////////////////////////////////////////////////////////////////////

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
            formatName: String? = null,
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

        // endregion

        ///////////////////////////////////////////////////////////////////////////
        // region uploadAsImage
        ///////////////////////////////////////////////////////////////////////////

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

        // endregion

        ///////////////////////////////////////////////////////////////////////////
        // region uploadAsFile
        ///////////////////////////////////////////////////////////////////////////

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
        @Suppress("DEPRECATION", "DEPRECATION_ERROR")
        @JvmStatic
        @JvmBlockingBridge
        @JvmOverloads
        @Deprecated(
            "Use sendTo instead.",
            ReplaceWith(
                "this.sendTo(contact, path, callback)",
                "net.mamoe.mirai.utils.ExternalResource.Companion.sendTo"
            ),
            level = DeprecationLevel.HIDDEN
        ) // deprecated since 2.7-M1
        @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
        public suspend fun File.uploadTo(
            contact: FileSupported,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null,
        ): FileMessage = toExternalResource().use {
            contact.filesRoot.resolve(path).upload(it, callback)
        }

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
        @Suppress("DEPRECATION", "DEPRECATION_ERROR")
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
            level = DeprecationLevel.HIDDEN
        ) // deprecated since 2.7-M1
        @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
        public suspend fun ExternalResource.uploadAsFile(
            contact: FileSupported,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null,
        ): FileMessage {
            return contact.filesRoot.resolve(path).upload(this, callback)
        }

        // endregion

        ///////////////////////////////////////////////////////////////////////////
        // region sendAsFileTo
        ///////////////////////////////////////////////////////////////////////////

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
        @Suppress("DEPRECATION_ERROR", "DEPRECATION")
        @Deprecated(
            "Deprecated. Please use AbsoluteFolder.uploadNewFile",
            ReplaceWith("contact.files.uploadNewFile(path, this, callback)")
        ) // deprecated since 2.8.0-RC
        @JvmStatic
        @JvmBlockingBridge
        @JvmOverloads
        @DeprecatedSinceMirai(warningSince = "2.8")
        public suspend fun <C : FileSupported> File.sendTo(
            contact: C,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null,
        ): MessageReceipt<C> = toExternalResource().use {
            contact.filesRoot.resolve(path).upload(it, callback).sendTo(contact)
        }

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
        @Suppress("DEPRECATION", "DEPRECATION_ERROR")
        @Deprecated(
            "Deprecated. Please use AbsoluteFolder.uploadNewFile",
            ReplaceWith("contact.files.uploadNewFile(path, this, callback)")
        ) // deprecated since 2.8.0-RC
        @JvmStatic
        @JvmBlockingBridge
        @JvmName("sendAsFile")
        @JvmOverloads
        @DeprecatedSinceMirai(warningSince = "2.8")
        public suspend fun <C : FileSupported> ExternalResource.sendAsFileTo(
            contact: C,
            path: String,
            callback: RemoteFile.ProgressionCallback? = null,
        ): MessageReceipt<C> {
            return contact.filesRoot.resolve(path).upload(this, callback).sendTo(contact)
        }

        // endregion

        ///////////////////////////////////////////////////////////////////////////
        // region uploadAsVoice
        ///////////////////////////////////////////////////////////////////////////

        @Suppress("DEPRECATION", "DEPRECATION_ERROR")
        @JvmBlockingBridge
        @JvmStatic
        @Deprecated(
            "Use `contact.uploadAudio(resource)` instead",
            level = DeprecationLevel.HIDDEN
        ) // deprecated since 2.7
        @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
        public suspend fun ExternalResource.uploadAsVoice(contact: Contact): net.mamoe.mirai.message.data.Voice {
            @Suppress("DEPRECATION", "DEPRECATION_ERROR")
            if (contact is Group) return contact.uploadAudio(this).toVoice()
            else throw UnsupportedOperationException("Contact `$contact` is not supported uploading voice")
        }
        // endregion
    }
}

/**
 * 一个实现了基本方法的外部资源
 *
 * ## 实现
 *
 * [AbstractExternalResource] 实现了大部分必要的方法,
 * 只有 [ExternalResource.inputStream], [ExternalResource.size] 还未实现
 *
 * 其中 [ExternalResource.inputStream] 要求每次读取的内容都是一致的
 *
 * Example:
 * ```
 * class MyCustomExternalResource: AbstractExternalResource() {
 *      override fun inputStream0(): InputStream = FileInputStream("/test.txt")
 *      override val size: Long get() = File("/test.txt").length()
 * }
 * ```
 *
 * ## 资源释放
 *
 * 如同 mirai 内置的 [ExternalResource] 实现一样,
 * [AbstractExternalResource] 也会被注册进入资源泄露监视器
 * (即意味着 [AbstractExternalResource] 也要求手动关闭)
 *
 * 为了确保逻辑正确性, [AbstractExternalResource] 不允许覆盖其 [close] 方法,
 * 必须在构造 [AbstractExternalResource] 的时候给定一个 [ResourceCleanCallback] 以进行资源释放
 *
 * 对于 [ResourceCleanCallback], 有以下要求
 *
 * - 没有对 [AbstractExternalResource] 的访问 (即没有 [AbstractExternalResource] 的任何引用)
 *
 * Example:
 * ```
 * class MyRes(
 *      cleanup: ResourceCleanCallback,
 *      val delegate: Closable,
 * ): AbstractExternalResource(cleanup) {
 * }
 *
 * // 错误, 该写法会导致 Resource 永远也不会被自动释放
 * lateinit var myRes: MyRes
 * val cleanup = ResourceCleanCallback {
 *      myRes.delegate.close()
 * }
 * myRes = MyRes(cleanup, fetchDelegate())
 *
 * // 正确
 * val delegate: Closable
 * val cleanup = ResourceCleanCallback {
 *      delegate.close()
 * }
 * val myRes = MyRes(cleanup, delegate)
 * ```
 *
 * @since 2.9
 *
 * @see ExternalResource
 * @see AbstractExternalResource.setResourceCleanCallback
 * @see AbstractExternalResource.registerToLeakObserver
 */
@Suppress("MemberVisibilityCanBePrivate")
public abstract class AbstractExternalResource
@JvmOverloads
public constructor(
    displayName: String? = null,
    cleanup: ResourceCleanCallback? = null,
) : ExternalResource {

    public constructor(
        cleanup: ResourceCleanCallback? = null,
    ) : this(null, cleanup)

    public fun interface ResourceCleanCallback {
        @Throws(IOException::class)
        public fun cleanup()
    }

    override val md5: ByteArray by lazy { inputStream().md5() }
    override val sha1: ByteArray by lazy { inputStream().sha1() }
    override val formatName: String by lazy {
        inputStream().detectFileTypeAndClose() ?: ExternalResource.DEFAULT_FORMAT_NAME
    }

    private val leakObserverRegistered = atomic(false)

    /**
     * 注册 [ExternalResource] 资源泄露监视器
     *
     * 受限于类继承构造器调用顺序, [AbstractExternalResource] 无法做到在完成初始化后马上注册监视器
     *
     * 该方法以允许 实现类 在完成初始化后直接注册资源监视器以避免意外的资源泄露
     *
     * 在不调用本方法的前提下, 如果没有相关的资源访问操作, `this` 可能会被意外泄露
     *
     * 正确示例:
     * ```
     * // Kotlin
     * public class MyResource: AbstractExternalResource() {
     *      init {
     *          val res: SomeResource
     *          // 一些资源初始化
     *          registerToLeakObserver()
     *          setResourceCleanCallback(Releaser(res))
     *      }
     *
     *      private class Releaser(
     *          private val res: SomeResource,
     *      ) : AbstractExternalResource.ResourceCleanCallback {
     *          override fun cleanup() = res.close()
     *      }
     * }
     *
     * // Java
     * public class MyResource extends AbstractExternalResource {
     *      public MyResource() throws IOException {
     *          SomeResource res;
     *          // 一些资源初始化
     *          registerToLeakObserver();
     *          setResourceCleanCallback(new Releaser(res));
     *      }
     *
     *      private static class Releaser implements ResourceCleanCallback {
     *          private final SomeResource res;
     *          Releaser(SomeResource res) { this.res = res; }
     *
     *          public void cleanup() throws IOException { res.close(); }
     *      }
     * }
     * ```
     *
     * @see setResourceCleanCallback
     */
    protected fun registerToLeakObserver() {
        // 用户自定义 AbstractExternalResource 也许会在 <init> 的时候失败
        // 于是在第一次使用 ExternalResource 相关的函数的时候注册 LeakObserver
        if (leakObserverRegistered.compareAndSet(expect = false, update = true)) {
            ExternalResourceLeakObserver.register(this, holder)
        }
    }

    /**
     * 该方法用于告知 [AbstractExternalResource] 不需要注册资源泄露监视器。
     * **仅在我知道我在干什么的前提下调用此方法**
     *
     * 不建议取消注册监视器, 这可能带来意外的错误
     *
     * @see registerToLeakObserver
     */
    protected fun dontRegisterLeakObserver() {
        leakObserverRegistered.value = true
    }

    final override fun inputStream(): InputStream {
        registerToLeakObserver()
        return inputStream0()
    }

    protected abstract fun inputStream0(): InputStream

    /**
     * 修改 `this` 的资源释放回调。
     * **仅在我知道我在干什么的前提下调用此方法**
     *
     * ```
     * class MyRes {
     * // region kotlin
     *
     *      private inner class Releaser : ResourceCleanCallback
     *
     *      private class NotInnerReleaser : ResourceCleanCallback
     *
     *      init {
     *          // 错误, 内部类, Releaser 存在对 MyRes 的引用
     *          setResourceCleanCallback(Releaser())
     *          // 错误, 匿名对象, 可能存在对 MyRes 的引用, 取决于编译器
     *          setResourceCleanCallback(object : ResourceCleanCallback {})
     *          // 正确, 无 inner 修饰, 等同于 java 的 private static class
     *          setResourceCleanCallback(NotInnerReleaser(directResource))
     *      }
     *
     * // endregion kotlin
     *
     * // region java
     *
     *      private class Releaser implements ResourceCleanCallback {}
     *      private static class StaticReleaser implements ResourceCleanCallback {}
     *
     *      MyRes() {
     *          // 错误, 内部类, 存在对 MyRes 的引用
     *          setResourceCleanCallback(new Releaser());
     *          // 错误, 匿名对象, 可能存在对 MyRes 的引用, 取决于 javac
     *          setResourceCleanCallback(new ResourceCleanCallback() {});
     *          // 正确
     *          setResourceCleanCallback(new StaticReleaser(directResource));
     *      }
     *
     * // endregion java
     * }
     * ```
     *
     * @see registerToLeakObserver
     */
    protected fun setResourceCleanCallback(cleanup: ResourceCleanCallback?) {
        holder.cleanup = cleanup
    }

    private class UsrCustomResHolder(
        @JvmField var cleanup: ResourceCleanCallback?,
        private val resourceName: String,
    ) : ExternalResourceHolder() {

        override val closed: Deferred<Unit> = CompletableDeferred()

        override fun closeImpl() {
            cleanup?.cleanup()
        }

        // display on logger of ExternalResourceLeakObserver
        override fun toString(): String = resourceName
    }

    private val holder = UsrCustomResHolder(cleanup, displayName ?: buildString {
        append("ExternalResourceHolder<")
        append(this@AbstractExternalResource.javaClass.name)
        append('@')
        append(System.identityHashCode(this@AbstractExternalResource))
        append('>')
    })

    final override val closed: Deferred<Unit> get() = holder.closed.also { registerToLeakObserver() }

    @Throws(IOException::class)
    final override fun close() {
        holder.close()
    }
}

/**
 * 执行 [action], 如果 [ExternalResource.isAutoClose], 在执行完成后调用 [ExternalResource.close].
 *
 * @since 2.8
 */
@MiraiExperimentalApi
// Continuing mark it as experimental until Kotlin's contextual receivers design is published.
// We might be able to make `action` a type `context(ExternalResource) () -> R`.
public inline fun <T : ExternalResource, R> T.withAutoClose(action: () -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    trySafely(
        block = { return action() },
        finally = { if (isAutoClose) close() }
    )
}

/**
 * 执行 [action], 如果 [ExternalResource.isAutoClose], 在执行完成后调用 [ExternalResource.close].
 *
 * @since 2.8
 */
@MiraiExperimentalApi
public inline fun <T : ExternalResource, R> T.runAutoClose(action: T.() -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return withAutoClose { action() }
}

/**
 * 执行 [action], 如果 [ExternalResource.isAutoClose], 在执行完成后调用 [ExternalResource.close].
 *
 * @since 2.8
 */
@MiraiExperimentalApi
public inline fun <T : ExternalResource, R> T.useAutoClose(action: (resource: T) -> R): R {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    return runAutoClose(action)
}
