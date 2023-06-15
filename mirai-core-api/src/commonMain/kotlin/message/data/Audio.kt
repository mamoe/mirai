/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:JvmMultifileClass
@file:JvmName("MessageUtils")

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import net.mamoe.mirai.contact.AudioSupported
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * 语音消息.
 *
 * [Audio] 分为 [OnlineAudio] 与 [OfflineAudio]. 在本地上传的, 或手动构造的语音为 [OfflineAudio]. 从服务器接收的语音为 [OnlineAudio].
 *
 * ## 上传和发送语音
 *
 * 使用 [AudioSupported.uploadAudio] 上传语音到服务器并取得 [Audio] 消息实例, 然后通过 [Contact.sendMessage] 发送.
 *
 * Java 示例:
 * ```
 * Audio audio;
 * try {
 *     audio = group.uploadAudio(resource); // 上传文件得到语音实例
 * } finally {
 *     resource.close(); // 保证资源正常关闭
 * }
 * group.sendMessage(audio); // 发送语音消息
 * ```
 *
 * ## 下载语音
 *
 * 使用 [OnlineAudio.urlForDownload] 获取文件下载链接.
 *
 * ## [Audio] 与 [Voice] 的转换
 *
 * 原 [Voice] 已弃用故不推荐进行兼容转换. [Audio] 将有稳定性保证, 请尽量使用新的 [Audio].
 *
 * 将 [Audio] 转为 [Voice]: [Voice.fromAudio]
 * 将 [Voice] 转为 [Audio]: [Voice.toAudio]
 *
 * @since 2.7
 */
public sealed interface Audio : MessageContent, ConstrainSingle {
    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, Audio>(MessageContent, { it.safeCast() })

    /**
     * 文件名称. 通常为 `XXX.amr`. 服务器要求文件名后缀必须为 ".amr", 但其[编码方式][codec]也有可能是非 [AudioCodec.AMR].
     */
    public val filename: String

    /**
     * 文件 MD5. 16 bytes.
     */
    public val fileMd5: ByteArray

    /**
     * 文件大小 bytes. 官方客户端支持最大文件大小约为 1MB, 过大的文件**可能**可以正常上传, 但在官方客户端无法收听 (显示文件损坏).
     */
    public val fileSize: Long

    /**
     * 编码方式.
     *
     * - 若语音文件真实编码方式为 [AudioCodec.SILK], 而该属性为 [AudioCodec.AMR], 语音文件将会被服务器压缩为低音质 [AudioCodec.AMR] 格式.
     * - 若语音文件真实编码方式为 [AudioCodec.AMR], 而该属性为 [AudioCodec.SILK], 语音也可以正常发送并在客户端收听, 音质随文件真实格式而决定.
     *
     * 因此在发送时 [codec] 通常可以总是使用 [AudioCodec.SILK] (这也是 [AudioSupported.uploadAudio] 的默认行为).
     */
    public val codec: AudioCodec

    /**
     * 文件的额外数据. 该数据由服务器提供, 可能会影响语音音质等属性.
     * [extraData] 为 `null` 时也可以发送语音, 但不确定发送和客户端收听是否会正常.
     *
     * [extraData] 可能随服务器更新而更新, 因此请不要尝试解析该数据.
     *
     * [extraData] 向下兼容, 即旧版本的 [extraData] 可以在新版本构造 [OfflineAudio] ([OfflineAudio.Factory.create]).
     */
    public val extraData: ByteArray?

    /**
     * @return `"[mirai:audio:${filename}]"`
     */
    public override fun toString(): String
    public override fun contentToString(): String = "[语音消息]"

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitAudio(this, data)
    }

    override val key: MessageKey<*> get() = Key
}


/**
 * 在线语音消息, 即从消息事件中接收到的语音消息.
 *
 * [OnlineAudio] 可以获取[语音长度][length]以及[下载链接][urlForDownload].
 *
 * [OnlineAudio] 仅可以从事件中的[消息链][MessageChain]接收, 不可手动构造. 若需要手动构造, 请使用 [OfflineAudio.Factory.create] 构造 [离线语音][OfflineAudio].
 *
 * ### 序列化支持
 *
 * [OnlineAudio] 支持序列化. 可使用 [MessageChain.serializeToJsonString] 以及 [MessageChain.deserializeFromJsonString].
 * 也可以在 [MessageSerializers.serializersModule] 获取到 [OnlineAudio] 的 [KSerializer].
 *
 * 要获取更多有关序列化的信息, 参阅 [MessageSerializers].
 *
 * ### 不建议自行实现该接口
 *
 * [OnlineAudio] 不稳定, 将来可能会增加新的抽象属性或方法而导致不兼容. 仅可以使用该接口而不能继承或实现它.
 *
 * @since 2.7
 * @see OfflineAudio
 */
@NotStableForInheritance
public interface OnlineAudio : Audio { // 协议实现
    /**
     * 下载链接 HTTP URL.
     * @return `"http://xxx"`
     */
    public val urlForDownload: String

    /**
     * 语音长度秒数
     */
    public val length: Long

    public companion object Key :
        AbstractPolymorphicMessageKey<Audio, OnlineAudio>(Audio, { it.safeCast() }) {

        public const val SERIAL_NAME: String = "OnlineAudio"
    }
}

/**
 * 获取语音长度秒数, 作为 [Duration].
 * @since 2.11
 */
@get:JvmSynthetic
public inline val OnlineAudio.lengthDuration: Duration
    get() = length.seconds

/**
 * 离线语音消息.
 *
 * [OfflineAudio] 仅拥有协议上必要的五个属性:
 * - 文件名 [filename]
 * - 文件 MD5 [fileMd5]
 * - 文件大小 [fileSize]
 * - 编码方式 [codec]
 * - 额外数据 [extraData]
 *
 * [OfflineAudio] 可由本地 [ExternalResource] 经过 [AudioSupported.uploadAudio] 上传到服务器得到, 故无[下载链接][OnlineAudio.urlForDownload].
 *
 * [OfflineAudio] 同时还可以用做自定义构造 [Audio] 实例, 使用 [OfflineAudio.Factory.create] 可通过上述五个必要参数获得 [OfflineAudio] 实例.
 *
 * ### 序列化支持
 *
 * [OfflineAudio] 支持序列化. 可使用 [MessageChain.serializeToJsonString] 以及 [MessageChain.deserializeFromJsonString].
 * 也可以在 [MessageSerializers.serializersModule] 获取到 [OfflineAudio] 的 [KSerializer].
 *
 * 要获取更多有关序列化的信息, 参阅 [MessageSerializers].
 *
 * ### 不建议自行实现该接口
 *
 * [OfflineAudio] 不稳定, 将来可能会增加新的抽象属性或方法而导致不兼容. 仅可以使用该接口而不能继承或实现它.
 *
 * @since 2.7
 */
@NotStableForInheritance
public interface OfflineAudio : Audio {

    public companion object Key :
        AbstractPolymorphicMessageKey<Audio, OfflineAudio>(Audio, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "OfflineAudio"
    }

    public interface Factory {
        /**
         * 构造 [OfflineAudio]. 有关参数的含义, 参考 [Audio].
         *
         * 在 Kotlin 可以使用类构造器的函数 [OfflineAudio]: `OfflineAudio(...)`
         */
        public fun create(
            filename: String,
            fileMd5: ByteArray,
            fileSize: Long,
            codec: AudioCodec,
            extraData: ByteArray?,
        ): OfflineAudio

        /**
         * 使用 [OnlineAudio] 的信息构造 [OfflineAudio].
         *
         * 在 Kotlin 可以使用类构造器的函数 [OfflineAudio]: `OfflineAudio(...)`
         */
        public fun from(onlineAudio: OnlineAudio): OfflineAudio = onlineAudio.run {
            create(filename, fileMd5, fileSize, codec, extraData)
        }

        public companion object INSTANCE :
            Factory by loadService("net.mamoe.mirai.internal.message.data.OfflineAudioFactoryImpl")
    }
}

/**
 * 构造 [OfflineAudio]. 有关参数的含义, 参考 [Audio].
 * @since 2.7
 */
@JvmSynthetic
public inline fun OfflineAudio(
    filename: String,
    fileMd5: ByteArray,
    fileSize: Long,
    codec: AudioCodec,
    extraData: ByteArray?,
): OfflineAudio = OfflineAudio.Factory.create(filename, fileMd5, fileSize, codec, extraData)

/**
 * 使用 [OnlineAudio] 的信息构造 [OfflineAudio].
 * @since 2.7
 */
@JvmSynthetic
public inline fun OfflineAudio(
    onlineAudio: OnlineAudio
): OfflineAudio = OfflineAudio.Factory.from(onlineAudio)


/**
 * 语音编码方式.
 *
 * @since 2.7
 */
@Serializable(AudioCodec.AsIntSerializer::class)
public enum class AudioCodec(
    public val id: Int,
    public val formatName: String,
) {
    /**
     * 低音质编码格式
     */
    AMR(0, "amr"),

    /**
     * 高音质编码格式
     */
    SILK(1, "silk");

    public companion object {
        private val VALUES = values()

        @JvmStatic
        public fun fromId(id: Int): AudioCodec = VALUES.first { it.id == id }

        @JvmStatic
        public fun fromFormatName(formatName: String): AudioCodec = VALUES.first { it.formatName == formatName }

        @JvmStatic
        public fun fromIdOrNull(id: Int): AudioCodec? = VALUES.find { it.id == id }

        @JvmStatic
        public fun fromFormatNameOrNull(formatName: String): AudioCodec? =
            VALUES.find { it.formatName == formatName }
    }

    internal object AsIntSerializer : KSerializer<AudioCodec> by Int.serializer().map(
        Int.serializer().descriptor,
        deserialize = { fromId(it) },
        serialize = { id }
    )
}