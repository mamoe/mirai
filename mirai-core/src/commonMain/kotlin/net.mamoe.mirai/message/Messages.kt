package net.mamoe.mirai.message

// Message 扩展方法
/**
 * 构造 [PlainText]
 */
fun String.toMessage(): PlainText = PlainText(this)

/**
 * 构造 [MessageChain]
 */
fun Message.toChain(): MessageChain = if (this is MessageChain) this else MessageChain(this)

/**
 * 构造 [MessageChain]
 */
fun List<Message>.toMessageChain(): MessageChain = MessageChain(this)