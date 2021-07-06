/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsVoice


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
        set(value) {
            field = value
            _pttInternalInstanceSerializeCache = null
        }

    @MiraiInternalApi
    protected val pttInternalInstanceSerializeCache: String
        get() {
            _pttInternalInstanceSerializeCache?.let { return it }
            return Mirai.serializePttElem(pttInternalInstance).also {
                _pttInternalInstanceSerializeCache = it
            }
        }

    @Transient
    private var _pttInternalInstanceSerializeCache: String? = null
}

/**
 * 语音消息, 目前只支持接收和转发
 *
 * 目前, 使用 [Voice] 类型是稳定的, 但调用 [Voice] 中的属性 [fileName], [md5], [fileSize] 是不稳定的. 语音的序列化也可能会在未来有变动.
 *
 * ## 使用语音
 *
 * 可以通过 [ExternalResource.uploadAsVoice] 或者 [Group.uploadVoice] 上传语音文件到服务器, 得到 [Voice] 实例. 但这不会发送给目标群.
 * 上传后需要通过 [Group.sendMessage] 发送 [Voice] 实例.
 *
 * [Voice] 实例可以通过序列化方式保存. 下次可以用它发送因而不需要上传. 但可能由于未来服务器更新, 这项功能就不稳定. 因此建议总是上传音频文件而不要保存 [Voice].
 */
@Suppress("DuplicatedCode")
@Serializable(Voice.Serializer::class) // experimental
@SerialName(Voice.SERIAL_NAME)
public class Voice @MiraiInternalApi constructor(
    @MiraiExperimentalApi public override val fileName: String,
    @MiraiExperimentalApi public override val md5: ByteArray,
    @MiraiExperimentalApi public override val fileSize: Long,

    @MiraiInternalApi public val codec: Int = 0,
    private val _url: String
) : PttMessage() {

    public companion object Key : AbstractPolymorphicMessageKey<PttMessage, Voice>(PttMessage, { it.safeCast() }) {
        public const val SERIAL_NAME: String = "Voice"
    }

    /**
     * 下载链接 HTTP URL.
     */
    public val url: String?
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Voice) return false

        if (this.pttInternalInstance != null && other.pttInternalInstance != null) {
            if (this.pttInternalInstance == other.pttInternalInstance)
                return true
            // strict
            return this.pttInternalInstanceSerializeCache == other.pttInternalInstanceSerializeCache
        }

        if (fileName != other.fileName) return false
        if (!md5.contentEquals(other.md5)) return false
        if (fileSize != other.fileSize) return false
        if (codec != other.codec) return false
        if (_url != other._url) return false

        return true
    }

    override fun hashCode(): Int {
        if (pttInternalInstance != null)
            return pttInternalInstanceSerializeCache.hashCode()

        var result = fileName.hashCode()
        result = 12 * result + md5.contentHashCode()
        result = 54 * result + fileSize.hashCode()
        result = 33 * result + codec
        result = 15 * result + _url.hashCode()
        return result
    }

    public object Serializer : KSerializer<Voice> by VoiceS.serializer().map(
        resultantDescriptor = VoiceS.serializer().descriptor.copy(SERIAL_NAME),
        deserialize = {
            Voice(
                fileName = it.fileName,
                md5 = it.md5,
                fileSize = it.fileSize,
                codec = it.codec,
                _url = it._url,
            ).also { v -> v.pttInternalInstance = Mirai.deserializePttElem(it.ptt) }
        },
        serialize = {
            VoiceS(
                fileName = it.fileName,
                md5 = it.md5,
                fileSize = it.fileSize,
                _url = it._url,
                codec = it.codec,
                ptt = Mirai.serializePttElem(it.pttInternalInstance)
            )
        }
    ) {
        @Serializable
        @SerialName(SERIAL_NAME)
        private class VoiceS(
            val fileName: String,
            val md5: ByteArray,
            val fileSize: Long,
            val codec: Int,
            val _url: String,
            val ptt: String = "",
        )
    }
}