package net.mamoe.mirai.message

// Message 扩展方法
/**
 * 构造 [PlainText]
 */
fun String.toMessage(): PlainText = PlainText(this)

/**
 * 用 `this` 构造 [MessageChain]
 */
fun Message.toChain(): MessageChain = if (this is MessageChain) this else MessageChain(this)

fun MessageChain.containsType(clazz: Class<out Message>): Boolean = list.any { clazz.isInstance(it) }

operator fun MessageChain.contains(sub: Class<out Message>): Boolean = containsType(sub)