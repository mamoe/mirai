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