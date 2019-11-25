package net.mamoe.mirai.message

/**
 * QQ 自带表情
 */
inline class Face(val id: FaceId) : Message {
    override val stringValue: String get() = "[face${id.value}]"
    override fun toString(): String = stringValue

    companion object Key : Message.Key<Face>
}