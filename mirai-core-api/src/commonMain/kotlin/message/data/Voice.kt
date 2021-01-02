/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.safeCast


/**
 * 需要通过上传到服务器的消息，如语音、文件
 */
@Serializable
@MiraiExperimentalApi
public abstract class PttMessage : MessageContent {

    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, PttMessage>(MessageContent, { it.safeCast() })

    @MiraiExperimentalApi
    public abstract val fileName: String

    @MiraiExperimentalApi
    public abstract val md5: ByteArray

    @MiraiExperimentalApi
    public abstract val fileSize: Long
}


/**
 * 语音消息, 目前只支持接收和转发
 */
@Serializable // experimental
public class Voice @MiraiInternalApi constructor(
    @MiraiExperimentalApi public override val fileName: String,
    @MiraiExperimentalApi public override val md5: ByteArray,
    @MiraiExperimentalApi public override val fileSize: Long,

    @MiraiInternalApi public val codec: Int = 0,
    private val _url: String
) : PttMessage() {

    public companion object Key : AbstractPolymorphicMessageKey<PttMessage, Voice>(PttMessage, { it.safeCast() })

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

    public override fun contentToString(): String = "[语音]"
}