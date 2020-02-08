package net.mamoe.mirai.message.data


/**
 * 群内的引用回复. 它将由协议模块实现为 `QuoteReplyImpl`
 */
interface QuoteReply : Message {
    val source: MessageSource

    companion object Key : Message.Key<QuoteReply>
}