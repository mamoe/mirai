package net.mamoe.mirai.message.data

/**
 * 消息源, 用于被引用. 它将由协议模块实现为 `MessageSourceImpl`
 */
interface MessageSource : Message {
    companion object : Message.Key<MessageSource>
}