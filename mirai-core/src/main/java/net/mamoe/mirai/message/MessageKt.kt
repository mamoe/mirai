@file:JvmName("MessageKt")

package net.mamoe.mirai.message

import net.mamoe.mirai.message.defaults.PlainText

/**
 * 实现使用 '+' 操作符连接 [Message] 与 [Message]
 */
infix operator fun Message.plus(another: Message): Message = this.concat(another)

/**
 * 实现使用 '+' 操作符连接 [Message] 与 [String]
 */
infix operator fun Message.plus(another: String): Message = this.concat(another)

/**
 * 连接 [String] 与 [Message]
 */
infix fun String.concat(another: Message): Message = PlainText(this).concat(another)