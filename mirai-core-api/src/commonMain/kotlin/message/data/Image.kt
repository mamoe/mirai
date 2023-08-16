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

@file:Suppress(
    "EXPERIMENTAL_API_USAGE",
    "unused",
    "UnusedImport",
    "DEPRECATION_ERROR", "MemberVisibilityCanBePrivate"
)

package net.mamoe.mirai.message.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.data.Image.Builder
import net.mamoe.mirai.message.data.Image.Key.IMAGE_ID_REGEX
import net.mamoe.mirai.message.data.Image.Key.IMAGE_RESOURCE_ID_REGEX_1
import net.mamoe.mirai.message.data.Image.Key.IMAGE_RESOURCE_ID_REGEX_2
import net.mamoe.mirai.message.data.Image.Key.isUploaded
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage

/**
 * 自定义表情 (收藏的表情) 和普通图片.
 *
 *
 * 最推荐的存储方式是存储图片原文件, 每次发送图片时都使用文件上传.
 * 在上传时服务器会根据其缓存情况回复已有的图片 ID 或要求客户端上传.
 *
 * # 获取 [Image] 实例
 *
 * ## 根据 ID 构造图片
 *
 * ID 格式查看 [Image.imageId]. 通过 ID 构造的图片若未存在于服务器中, 发送后在客户端将不能正常显示. 可通过 [Image.isUploaded] 检查图片是否存在于服务器.
 * [Image.isUploaded] 需要除了 ID 以外其他参数, 如 [width], [size] 等, 因此不建议通过 ID 构造图片, 而是使用 [Builder] 构建, 以提供详细参数.
 *
 * 使用 [Image.fromId]. 在 Kotlin, 也可以使用顶层函数 `val image = Image("id")`.
 *
 * ### 在 Kotlin 通过 ID 构造图片
 * ```
 * // 根据喜好选择
 * val image = Image.fromId("id")
 * val image2 = Image("id")
 * ```
 *
 * ### 在 Java 通过 ID 构造图片
 * ```java
 * Image image = Image.fromId("id")
 * ```
 *
 * ## 使用 [Builder] 构建图片
 *
 * [Image] 提供 [Builder] 构建方式, 可以指定 [width], [height] 等额外参数. 请尽可能提供这些参数以提升图片发送的成功率和 [Image.isUploaded] 的准确性.
 *
 * ## 上传图片资源获得 [Image]
 *
 * 使用 [Contact.uploadImage], 将 [ExternalResource] 上传得到 [Image].
 *
 * 也可以使用 [ExternalResource.uploadAsImage] 扩展.
 *
 * # 发送图片消息
 *
 * 在获取实例后, 将图片元素连接到[消息链][MessageChain]即可发送. 图片可以与[纯文本][PlainText]等其他 [MessageContent] 混合使用 (在同一[消息链][MessageChain]中).
 *
 * # 下载图片
 *
 * 通过[事件][MessageEvent]等方式获取到 [Image] 实例后, 使用 [Image.queryUrl] 查询得到图片的下载地址, 自行使用 HTTP 客户端下载.
 *
 * # 其他查询
 *
 * ## 查询图片是否已存在于服务器
 *
 * 使用 [Image.isUploaded]. 当图片在服务器上存在时返回 `true`, 这意味着图片可以直接发送.
 *
 * 服务器通过 [Image.md5] 鉴别图片. 当图片已经存在于服务器时, [Contact.uploadImage] 会更快返回 (仍然需要进行网络请求), 不会上传图片数据.
 *
 * ## 检测图片 ID 合法性
 *
 * 使用 [Image.IMAGE_ID_REGEX].
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:image:*[Image.imageId]*&#93;
 *
 * @see FlashImage 闪照
 * @see Image.flash 转换普通图片为闪照
 */
@Suppress("DEPRECATION", "DEPRECATION_ERROR")
@Serializable(Image.Serializer::class)
@NotStableForInheritance
public interface Image : Message, MessageContent, CodableMessage {
    /**
     * 图片的 id.
     *
     * 图片 id 不一定会长时间保存, 也可能在将来改变格式, 因此不建议使用 id 发送图片.
     *
     * ### 格式
     * 所有图片的 id 都满足正则表达式 [IMAGE_ID_REGEX]. 示例: `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.ext` (ext 为文件后缀, 如 png)
     *
     * @see Image 使用 id 构造图片
     */
    public val imageId: String

    /**
     * 图片的宽度 (px), 当无法获取时为 0
     *
     * @since 2.8.0
     */
    public val width: Int

    /**
     * 图片的高度 (px), 当无法获取时为 0
     *
     * @since 2.8.0
     */
    public val height: Int

    /**
     * 图片的大小（字节）, 当无法获取时为 0. 可用于 [isUploaded].
     *
     * @since 2.8.0
     */
    public val size: Long

    /**
     * 图片的类型, 当无法获取时为未知 [ImageType.UNKNOWN]
     *
     * @since 2.8.0
     *
     * @see ImageType
     */
    public val imageType: ImageType

    /**
     * 判断该图片是否为 `动画表情`
     *
     * @since 2.8.0
     */
    public val isEmoji: Boolean get() = false

    /**
     * 图片文件 MD5. 可用于 [isUploaded].
     *
     * @return 16 bytes
     * @see isUploaded
     * @since 2.9.0
     */ // was an extension on Image before 2.9.0-M1.
    public val md5: ByteArray get() = calculateImageMd5ByImageId(imageId)

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitImage(this, data)
    }

    public object AsStringSerializer : KSerializer<Image> by String.serializer().mapPrimitive(
        SERIAL_NAME,
        serialize = { imageId },
        deserialize = { Image(it) },
    )

    @OptIn(MiraiInternalApi::class)
    @Deprecated(
        message = "For internal use only. Deprecated for removal. Please retrieve serializer from MessageSerializers.serializersModule.",
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.13") // error since 2.15, hidden since 2.16
    public object Serializer : KSerializer<Image> by FallbackSerializer(SERIAL_NAME)

    // move to mirai-core in 2.16. Delegate Serializer to the implementation from MessageSerializers.
    @MiraiInternalApi
    public open class FallbackSerializer(serialName: String) : KSerializer<Image> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(serialName) {
            element("imageId", String.serializer().descriptor)
            element("size", Long.serializer().descriptor)
            element("imageType", ImageType.serializer().descriptor)
            element("width", Int.serializer().descriptor)
            element("height", Int.serializer().descriptor)
            element("isEmoji", Boolean.serializer().descriptor)
        }

        // Note: Manually written to overcome discriminator issues.
        // Without this implementation you will need `ignoreUnknownKeys` on deserialization.
        override fun deserialize(decoder: Decoder): Image {
            return decoder.decodeStructure(descriptor) {
                @OptIn(ExperimentalSerializationApi::class)
                if (runCatching { this.decodeSequentially() }.getOrElse { false }) {
                    val imageId = this.decodeStringElement(descriptor, 0)
                    val size = this.decodeLongElement(descriptor, 1)
                    val type = this.decodeSerializableElement(descriptor, 2, ImageType.serializer())
                    val width = this.decodeIntElement(descriptor, 3)
                    val height = this.decodeIntElement(descriptor, 4)
                    val isEmoji = this.decodeBooleanElement(descriptor, 5)
                    return@decodeStructure Image(imageId) {
                        this.size = size
                        this.type = type
                        this.width = width
                        this.height = height
                        this.isEmoji = isEmoji
                    }
                } else {
                    return@decodeStructure Image("") {
                        while (true) {
                            when (val index = this@decodeStructure.decodeElementIndex(descriptor)) {
                                0 -> imageId = this@decodeStructure.decodeStringElement(descriptor, index)
                                1 -> size = this@decodeStructure.decodeLongElement(descriptor, index)
                                2 -> type = this@decodeStructure.decodeSerializableElement(
                                    descriptor,
                                    index,
                                    ImageType.serializer()
                                )

                                3 -> width = this@decodeStructure.decodeIntElement(descriptor, index)
                                4 -> height = this@decodeStructure.decodeIntElement(descriptor, index)
                                5 -> isEmoji = this@decodeStructure.decodeBooleanElement(descriptor, index)
                                CompositeDecoder.DECODE_DONE -> break
                            }
                        }
                        check(imageId.isNotEmpty()) { "imageId must not empty" }
                    }
                }
            }
        }

        override fun serialize(encoder: Encoder, value: Image) {
            Delegate.serializer().serialize(
                encoder, Delegate(
                    value.imageId,
                    value.size,
                    value.imageType,
                    value.width,
                    value.height,
                    value.isEmoji
                )
            )
        }

        @SerialName(SERIAL_NAME)
        @Serializable
        private data class Delegate(
            val imageId: String,
            val size: Long,
            val imageType: ImageType,
            val width: Int,
            val height: Int,
            val isEmoji: Boolean
        )
    }


    /**
     * [Image] 构建器.
     *
     * 示例:
     *
     * ```java
     * Builder builder = Image.Builder.newBuilder("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.jpg")
     * builder.setSize(123);
     * builder.setType(ImageType.PNG);
     *
     * Image image = builder.build();
     * ```
     *
     * @since 2.9.0
     */
    public class Builder private constructor(
        /**
         * @see Image.imageId
         */
        public var imageId: String,
    ) {
        /**
         * 图片大小字节数. 如果不提供该属性, 将无法 [Image.Key.isUploaded]
         *
         * @see Image.size
         */
        public var size: Long = 0

        /**
         * @see Image.imageType
         */
        public var type: ImageType = ImageType.UNKNOWN
        /**
         * @see Image.width
         */
        public var width: Int = 0

        /**
         * @see Image.height
         */
        public var height: Int = 0

        /**
         * @see Image.isEmoji
         */
        public var isEmoji: Boolean = false


        public fun build(): Image {
            if (type == ImageType.UNKNOWN) {
                type = ImageType.match(imageId.split(".").last())
            }
            @OptIn(MiraiInternalApi::class)
            return InternalImageProtocol.instance.createImage(
                imageId = imageId,
                size = size,
                type = type,
                width = width,
                height = height,
                isEmoji = isEmoji,
            )
        }

        public companion object {
            /**
             * 创建一个 [Builder]
             */
            @JvmStatic
            public fun newBuilder(imageId: String): Builder = Builder(imageId)
        }
    }

    @JvmBlockingBridge
    public companion object Key : AbstractMessageKey<Image>({ it.safeCast() }) {
        public const val SERIAL_NAME: String = "Image"

        /**
         * 通过 [Image.imageId] 构造一个 [Image] 以便发送.
         *
         * 图片 ID 不一定会长时间保存, 因此不建议使用 ID 发送图片. 建议使用 [Builder], 可以指定更多参数 (以及用于查询图片是否存在于服务器的必要参数 size).
         *
         * @see Image 获取更多说明
         * @see Image.imageId 获取更多说明
         * @see Builder
         */
        @JvmStatic
        public fun fromId(imageId: String): Image = Mirai.createImage(imageId)

        /**
         * 构造一个 [Image.Builder] 实例.
         *
         * @since 2.9.0
         */
        @JvmStatic
        public fun newBuilder(imageId: String): Builder = Builder.newBuilder(imageId)

        /**
         * 查询原图下载链接.
         *
         * - 当图片为从服务器接收的消息中的图片时, 可以直接获取下载链接, 本函数不会挂起协程.
         * - 其他情况下协程可能会挂起并向服务器查询下载链接, 或不挂起并拼接一个链接.
         *
         * @return 原图 HTTP 下载链接
         * @throws IllegalStateException 当无任何 [Bot] 在线时抛出 (因为无法获取相关协议)
         */
        @JvmStatic
        public suspend fun Image.queryUrl(): String {
            val bot = Bot.instancesSequence.firstOrNull() ?: error("No Bot available to query image url")
            return Mirai.queryImageUrl(bot, this)
        }

        /**
         * 当图片在服务器上存在时返回 `true`, 这意味着图片可以直接发送给 [contact].
         *
         * 若返回 `false`, 则图片需要用 [ExternalResource] 重新上传 ([Contact.uploadImage]).
         *
         * @since 2.9.0
         */
        @JvmStatic
        public suspend fun Image.isUploaded(bot: Bot): Boolean {
            @OptIn(MiraiInternalApi::class)
            return InternalImageProtocol.instance.isUploaded(bot, md5, size, null, imageType, width, height)
        }

        /**
         * 当图片在服务器上存在时返回 `true`, 这意味着图片可以直接发送给 [contact].
         *
         * 若返回 `false`, 则图片需要用 [ExternalResource] 重新上传 ([Contact.uploadImage]).
         *
         * @param md5 图片文件 MD5. 可通过 [Image.md5] 获得.
         * @param size 图片文件大小.
         *
         * @since 2.9.0
         */
        @JvmStatic
        public suspend fun isUploaded(
            bot: Bot,
            md5: ByteArray,
            size: Long,
        ): Boolean {
            @OptIn(MiraiInternalApi::class)
            return InternalImageProtocol.instance.isUploaded(bot, md5, size, null)
        }

        /**
         * 由 [Image.imageId] 计算 [Image.md5].
         *
         * @since 2.9.0
         */
        public fun calculateImageMd5ByImageId(imageId: String): ByteArray {
            @OptIn(MiraiInternalApi::class)
            return when {
                imageId matches IMAGE_ID_REGEX -> imageId.imageIdToMd5(1)
                imageId matches IMAGE_RESOURCE_ID_REGEX_2 -> imageId.imageIdToMd5(imageId.skipToSecondHyphen() + 1)
                imageId matches IMAGE_RESOURCE_ID_REGEX_1 -> imageId.imageIdToMd5(1)

                else -> throw IllegalArgumentException(
                    "Illegal imageId: '$imageId'. $ILLEGAL_IMAGE_ID_EXCEPTION_MESSAGE"
                )
            }
        }

        /**
         * 统一 ID 正则表达式
         *
         * `{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.ext`
         */
        @Suppress("RegExpRedundantEscape") // This is required on Android
        @JvmStatic
        @get:JvmName("getImageIdRegex")
        public val IMAGE_ID_REGEX: Regex =
            Regex("""\{[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\}\..{3,5}""")

        /**
         * 图片资源 ID 正则表达式 1. mirai 内部使用.
         *
         * `/f8f1ab55-bf8e-4236-b55e-955848d7069f`
         * @see IMAGE_RESOURCE_ID_REGEX_2
         */
        @JvmStatic
        @MiraiInternalApi
        @get:JvmName("getImageResourceIdRegex1")
        public val IMAGE_RESOURCE_ID_REGEX_1: Regex =
            Regex("""/[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}""")

        /**
         * 图片资源 ID 正则表达式 2. mirai 内部使用.
         *
         * `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
         * @see IMAGE_RESOURCE_ID_REGEX_1
         */
        @JvmStatic
        @MiraiInternalApi
        @get:JvmName("getImageResourceIdRegex2")
        public val IMAGE_RESOURCE_ID_REGEX_2: Regex =
            Regex("""/[0-9]*-[0-9]*-[0-9a-fA-F]{32}""")
    }
}

/**
 * 通过 [Image.imageId] 构造一个 [Image] 以便发送.
 *
 * 图片 ID 不一定会长时间保存, 因此不建议使用 ID 发送图片. 建议使用 [Image.Builder], 可以指定更多参数 (以及用于查询图片是否存在于服务器的必要参数 size).
 *
 * @see Image 获取更多关于 [Image] 的说明
 * @see Image.Builder 获取更多关于构造 [Image] 的方法
 *
 * @see IMirai.createImage
 */
@JvmSynthetic
public fun Image(imageId: String): Image = Builder.newBuilder(imageId).build()

/**
 * 使用 [Image.Builder] 构建一个 [Image].
 *
 * @see Image.Builder
 * @since 2.9.0
 */
@JvmSynthetic
public inline fun Image(imageId: String, builderAction: Builder.() -> Unit = {}): Image =
    Builder.newBuilder(imageId).apply(builderAction).build()

@Serializable
public enum class ImageType(
    /**
     * @since 2.9.0
     */
    @MiraiInternalApi public val formatName: String,
) {
    PNG("png"),
    BMP("bmp"),
    JPG("jpg"),
    GIF("gif"),

    //WEBP, //Unsupported by pc client
    APNG("png"),
    UNKNOWN("gif"); // bad design, should use `null` to represent unknown, but we cannot change it anymore.

    public companion object {
        private val IMAGE_TYPE_ENUM_LIST = values()

        @JvmStatic
        public fun match(str: String): ImageType {
            return matchOrNull(str) ?: UNKNOWN
        }

        @JvmStatic
        public fun matchOrNull(str: String): ImageType? {
            val input = str.uppercase()
            return IMAGE_TYPE_ENUM_LIST.firstOrNull { it.name == input }
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Internals
///////////////////////////////////////////////////////////////////////////

@Deprecated("Use member function", level = DeprecationLevel.HIDDEN) // safe since it was internal
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@MiraiInternalApi
@get:JvmName("calculateImageMd5")
@DeprecatedSinceMirai(hiddenSince = "2.9")
public val Image.md5: ByteArray
    get() = Image.calculateImageMd5ByImageId(imageId)


/**
 * 内部图片协议实现
 * @since 2.9.0-M1
 */
@MiraiInternalApi
public interface InternalImageProtocol { // naming it Internal* to assign it a lower priority when resolving Image*
    public fun createImage(
        imageId: String,
        size: Long,
        type: ImageType = ImageType.UNKNOWN,
        width: Int = 0,
        height: Int = 0,
        isEmoji: Boolean = false
    ): Image

    /**
     * @param context 用于检查的 [Contact]. 群图片与好友图片是两个通道, 建议使用欲发送到的 [Contact] 对象作为 [contact] 参数, 但目前不提供此参数时也可以检查.
     */
    public suspend fun isUploaded(
        bot: Bot,
        md5: ByteArray,
        size: Long,
        context: Contact? = null,
        type: ImageType = ImageType.UNKNOWN,
        width: Int = 0,
        height: Int = 0
    ): Boolean

    @MiraiInternalApi
    public companion object {
        public val instance: InternalImageProtocol by lazy {
            Mirai // initialize MiraiImpl first
            loadService(
                InternalImageProtocol::class,
                "net.mamoe.mirai.internal.message.InternalImageProtocolImpl"
            )
        }
    }
}
