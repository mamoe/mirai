package net.mamoe.mirai

import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * [Bot] 中为了让 Java 使用者调用更方便的 API 列表.
 */
@MiraiInternalAPI
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
    actual open fun __queryImageUrlBlockingForJava__(image: Image): String {
        return runBlocking { queryImageUrl(image) }
    }

    @JvmName("join")
    actual open fun __joinBlockingForJava__() {
        runBlocking { join() }
    }

    @UseExperimental(MiraiExperimentalAPI::class)
    @JvmOverloads
    @JvmName("addFriend")
    actual open fun __addFriendBlockingForJava__(
        id: Long,
        message: String?,
        remark: String?
    ): AddFriendResult {
        return runBlocking { addFriend(id, message, remark) }
    }
}