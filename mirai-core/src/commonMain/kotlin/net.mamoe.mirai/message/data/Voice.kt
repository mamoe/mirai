package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai

/**
 * 需要通过上传到服务器的消息，如语音、文件
 */
@MiraiExperimentalAPI
public abstract class PttMessage : MessageContent {

    public companion object Key : Message.Key<PttMessage> {
        public override val typeName: String
            get() = "PttMessage"
    }

    public abstract val fileName: String
    public abstract val md5: ByteArray
    public abstract val fileSize: Long
}


/**
 * 语音消息, 目前只支持接收和转发
 */
@SinceMirai("1.2.0")
public class Voice @MiraiInternalAPI constructor(
    public override val fileName: String,
    public override val md5: ByteArray,
    public override val fileSize: Long,
    private val _url: String
) : PttMessage() {

    public companion object Key : Message.Key<Voice> {
        override val typeName: String
            get() = "Voice"
    }

    public val url: String?
        get() =
            if (_url.startsWith("http")) _url
            else null

    private var _stringValue: String? = null
        get() = field ?: kotlin.run {
            field = "[mirai:voice:$fileName]"
            field
        }

    public override fun toString(): String = _stringValue!!

    public override fun contentToString(): String = "[语音]"
}