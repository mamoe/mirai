/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.copy
import net.mamoe.mirai.utils.isSameType
import net.mamoe.mirai.utils.map


/**
 * ## Audio Implementation Overview
 *
 * ```
 *                     (api)Audio
 *                          |
 *                    /------------------\
 *         (api)OnlineAudio        (api)OfflineAudio
 *              |                         |
 *              |                         |
 * (core)OnlineAudioImpl      (core)OfflineAudioImpl
 * ```
 *
 * - [OnlineAudioImpl]: 实现从 [ImMsgBody.Ptt] 解析
 * - [OfflineAudioImpl]: 支持用户手动构造
 *
 * ## Equality
 *
 * - [OnlineAudio] != [OfflineAudio]
 *
 * ## Converting [Audio] to [ImMsgBody.Ptt]
 *
 * Always call [Audio.toPtt]
 */
internal interface AudioPttSupport : MessageContent { // Audio is sealed in mirai-core-api
    /**
     * 原协议数据. 用于在接受到其他用户发送的语音时能按照原样发回.
     */
    val originalPtt: ImMsgBody.Ptt?
}

@Serializable
internal class AudioExtraData(
    @ProtoNumber(1) val ptt: ImMsgBody.Ptt?,
) : ProtoBuf {
    fun toByteArray(): ByteArray {
        return Wrapper(CURRENT_VERSION, this).toByteArray(Wrapper.serializer())
    }

    companion object {
        @Serializable
        class Wrapper(
            @ProtoNumber(1) val version: Int,
            @ProtoNumber(2) val v1: AudioExtraData? = null,
        ) : ProtoBuf

        private const val CURRENT_VERSION = 1


        fun loadFrom(byteArray: ByteArray?): AudioExtraData? {
            byteArray ?: return null
            return kotlin.runCatching {
                byteArray.loadAs(Wrapper.serializer()).v1 // in this version we only support v1
            }.getOrNull()
        }
    }
}

internal fun Audio.toPtt(): ImMsgBody.Ptt {
    if (this is AudioPttSupport) {
        this.originalPtt?.let { return it }
    }
    return ImMsgBody.Ptt(
        fileName = this.filename.toByteArray(),
        fileMd5 = this.fileMd5,
        boolValid = true,
        fileSize = this.fileSize.toInt(),
        fileType = 4,
        pbReserve = byteArrayOf(0),
        format = this.codec.id
    )
}

@SerialName(OnlineAudio.SERIAL_NAME)
@Serializable(OnlineAudioImpl.Serializer::class)
internal class OnlineAudioImpl(
    override val filename: String,
    override val fileMd5: ByteArray,
    override val fileSize: Long,
    override val codec: AudioCodec,
    url: String,
    override val length: Long,
    override val originalPtt: ImMsgBody.Ptt?,
) : OnlineAudio, AudioPttSupport {
    private val _url = refineUrl(url)

    override val extraData: ByteArray? by lazy {
        AudioExtraData(originalPtt).toByteArray()
    }

    override val urlForDownload: String
        get() = _url.takeIf { it.isNotBlank() }
            ?: throw UnsupportedOperationException("Could not fetch URL for audio $filename")

    private val _stringValue: String by lazy { "[mirai:audio:${filename}]" }
    override fun toString(): String = _stringValue

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false

        if (filename != other.filename) return false
        if (!fileMd5.contentEquals(other.fileMd5)) return false
        if (fileSize != other.fileSize) return false
        if (_url != other._url) return false
        if (codec != other.codec) return false
        if (length != other.length) return false
        if (originalPtt != other.originalPtt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + fileMd5.contentHashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + _url.hashCode()
        result = 31 * result + codec.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + originalPtt.hashCode()
        return result
    }


    companion object {
        fun refineUrl(url: String) = when {
            url.isBlank() -> ""
            url.startsWith("http") -> url
            url.startsWith("/") -> "$DOWNLOAD_URL$url"
            else -> "$DOWNLOAD_URL/$url"
        }

        @Suppress("HttpUrlsUsage")
        const val DOWNLOAD_URL = "http://grouptalk.c2c.qq.com"
    }

    object Serializer : KSerializer<OnlineAudioImpl> by Surrogate.serializer().map(
        resultantDescriptor = Surrogate.serializer().descriptor,
        deserialize = {
            OnlineAudioImpl(
                filename = filename,
                fileMd5 = fileMd5,
                fileSize = fileSize,
                url = urlForDownload,
                codec = codec,
                length = length,
                originalPtt = AudioExtraData.loadFrom(extraData)?.ptt
            )
        },
        serialize = {
            Surrogate(
                filename = filename,
                fileMd5 = fileMd5,
                fileSize = fileSize,
                urlForDownload = urlForDownload,
                codec = codec,
                length = length,
                extraData = extraData
            )
        }
    ) {
        @Serializable
        @SerialName(OnlineAudio.SERIAL_NAME)
        private class Surrogate(
            override val filename: String,
            override val fileMd5: ByteArray,
            override val fileSize: Long,
            override val codec: AudioCodec,
            override val length: Long,
            override val extraData: ByteArray?,
            override val urlForDownload: String,
        ) : OnlineAudio {
            override fun toString(): String {
                return "Surrogate(filename='$filename', fileMd5=${fileMd5.contentToString()}, fileSize=$fileSize, codec=$codec, length=$length, extraData=${extraData.contentToString()}, urlForDownload='$urlForDownload')"
            }
        }
    }
}

@SerialName(OfflineAudio.SERIAL_NAME)
@Serializable(OfflineAudioImpl.Serializer::class)
internal class OfflineAudioImpl(
    override val filename: String,
    override val fileMd5: ByteArray,
    override val fileSize: Long,
    override val codec: AudioCodec,
    override val originalPtt: ImMsgBody.Ptt?,
) : OfflineAudio, AudioPttSupport {
    constructor(
        filename: String,
        fileMd5: ByteArray,
        fileSize: Long,
        codec: AudioCodec,
        extraData: ByteArray?,
    ) : this(filename, fileMd5, fileSize, codec, AudioExtraData.loadFrom(extraData)?.ptt)

    override val extraData: ByteArray? by lazy {
        AudioExtraData(originalPtt).toByteArray()
    }

    private val _stringValue: String by lazy { "[mirai:audio:${filename}]" }
    override fun toString(): String = _stringValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (!isSameType(this, other)) return false

        if (filename != other.filename) return false
        if (!fileMd5.contentEquals(other.fileMd5)) return false
        if (fileSize != other.fileSize) return false
        if (codec != other.codec) return false
        if (originalPtt != other.originalPtt) return false
        return true
    }

    override fun hashCode(): Int {
        var result = filename.hashCode()
        result = 31 * result + fileMd5.contentHashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + codec.hashCode()
        result = 31 * result + originalPtt.hashCode()
        return result
    }

    object Serializer : KSerializer<OfflineAudioImpl> by Surrogate.serializer().map(
        resultantDescriptor = Surrogate.serializer().descriptor,
        deserialize = {
            OfflineAudioImpl(
                filename = filename,
                fileMd5 = fileMd5,
                fileSize = fileSize,
                codec = codec,
                extraData = extraData,
            )
        },
        serialize = {
            Surrogate(
                filename = filename,
                fileMd5 = fileMd5,
                fileSize = fileSize,
                codec = codec,
                extraData = extraData,
            )
        }
    ) {
        @Serializable
        @SerialName(OfflineAudio.SERIAL_NAME)
        private class Surrogate(
            override val filename: String,
            override val fileMd5: ByteArray,
            override val fileSize: Long,
            override val codec: AudioCodec,
            override val extraData: ByteArray?,
        ) : OfflineAudio {
            override fun toString(): String {
                return "OfflineAudio(filename='$filename', fileMd5=${fileMd5.contentToString()}, fileSize=$fileSize, codec=$codec, extraData=${extraData.contentToString()})"
            }
        }
    }
}

@PublishedApi
internal class OfflineAudioFactoryImpl : OfflineAudio.Factory {
    override fun create(
        filename: String,
        fileMd5: ByteArray,
        fileSize: Long,
        codec: AudioCodec,
        extraData: ByteArray?
    ): OfflineAudio = OfflineAudioImpl(filename, fileMd5, fileSize, codec, extraData)
}