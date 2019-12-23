@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.LockFreeLinkedList
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.coroutines.CoroutineContext

/*
 * 泛型 N 不需要向外(接口)暴露.
 */
@MiraiInternalAPI
abstract class BotImpl<N : BotNetworkHandler> constructor(
    account: BotAccount,
    logger: MiraiLogger?,
    context: CoroutineContext
) : Bot(), CoroutineScope {
    private val supervisorJob = SupervisorJob(context[Job])
    override val coroutineContext: CoroutineContext =
        context + supervisorJob + CoroutineExceptionHandler { _, e -> e.logStacktrace("An exception was thrown under a coroutine of Bot") }

    @Suppress("CanBePrimaryConstructorProperty") // for logger
    override val account: BotAccount = account
    override val logger: MiraiLogger = logger ?: DefaultLogger("Bot(" + account.id + ")")

    init {
        @Suppress("LeakingThis")
        instances.addLast(this)
    }

    companion object {
        @PublishedApi
        internal val instances: LockFreeLinkedList<Bot> = LockFreeLinkedList()

        inline fun forEachInstance(block: (Bot) -> Unit) = instances.forEach(block)

        fun instanceWhose(qq: Long): Bot {
            instances.forEach {
                if (it.qqAccount == qq) {
                    return it
                }
            }
            throw NoSuchElementException()
        }
    }

    override fun toString(): String = "Bot(${account.id})"

    // region network

    abstract override val network: N
    // endregion

    @UseExperimental(MiraiInternalAPI::class)
    override fun close(throwable: Throwable?) {
        if (throwable == null) {
            network.close()
            this.supervisorJob.complete()
            groups.delegate.clear()
            qqs.delegate.clear()
        } else {
            network.close(throwable)
            this.supervisorJob.completeExceptionally(throwable)
            groups.delegate.clear()
            qqs.delegate.clear()
        }
    }
}
