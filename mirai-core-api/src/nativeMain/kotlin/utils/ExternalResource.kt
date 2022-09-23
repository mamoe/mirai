/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.internal.utils.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.sendTo
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

/**
 * 一个*不可变的*外部资源. 仅包含资源内容, 大小, 文件类型, 校验值而不包含文件名, 文件位置等. 外部资源有可能是一个文件, 也有可能只存在于内存, 或者以任意其他方式实现.
 *
 * [ExternalResource] 在创建之后就应该保持其属性的不变, 即任何时候获取其属性都应该得到相同结果, 任何时候打开流都得到的一样的数据.
 *
 * # 创建
 * - [ByteArray.toExternalResource]
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
 * 注意, 若使用 [Input], 必须手动关闭 [Input]. 一种使用情况示例:
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
 * 注意, 若使用 [Input], 必须手动关闭 [Input]. 一种使用情况示例:
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
 * 当 [ExternalResource] 创建时就可能会打开一些资源.
 * 类似于 [Input], [ExternalResource] 需要被 [关闭][close].
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
public actual interface ExternalResource : Closeable {

    /**
     * 是否在 _使用一次_ 后自动 [close].
     *
     * 该属性仅供调用方参考. 如 [Contact.uploadImage] 会在方法结束时关闭 [isAutoClose] 为 `true` 的 [ExternalResource], 无论上传图片是否成功.
     *
     * 所有 mirai 内置的上传图片, 上传语音等方法都支持该行为.
     *
     * @since 2.8
     */
    public actual val isAutoClose: Boolean
        get() = false

    /**
     * 文件内容 MD5. 16 bytes
     */
    public actual val md5: ByteArray

    /**
     * 文件内容 SHA1. 16 bytes
     * @since 2.5
     */
    public actual val sha1: ByteArray
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
    public actual val formatName: String

    /**
     * 文件大小 bytes
     */
    public actual val size: Long

    /**
     * 当 [close] 时会 [CompletableDeferred.complete] 的 [Deferred].
     */
    public actual val closed: Deferred<Unit>

    /**
     * 打开 [Input]. 在返回的 [Input] 被 [关闭][Input.close] 前无法再次打开流.
     *
     * 关闭此流不会关闭 [ExternalResource].
     * @throws IllegalStateException 当上一个流未关闭又尝试打开新的流时抛出
     *
     * @since 2.13
     */
    @MiraiInternalApi
    public actual fun input(): Input

    @MiraiInternalApi
    public actual fun calculateResourceId(): String {
        return generateImageId(md5, formatName.ifEmpty { DEFAULT_FORMAT_NAME })
    }

    /**
     * 该 [ExternalResource] 的数据来源, 可能有以下的返回
     *
     * - [ByteArray] RAM
     * - ...
     *
     * implementation note:
     *
     * - 对于无法二次读取的数据来源 (如 [Input]), 返回 `null`
     * - 不要返回 [String], 没有约定 [String] 代表什么
     * - 数据源外漏会严重影响 [inputStream] 等的执行的可以返回 `null` (如文件句柄)
     *
     * @since 2.8.0
     */
    public actual val origin: Any? get() = null

    /**
     * 创建一个在 _使用一次_ 后就会自动 [close] 的 [ExternalResource].
     *
     * @since 2.8.0
     */
    public actual fun toAutoCloseable(): ExternalResource {
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


    public actual companion object {
        /**
         * 在无法识别文件格式时使用的默认格式名. "mirai".
         *
         * @see ExternalResource.formatName
         */
        public actual const val DEFAULT_FORMAT_NAME: String = "mirai"

        ///////////////////////////////////////////////////////////////////////////
        // region toExternalResource
        ///////////////////////////////////////////////////////////////////////////

        /**
         * 创建 [ExternalResource]. 注意, 返回的 [ExternalResource] 需要在使用完毕后调用 [ExternalResource.close] 关闭.
         *
         * @param formatName 查看 [ExternalResource.formatName]
         */
        public actual fun ByteArray.toExternalResource(formatName: String?): ExternalResource =
            ExternalResourceImplByByteArray(this, formatName)

        // endregion

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
        public actual suspend fun <C : Contact> ExternalResource.sendAsImageTo(contact: C): MessageReceipt<C> =
            contact.uploadImage(this).sendTo(contact)

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
        public actual suspend fun ExternalResource.uploadAsImage(contact: Contact): Image = contact.uploadImage(this)

        // endregion
    }
}
