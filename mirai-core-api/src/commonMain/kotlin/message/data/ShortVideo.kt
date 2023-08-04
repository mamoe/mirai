/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.message.data.ShortVideo.Builder
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*

/**
 * 短视频消息，指的是可在聊天界面在线播放的视频消息，而非在群文件上传的视频文件.
 *
 * 短视频消息分为 [OnlineShortVideo] 与 [OfflineShortVideo]. 在本地上传的短视频为 [OfflineShortVideo]. 从服务器接收的短视频为 [OnlineShortVideo].
 *
 * 最推荐存储的方式是下载视频文件，每次都通过上传该文件获取视频消息.
 * 在上传视频时服务器会根据缓存情况选择回复已有视频 ID 或要求客户端上传.
 *
 * # 获取短视频消息示例
 *
 * ## 上传短视频
 * 使用 [Contact.uploadShortVideo]，将视频缩略图和视频[资源][ExternalResource] 上传以得到 [OfflineShortVideo].
 *
 * ## 使用 [Builder] 构建短视频
 * [ShortVideo] 提供 [Builder] 构建方式，必须指定 [videoId], [fileName], [fileMd5], [fileSize] 和 [fileFormat] 参数.
 * 可选指定 [Builder.thumbnailMd5] 和 [Builder.thumbnailSize]. 若不提供，可能会影响服务器判断缓存.
 *
 * ## 从服务器接收
 * 通过监听消息接收的短视频消息可直接转换为 [OnlineShortVideo].
 *
 * # 下载视频
 * 通过 [OnlineShortVideo.urlForDownload] 获取下载链接.
 * 该下载链接不包含短视频的文件信息，可以使用 [videoId] 或 [fileName] 作为文件名，[fileFormat] 作为文件拓展名.
 *
 * # 其他信息
 *
 * ## mirai 码支持
 * [ShortVideo] 不支持 mirai 码，意味着如果通过 mirai 码持久化消息，短视频消息将不会被存储.
 *
 * @since 2.16
 */
@NotStableForInheritance
public interface ShortVideo : MessageContent, ConstrainSingle {
    /**
     * 视频 ID.
     */
    public val videoId: String

    /**
     * 视频文件 MD5. 16 bytes.
     */
    public val fileMd5: ByteArray

    /*
     * 视频大小
     */
    public val fileSize: Long

    /**
     * 视频文件类型（拓展名）
     */
    public val fileFormat: String

    /*
     * 视频文件名，不包括拓展名
     */
    public val fileName: String


    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitShortVideo(this, data)
    }

    override val key: MessageKey<*>
        get() = Key


    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, ShortVideo>(MessageContent, { it.safeCast() }) {

    }

    public class Builder internal constructor(
        public var videoId: String
    ) {
        public constructor() : this("")

        /**
         * @see ShortVideo.fileMd5
         */
        public var fileMd5: ByteArray = EMPTY_BYTE_ARRAY

        /**
         * @see ShortVideo.fileSize
         */
        public var fileSize: Long = 0

        /**
         * @see ShortVideo.fileFormat
         */
        public var fileFormat: String = ""

        /**
         * @see ShortVideo.fileName
         */
        public var fileName: String = ""

        /**
         * 缩略图文件 MD5，仅通过 [Contact.uploadShortVideo] 获取的 [OfflineShortVideo] 或 [OnlineShortVideo] 的 mirai 码获取.
         */
        public var thumbnailMd5: ByteArray = EMPTY_BYTE_ARRAY

        /**
         * 缩略图文件大小，仅通过 [Contact.uploadShortVideo] 获取的 [OfflineShortVideo] 或 [OnlineShortVideo] 的 mirai 码获取.
         */
        public var thumbnailSize: Long = 0

        public fun build(): ShortVideo {
            if (videoId.isEmpty()) {
                throw IllegalArgumentException("videoId is empty.")
            }
            if (fileMd5.contentEquals(EMPTY_BYTE_ARRAY)) {
                throw IllegalArgumentException("fileMd5 is empty.")
            }
            if (fileSize == 0L) {
                throw IllegalArgumentException("fileSize is zero.")
            }
            if (fileFormat.isEmpty()) {
                throw IllegalArgumentException("fileFormat is empty.")
            }
            if (fileName.isEmpty()) {
                throw IllegalArgumentException("fileName is empty.")
            }

            @OptIn(MiraiInternalApi::class)
            return InternalShortVideoProtocol.instance.createOfflineShortVideo(
                videoId, fileMd5, fileSize, fileFormat, fileName, thumbnailMd5, thumbnailSize
            )
        }

        public companion object {
            /**
             * 创建一个 [Builder]
             */
            @JvmStatic
            public fun newBuilder(videoId: String): Builder = Builder(videoId)
        }
    }
}

/**
 * 在线短视频消息，即从消息事件中接收到的视频消息.
 *
 * [OnlineShortVideo] 仅可以从事件中的[消息链][MessageChain]接收, 不可手动构造. 若需要手动构造, 请使用 [ShortVideo.Builder] 构造 [离线短视频][OfflineShortVideo].
 *
 * ### 序列化支持
 *
 * [OnlineShortVideo] 支持序列化. 可使用 [MessageChain.serializeToJsonString] 以及 [MessageChain.deserializeFromJsonString].
 * 也可以在 [MessageSerializers.serializersModule] 获取到 [OnlineShortVideo] 的 [KSerializer].
 *
 * 要获取更多有关序列化的信息, 参阅 [MessageSerializers].
 *
 * @since 2.16
 */
@NotStableForInheritance
public interface OnlineShortVideo : ShortVideo {
    /**
     * 下载链接
     */
    public val urlForDownload: String

    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, OnlineShortVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "OnlineShortVideo"
    }
}

/**
 * 离线短视频消息.
 *
 * [OfflineShortVideo] 拥有协议上必要的五个属性:
 * - 视频 ID [videoId]
 * - 视频文件名 [fileName]
 * - 视频 MD5 [fileMd5]
 * - 视频大小 [fileSize]
 * - 视频格式 [fileFormat]
 *
 * 和非必要属性：
 * - 缩略图 MD5 `thumbnailMd5`
 * - 缩略图大小 `thumbnailSize`
 *
 * [OfflineShortVideo] 可由本地 [ExternalResource] 经过 [AudioSupported.uploadShortVideo] 上传到服务器得到, 故无[下载链接][OnlineShortVideo.urlForDownload].
 *
 * [OfflineShortVideo] 支持使用 [ShortVideo.Builder] 可通过上述七个参数获得 [OfflineShortVideo] 实例.
 *
 * ### 序列化支持
 *
 * [OfflineShortVideo] 支持序列化. 可使用 [MessageChain.serializeToJsonString] 以及 [MessageChain.deserializeFromJsonString].
 * 也可以在 [MessageSerializers.serializersModule] 获取到 [OfflineShortVideo] 的 [KSerializer].
 *
 * 要获取更多有关序列化的信息, 参阅 [MessageSerializers].
 * @since 2.16
 */
@NotStableForInheritance
public interface OfflineShortVideo : ShortVideo {

    public companion object Key :
        AbstractPolymorphicMessageKey<ShortVideo, OfflineShortVideo>(ShortVideo, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "OfflineShortVideo"
    }
}

/**
 * 内部短视频协议实现
 * @since 2.16.0
 */
@MiraiInternalApi
public interface InternalShortVideoProtocol {
    public fun createOfflineShortVideo(
        videoId: String,
        fileMd5: ByteArray,
        fileSize: Long,
        fileFormat: String,
        fileName: String,
        thumbnailMd5: ByteArray,
        thumbnailSize: Long
    ): OfflineShortVideo

    @MiraiInternalApi
    public companion object {
        public val instance: InternalShortVideoProtocol by lazy {
            Mirai // initialize MiraiImpl first
            loadService(
                InternalShortVideoProtocol::class,
                "net.mamoe.mirai.internal.message.InternalShortVideoProtocolImpl"
            )
        }
    }
}