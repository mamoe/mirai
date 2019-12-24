package net.mamoe.mirai.message.data


inline class PlainText(val stringValue: String) : Message {
    override operator fun contains(sub: String): Boolean = sub in stringValue
    override fun toString(): String = stringValue

    companion object Key : Message.Key<PlainText>
}

/**
 * 构造 [PlainText]
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toMessage(): PlainText = PlainText(this)

/**
 * 得到包含作为 [PlainText] 的 [this] 的 [MessageChain].
 *
 * @return 唯一成员且不可修改的 [SingleMessageChainImpl]
 *
 * @see SingleMessageChain
 * @see SingleMessageChainImpl
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.singleChain(): MessageChain = this.toMessage().chain()