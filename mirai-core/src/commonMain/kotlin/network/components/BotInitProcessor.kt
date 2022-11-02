/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.network.handler.NetworkHandler.State
import net.mamoe.mirai.internal.network.handler.selector.NetworkException
import net.mamoe.mirai.internal.network.handler.state.JobAttachStateObserver
import net.mamoe.mirai.internal.network.handler.state.StateObserver
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPushForceOffline
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.Symbol


/**
 * Handles initialization jobs after successful logon.
 *
 * The initialization includes:
 * - Downloading contact list, which might read from local cache
 * - Synchronizing message sequence id
 * - Synchronizing BDH session for resource uploading
 *
 * Calls [ContactUpdater], [OtherClientUpdater], [ConfigPushSyncer], ... (see [BotInitProcessorImpl])
 *
 * Attached to handler state [NetworkHandler.State.LOADING] [as state observer][asObserver] in [QQAndroidBot.stateObserverChain].
 */
internal interface BotInitProcessor {
    /**
     * Do initialization. Implementor must ensure initialization runs exactly single time.
     */
    suspend fun init()

    /**
     * Called when login was potentially halted, meaning the data might not have been loaded,
     * so we need to set the flag that helps keep single-initialization to UNINITIALIZED.
     *
     * This is called in [MessageSvcPushForceOffline], which is in case connection is closed by server during the [NetworkHandler.State.LOADING] state.
     *
     * This function only marks current initialization work has failed. It has nothing to do with result of login.
     * To update that result, update `bot.components[SsoProcessor].firstLoginResult`.
     *
     * See [BotInitProcessorImpl.state].
     */
    fun setLoginHalted()

    companion object : ComponentKey<BotInitProcessor>
}

internal fun BotInitProcessor.asObserver(targetState: State = State.LOADING): StateObserver {
    return JobAttachStateObserver("BotInitProcessor.init", targetState) { init() }
}


internal class BotInitProcessorImpl(
    private val bot: QQAndroidBot,
    private val context: ComponentStorage,
    private val logger: MiraiLogger,
) : BotInitProcessor {
    companion object {
        private val UNINITIALIZED = Symbol("UNINITIALIZED")
        private val INITIALIZING = Symbol("INITIALIZING")
        private val INITIALIZED = Symbol("INITIALIZED")
    }

    private val state = atomic(UNINITIALIZED)

    override fun setLoginHalted() {
        state.compareAndSet(expect = INITIALIZING, update = UNINITIALIZED)
    }

    override suspend fun init() {
        if (!state.compareAndSet(expect = UNINITIALIZED, update = INITIALIZING)) return

        try {
            check(bot.isActive) { "bot is dead therefore network can't init." }
            context[ContactUpdater].closeAllContacts(CancellationException("re-init"))

            val registerResp =
                context[SsoProcessor].registerResp ?: error("Internal error: registerResp is not yet available.")

            context[MessageSvcSyncer].startSync()
            context[BdhSessionSyncer].loadFromCache()

            // do them parallel.
            coroutineScope {
                launch { runWithCoverage("loading OtherClients") { context[OtherClientUpdater].update() } }
                launch { runWithCoverage("loading friends") { context[ContactUpdater].reloadFriendList(registerResp.origin) } }
                launch { runWithCoverage("loading groups") { context[ContactUpdater].reloadGroupList() } }
                launch { runWithCoverage("loading otherClients") { context[ContactUpdater].reloadStrangerList() } }
                launch { runWithCoverage("loading friendGroups") { context[ContactUpdater].reloadFriendGroupList() } }
            }

            state.value = INITIALIZED
            bot.components[SsoProcessor].casFirstLoginResult(null, FirstLoginResult.PASSED)
        } catch (e: Throwable) {
            setLoginHalted()
            bot.components[SsoProcessor].casFirstLoginResult(null, FirstLoginResult.OTHER_FAILURE)
            throw e
        }
    }

    private inline fun runWithCoverage(hint: String, block: () -> Unit) {
        try {
            block()
        } catch (e: NetworkException) {
            logger.warning(
                "An NetworkException was thrown during '$hint' of Bot ${bot.id}. " +
                        "This means your network is unstable at this moment, " +
                        "or the server has closed the connection due to some reason (you will see the cause if further trials are all failed). " +
                        "Halting the log-in process to wait for a while to reconnect..."
            )
            throw e
        } catch (e: Throwable) {
            logger.warning(
                "An exception was thrown during '$hint' of Bot ${bot.id}. " +
                        "Trying to ignore the error and continue logging in...",
                e
            )
        }
    }

}

