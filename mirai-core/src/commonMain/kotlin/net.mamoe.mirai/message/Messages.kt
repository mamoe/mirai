package net.mamoe.mirai.message

// Message 扩展方法
/**
 * 构造 [PlainText]
 */
fun String.toMessage(): PlainText = PlainText(this)

/**
 * 得到包含 [this] 的 [MessageChain].
 * 若 [this] 为 [MessageChain] 将直接返回 this
 * 否则将调用 [SingleMessageChain] 构造一个唯一成员不可修改的 [SingleMessageChainImpl]
 *
 * @see SingleMessageChain
 * @see SingleMessageChainImpl
 */
fun Message.toChain(): MessageChain = if (this is MessageChain) this else SingleMessageChain(this)

/**
 * 以 [this] 为代表 (delegate) 构造 [SingleMessageChain].
 * @throws IllegalArgumentException 当 [this] 为 [MessageChain] 的实例时
 */
fun Message.singleChain(): MessageChain = SingleMessageChain(this)

/**
 * 构造 [MessageChain]
 */
fun List<Message>.toMessageChain(): MessageChain = MessageChain(this)