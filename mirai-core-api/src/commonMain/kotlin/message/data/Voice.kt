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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.visitor.MessageVisitor
import net.mamoe.mirai.utils.*
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic


/**
 * 需要通过上传到服务器的消息，如语音、文件.
 *
 * @suppress 不要使用这个接口. 目前只应该使用 [Voice].
 */
@Serializable
@MiraiExperimentalApi
@NotStableForInheritance
public abstract class PttMessage : MessageContent {

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, PttMessage>(MessageContent, { it.safeCast() })

    @MiraiExperimentalApi
    public abstract val fileName: String

    @MiraiExperimentalApi
    public abstract val md5: ByteArray

    @MiraiExperimentalApi
    public abstract val fileSize: Long

    /*
     * **internal impl note**
     * 用于中转 ImMsgBody.Ptt, 在接受到其他用户发送的语音时能按照原样发回,
     * 并且便于未来修改 (对 api 修改最小化)
     */
    @MiraiInternalApi
    @Transient
    public var pttInternalInstance: Any? = null
}

/**
 * 已弃用的旧版本语音消息.
 *
 * [Voice] 由于有设计缺陷已弃用且可能会在将来版本删除, 请使用 [Audio].
 *
 * ## 迁移指南
 *
 * - 将使用的 [Voice] 类型替换为 [Audio] 类型
 * - 将 [Group.uploadVoice] 替换为 [Group.uploadAudio]
 * - 如果有必须使用旧 [Voice] 类型的情况, 请使用 [Audio.toVoice]
 */
@OptIn(MiraiExperimentalApi::class)
@Suppress("DuplicatedCode", "DEPRECATION_ERROR", "PropertyName", "HttpUrlsUsage")
@Serializable
@SerialName(Voice.SERIAL_NAME)
@Deprecated(
    "Please use Audio instead.",
    replaceWith = ReplaceWith("Audio", "net.mamoe.mirai.message.data.Audio"),
    level = DeprecationLevel.HIDDEN
) // deprecated since 2.7
@DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
public open class Voice @MiraiInternalApi constructor(
    @MiraiExperimentalApi public override val fileName: String,
    @MiraiExperimentalApi public override val md5: ByteArray,
    @MiraiExperimentalApi public override val fileSize: Long,

    @SerialName("codec") @MiraiInternalApi public val _codec: Int = 0,
    private val _url: String
) : PttMessage() {

    @Deprecated(
        "Please use Audio instead.",
        replaceWith = ReplaceWith("Audio.Key", "net.mamoe.mirai.message.data.Audio.Key"),
        level = DeprecationLevel.HIDDEN
    ) // deprecated since 2.7
    @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
    public companion object Key : AbstractPolymorphicMessageKey<PttMessage, Voice>(PttMessage, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "Voice"

        /**
         * 将 2.7 新增的 [Audio] 转为旧版本的 [Voice], 以兼容某些情况.
         *
         * @see Audio.toVoice
         * @since 2.7
         */
        @OptIn(MiraiInternalApi::class)
        @Deprecated(
            "Please consider migrating to Audio",
            level = DeprecationLevel.ERROR
        ) // deprecated since 2.7
        @DeprecatedSinceMirai(
            warningSince = "2.7",
            errorSince = "2.10"
        )  // if HIDDEN, it cannot be resolved by Audio.toVoice
        @JvmStatic
        public fun fromAudio(audio: Audio): Voice {
            audio.run {
                return Voice(
                    filename,
                    fileMd5,
                    fileSize,
                    codec.id,
                    if (this is OnlineAudio) kotlin.runCatching { urlForDownload }.getOrElse { "" } else ""
                )
            }
        }
    }

    /**
     * 下载链接 HTTP URL.
     */
    public open val url: String?
        get() = when {
            _url.isBlank() -> null
            _url.startsWith("http") -> _url
            else -> "http://grouptalk.c2c.qq.com$_url"
        }

    private var _stringValue: String? = null
        get() = field ?: kotlin.run {
            field = "[mirai:voice:$fileName]"
            field
        }

    public override fun toString(): String = _stringValue!!

    public override fun contentToString(): String = "[语音消息]"

    /**
     * 转换为 2.7 新增的 [Audio], 以兼容某些无法迁移的情况.
     *
     * @since 2.7
     */
    @OptIn(MiraiExperimentalApi::class, MiraiInternalApi::class)
    public fun toAudio(): Audio {
        val voice = this
        return OfflineAudio(
            voice.fileName,
            voice.md5,
            voice.fileSize,
            AudioCodec.fromIdOrNull(voice._codec) ?: AudioCodec.SILK,
            byteArrayOf()
        )
    }

    @OptIn(MiraiInternalApi::class)
    @MiraiExperimentalApi
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Voice) return false

        if (fileName != other.fileName) return false
        if (!md5.contentEquals(other.md5)) return false
        if (fileSize != other.fileSize) return false
        if (_codec != other._codec) return false
        return _url == other._url
    }

    @OptIn(MiraiInternalApi::class)
    @MiraiExperimentalApi
    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 12 * result + md5.contentHashCode()
        result = 54 * result + fileSize.hashCode()
        result = 33 * result + _codec
        result = 15 * result + _url.hashCode()
        return result
    }

    @MiraiInternalApi
    override fun <D, R> accept(visitor: MessageVisitor<D, R>, data: D): R {
        return visitor.visitVoice(this, data)
    }
}

/**
 * 将 2.7 新增的 [Audio] 转为旧版本的 [Voice], 以兼容某些情况.
 *
 * @since 2.7
 */
@Suppress("DeprecatedCallableAddReplaceWith", "DEPRECATION_ERROR")
@Deprecated(
    "Please migrate to Audio",
    level = DeprecationLevel.ERROR
) // deprecated since 2.7
@JvmSynthetic
@DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
public fun Audio.toVoice(): Voice = Voice.fromAudio(this)