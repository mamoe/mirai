package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * [Bot] 中为了让 Java 使用者调用更方便的 API 列表.
 */
@MiraiInternalAPI
@Suppress("FunctionName", "INAPPLICABLE_JVM_NAME", "unused")
actual abstract class BotJavaHappyAPI actual constructor() {
    init {
        @Suppress("LeakingThis")
        assert(this is Bot)
    }

    private inline fun <R> runBlocking(crossinline block: suspend Bot.() -> R): R {
        return kotlinx.coroutines.runBlocking { block(this@BotJavaHappyAPI as Bot) }
    }

    private inline fun <R> future(crossinline block: suspend Bot.() -> R): Future<R> {
        return (this as Bot).run { future(block) }
    }


    @JvmName("login")
    fun __loginBlockingForJava__() {
        runBlocking { login() }
    }

    @JvmName("recall")
    fun __recallBlockingForJava__(source: MessageSource) {
        runBlocking { recall(source) }
    }

    @JvmName("recall")
    fun __recallBlockingForJava__(source: MessageChain) {
        runBlocking { recall(source) }
    }

    @JvmName("recallIn")
    fun __recallIn_MemberForJava__(source: MessageSource, millis: Long) {
        runBlocking { recallIn(source, millis) }
    }

    @JvmName("recallIn")
    fun __recallIn_MemberForJava__(source: MessageChain, millis: Long) {
        runBlocking { recallIn(source, millis) }
    }

    @JvmName("queryImageUrl")
    fun __queryImageUrlBlockingForJava__(image: Image): String {
        return runBlocking { queryImageUrl(image) }
    }

    @JvmName("join")
    fun __joinBlockingForJava__() {
        runBlocking { join() }
    }

    @JvmOverloads
    @JvmName("addFriend")
    fun __addFriendBlockingForJava__(
        id: Long,
        message: String? = null,
        remark: String? = null
    ): AddFriendResult {
        @UseExperimental(MiraiExperimentalAPI::class)
        return runBlocking { addFriend(id, message, remark) }
    }

    @JvmName("loginAsync")
    fun __loginAsyncForJava__(): Future<Unit> {
        return future { login() }
    }

    @JvmName("recallAsync")
    fun __recallAsyncForJava__(source: MessageSource): Future<Unit> {
        return future { recall(source) }
    }

    @JvmName("recallAsync")
    fun __recallAsyncForJava__(source: MessageChain): Future<Unit> {
        return future { recall(source) }
    }

    @JvmName("queryImageUrlAsync")
    fun __queryImageUrlAsyncForJava__(image: Image): Future<String> {
        return future { queryImageUrl(image) }
    }
}

private inline fun <R> Bot.future(crossinline block: suspend Bot.() -> R): Future<R> {
    return object : Future<R> {
        val value: CompletableDeferred<R> = CompletableDeferred()

        init {
            launch {
                @UseExperimental(ExperimentalCoroutinesApi::class)
                value.completeWith(kotlin.runCatching { block() })
            }
        }

        override fun isDone(): Boolean {
            return value.isCompleted
        }

        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        override fun get(): R {
            if (value.isCompleted) {
                @UseExperimental(ExperimentalCoroutinesApi::class)
                return value.getCompleted()
            }
            return runBlocking { value.await() }
        }

        override fun get(timeout: Long, unit: TimeUnit): R {
            if (value.isCompleted) {
                @UseExperimental(ExperimentalCoroutinesApi::class)
                return value.getCompleted()
            }
            return runBlocking {
                withTimeoutOrNull(TimeUnit.MILLISECONDS.convert(timeout, unit)) { value.await() }
                    ?: throw TimeoutException()
            }
        }

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            if (value.isCompleted || value.isCancelled) {
                return false
            }

            return if (mayInterruptIfRunning && value.isActive) {
                value.cancel()
                true
            } else {
                false
            }
        }

        override fun isCancelled(): Boolean {
            return value.isCancelled
        }
    }
}