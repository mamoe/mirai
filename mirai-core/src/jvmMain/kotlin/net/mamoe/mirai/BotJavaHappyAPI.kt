package net.mamoe.mirai

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource

/**
 * [Bot] 中为了让 Java 使用者调用更方便的 API 列表.
 */
@Suppress("FunctionName", "INAPPLICABLE_JVM_NAME", "unused")
actual abstract class BotJavaHappyAPI actual constructor() {
    init {
        @Suppress("LeakingThis")
        check(this is Bot)
    }

    private inline fun <R> runBlocking(crossinline block: suspend Bot.() -> R): R {
        return kotlinx.coroutines.runBlocking { block(this@BotJavaHappyAPI as Bot) }
    }


    @JvmName("login")
    actual open fun __loginBlockingForJava__() {
        runBlocking { login() }
    }

    @JvmName("recall")
    actual open fun __recallBlockingForJava__(source: MessageSource) {
        runBlocking { recall(source) }
    }

    @JvmName("queryImageUrl")
    actual open fun __queryImageUrl__(image: Image): String {
        return runBlocking { queryImageUrl(image) }
    }
}