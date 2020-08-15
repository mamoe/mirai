/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * 需要通过上传到服务器的消息，如语音、文件
 */
@MiraiExperimentalAPI
abstract class PttMessage : MessageContent {

    companion object Key : Message.Key<PttMessage> {
        override val typeName: String
            get() = "PttMessage"
    }

    abstract val fileName: String
    abstract val md5: ByteArray
}


/**
 * 语音消息, 目前只支持接收和转发
 */
@MiraiExperimentalAPI
class Voice(
    override val fileName: String,
    override val md5: ByteArray,
    private val _url: String
) : PttMessage() {

    companion object Key : Message.Key<Voice> {
        override val typeName: String
            get() = "Voice"
    }

    val url: String
        get() = if (_url.startsWith("http")) _url
        else "http://grouptalk.c2c.qq.com$_url"

    private var _stringValue: String? = null
        get() = field ?: kotlin.run {
            field = "[mirai:voice:$fileName]"
            field
        }

    override fun toString(): String = _stringValue!!

    override fun contentToString(): String = "[语音]"
}