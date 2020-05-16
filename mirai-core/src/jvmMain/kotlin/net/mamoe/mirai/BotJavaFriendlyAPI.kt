package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.recall
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.queryUrl
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * [Bot] 中为了让 Java 使用者调用更方便的 API 列表.
 */
@Suppress("FunctionName", "INAPPLICABLE_JVM_NAME", "unused")
internal actual interface BotJavaFriendlyAPI {

    /**
     * 登录, 或重新登录.
     * 这个函数总是关闭一切现有网路任务, 然后重新登录并重新缓存好友列表和群列表.
     *
     * 一般情况下不需要重新登录. Mirai 能够自动处理掉线情况.
     *
     * 最终调用 [net.mamoe.mirai.network.BotNetworkHandler.closeEverythingAndRelogin]
     *
     * @throws LoginFailedException
     */
    @Throws(LoginFailedException::class)
    @JvmName("login")
    fun __loginBlockingForJava__() {
        runBlocking { login() }
    }

    /**
     * 撤回这条消息. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
     *
     * [Bot] 撤回自己的消息不需要权限.
     * [Bot] 撤回群员的消息需要管理员权限.
     *
     * @param source 消息源. 可从 [MessageReceipt.source] 获得, 或从消息事件中的 [MessageChain] 获得.
     *
     * @throws PermissionDeniedException 当 [Bot] 无权限操作时
     *
     * @see Bot.recall (扩展函数) 接受参数 [MessageChain]
     */
    @JvmName("recall")
    fun __recallBlockingForJava__(source: MessageSource) {
        runBlocking { recall(source) }
    }

    /**
     * 撤回这条消息.
     * 根据 [message] 内的 [MessageSource] 进行相关判断.
     *
     * [Bot] 撤回自己的消息不需要权限.
     * [Bot] 撤回群员的消息需要管理员权限.
     *
     * @throws PermissionDeniedException 当 [Bot] 无权限操作时
     * @see Bot.recall
     */
    @JvmName("recall")
    fun __recallBlockingForJava__(message: MessageChain) {
        runBlocking { recall(message) }
    }

    /**
     * 在一段时间后撤回这条消息.
     * 将根据 [MessageSource.groupId] 判断消息是群消息还是好友消息.
     *
     * @param millis 延迟的时间, 单位为毫秒
     * @see recall
     */
    @JvmName("recallIn")
    fun __recallIn_MemberForJava__(source: MessageSource, millis: Long) {
        runBlocking { recallIn(source, millis) }
    }

    /**
     * 在一段时间后撤回这条消息.
     *
     * @param millis 延迟的时间, 单位为毫秒
     * @see recall
     */
    @JvmName("recallIn")
    fun __recallIn_MemberForJava__(source: MessageChain, millis: Long) {
        runBlocking { recallIn(source, millis) }
    }

    /**
     * 获取图片下载链接
     */
    @JvmName("queryImageUrl")
    fun __queryImageUrlBlockingForJava__(image: Image): String {
        return runBlocking { image.queryUrl() }
    }

    /**
     * 阻塞当前线程直到 [Bot] 下线.
     */
    @JvmName("join")
    fun __joinBlockingForJava__() {
        runBlocking { join() }
    }

    /**
     * 异步调用 [__loginBlockingForJava__]
     */
    @JvmName("loginAsync")
    fun __loginAsyncForJava__(): Future<Unit> {
        return future { login() }
    }

    /**
     * 异步调用 [__recallBlockingForJava__]
     */
    @JvmName("recallAsync")
    fun __recallAsyncForJava__(source: MessageSource): Future<Unit> {
        return future { recall(source) }
    }

    /**
     * 异步调用 [__recallBlockingForJava__]
     */
    @JvmName("recallAsync")
    fun __recallAsyncForJava__(source: MessageChain): Future<Unit> {
        return future { recall(source) }
    }

    /**
     * 异步调用 [__queryImageUrlBlockingForJava__]
     */
    @JvmName("queryImageUrlAsync")
    fun __queryImageUrlAsyncForJava__(image: Image): Future<String> {
        return future { image.queryUrl() }
    }
}

private inline fun <R> BotJavaFriendlyAPI.future(crossinline block: suspend Bot.() -> R): Future<R> {
    return (this as CoroutineScope).run { future { block(this as Bot) } }
}

private inline fun <R> BotJavaFriendlyAPI.runBlocking(crossinline block: suspend Bot.() -> R): R {
    return kotlinx.coroutines.runBlocking { block(this@runBlocking as Bot) }
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <R, C : CoroutineScope> C.future(block: suspend C.() -> R): Future<R> {
    val future = object : Future<R> {
        val value: CompletableDeferred<R> = CompletableDeferred()

        override fun isDone(): Boolean {
            return value.isCompleted
        }

        override fun get(): R {
            if (value.isCompleted) {
                return value.getCompleted()
            }
            return runBlocking { value.await() }
        }

        override fun get(timeout: Long, unit: TimeUnit): R {
            if (value.isCompleted) {
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

    launch {
        @OptIn(ExperimentalCoroutinesApi::class)
        future.value.completeWith(kotlin.runCatching { block() })
    }

    return future
}