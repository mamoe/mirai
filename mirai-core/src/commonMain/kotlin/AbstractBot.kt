/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.internal

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.internal.contact.info.FriendInfoImpl
import net.mamoe.mirai.internal.contact.info.StrangerInfoImpl
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.components.SsoProcessor
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.impl.netty.asCoroutineExceptionHandler
import net.mamoe.mirai.supervisorJob
import net.mamoe.mirai.utils.*
import kotlin.collections.set
import kotlin.coroutines.CoroutineContext

/**
 * Protocol-irrelevant implementations
 */
internal abstract class AbstractBot constructor(
    final override val configuration: BotConfiguration,
    final override val id: Long,
) : Bot, CoroutineScope {
    ///////////////////////////////////////////////////////////////////////////
    // lifecycle
    ///////////////////////////////////////////////////////////////////////////

    // FASTEST INIT
    @Suppress("LeakingThis")
    final override val logger: MiraiLogger = configuration.botLoggerSupplier(this)

    final override val coroutineContext: CoroutineContext =
        CoroutineName("Bot.$id")
            .plus(logger.asCoroutineExceptionHandler())
            .childScopeContext(configuration.parentCoroutineContext)
            .apply {
                job.invokeOnCompletion { throwable ->
                    logger.info { "Bot cancelled" + throwable?.message?.let { ": $it" }.orEmpty() }

                    kotlin.runCatching {
                        network.close(throwable)
                    }.onFailure {
                        if (it !is CancellationException) logger.error(it)
                    }

                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    Bot._instances.remove(id)

                    // help GC release instances
                    groups.forEach { it.members.delegate.clear() }
                    groups.delegate.clear() // job is cancelled, so child jobs are to be cancelled
                    friends.delegate.clear()
                    strangers.delegate.clear()
                }
            }

    init {
        @Suppress("LeakingThis", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
        Bot._instances[this.id] = this
    }

    ///////////////////////////////////////////////////////////////////////////
    // overrides
    ///////////////////////////////////////////////////////////////////////////

    abstract val components: ComponentStorage

    final override val isOnline: Boolean get() = network.isOk()
    final override val eventChannel: EventChannel<BotEvent> =
        GlobalEventChannel.filterIsInstance<BotEvent>().filter { it.bot === this@AbstractBot }

    final override val otherClients: ContactList<OtherClient> = ContactList()
    final override val friends: ContactList<Friend> = ContactList()
    final override val groups: ContactList<Group> = ContactList()
    final override val strangers: ContactList<Stranger> = ContactList()

    final override val asFriend: Friend by lazy { Mirai.newFriend(this, FriendInfoImpl(uin, nick, "")) }
    final override val asStranger: Stranger by lazy { Mirai.newStranger(this, StrangerInfoImpl(bot.id, bot.nick)) }

    override fun close(cause: Throwable?) {
        if (!this.isActive) return

        if (cause == null) {
            supervisorJob.cancel()
        } else {
            supervisorJob.cancel(CancellationException("Bot closed", cause))
        }
    }

    final override fun toString(): String = "Bot($id)"

    ///////////////////////////////////////////////////////////////////////////
    // network
    ///////////////////////////////////////////////////////////////////////////

    val network: NetworkHandler by lazy { createNetworkHandler() } // the selector handles renewal of [NetworkHandler]

    final override suspend fun login() {
        if (!isActive) error("Bot is already closed and cannot relogin. Please create a new Bot instance then do login.")
        try {
            network.resumeConnection()
        } catch (e: Throwable) { // failed to init
            val cause = e.unwrap<NetworkException>()
            if (!components[SsoProcessor].firstLoginSucceed) {
                this.close(cause) // failed to do first login.
            }
            throw cause
        }
        logger.info { "Bot login successful." }
    }

    protected abstract fun createNetworkHandler(): NetworkHandler
}