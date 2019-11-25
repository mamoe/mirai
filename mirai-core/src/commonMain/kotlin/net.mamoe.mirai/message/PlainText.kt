package net.mamoe.mirai.message


inline class PlainText(override val stringValue: String) : Message {
    override operator fun contains(sub: String): Boolean = sub in stringValue
    override fun toString(): String = stringValue

    companion object Key : Message.Key<PlainText>
}

/**
 * 构造 [PlainText]
 */
fun String.toMessage(): PlainText = PlainText(this)

/**
 * 得到包含作为 [PlainText] 的 [this] 的 [MessageChain].
 *
 * @return 唯一成员且不可修改的 [SingleMessageChainImpl]
 *
 * @see SingleMessageChain
 * @see SingleMessageChainImpl
 */
fun String.singleChain(): MessageChain = this.toMessage().chain()