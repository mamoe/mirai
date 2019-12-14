@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.internal.Group
import net.mamoe.mirai.contact.internal.QQ
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.KnownPacketId
import net.mamoe.mirai.network.protocol.tim.packet.action.GroupNotFound
import net.mamoe.mirai.network.protocol.tim.packet.action.GroupPacket
import net.mamoe.mirai.network.protocol.tim.packet.action.RawGroupInfo
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.network.protocol.tim.packet.login.isSuccess
import net.mamoe.mirai.network.qqAccount
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.internal.PositiveNumbers
import net.mamoe.mirai.utils.internal.coerceAtLeastOrFail
import net.mamoe.mirai.utils.io.inline
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

@PublishedApi
internal class BotImpl @PublishedApi internal constructor(
    override val account: BotAccount,
    override val logger: MiraiLogger = DefaultLogger("Bot(" + account.id + ")"),
    context: CoroutineContext
) : Bot, CoroutineScope {
    private val supervisorJob = SupervisorJob(context[Job])
    override val coroutineContext: CoroutineContext =
        context + supervisorJob + CoroutineExceptionHandler { _, e -> e.logStacktrace("An exception was thrown under a coroutine of Bot") }

    init {
        launch {
            instances.addLast(this@BotImpl)
        }
    }

    companion object {
        init {
            KnownPacketId.values() /* load id classes */
        }

        @PublishedApi
        internal val instances: LockFreeLinkedList<Bot> = LockFreeLinkedList()

        inline fun forEachInstance(block: (Bot) -> Unit) = instances.forEach(block)

        fun instanceWhose(qq: UInt): Bot {
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

    override val network: BotNetworkHandler<*> get() = _network
    private lateinit var _network: BotNetworkHandler<*>

    override fun tryReinitializeNetworkHandler(// shouldn't be suspend!! This function MUST NOT inherit the context from the caller because the caller(NetworkHandler) is going to close
        configuration: BotConfiguration,
        cause: Throwable?
    ): Job = launch {
        repeat(configuration.reconnectionRetryTimes) {
            if (reinitializeNetworkHandlerAsync(configuration, cause).await().isSuccess()) {
                logger.info("Reconnected successfully")
                return@launch
            } else {
                delay(configuration.reconnectPeriodMillis)
            }
        }
    }

    override suspend fun reinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable?
    ): LoginResult {
        logger.info("BotAccount: ${qqAccount.toLong()}")
        logger.info("Initializing BotNetworkHandler")
        try {
            if (::_network.isInitialized) {
                _network.close(cause)
            }
        } catch (e: Exception) {
            logger.error("Cannot close network handler", e)
        }
        _network = TIMBotNetworkHandler(this.coroutineContext + configuration, this)

        return _network.login()
    }

    override fun reinitializeNetworkHandlerAsync(
        configuration: BotConfiguration,
        cause: Throwable?
    ): Deferred<LoginResult> = async { reinitializeNetworkHandler(configuration, cause) }

    // endregion

    // region contacts
    override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())

    override val qqs: ContactList<QQ> = ContactList(LockFreeLinkedList())

    /**
     * 线程安全地获取缓存的 QQ 对象. 若没有对应的缓存, 则会创建一个.
     */
    @UseExperimental(MiraiInternalAPI::class)
    @JvmSynthetic
    override fun getQQ(id: UInt): QQ = qqs.delegate.getOrAdd(id) { QQ(this, id, coroutineContext) }

    // NO INLINE!! to help Java
    @UseExperimental(MiraiInternalAPI::class)
    override fun getQQ(@PositiveNumbers id: Long): QQ = getQQ(id.coerceAtLeastOrFail(0).toUInt())

    override suspend fun getGroup(internalId: GroupInternalId): Group = getGroup(internalId.toId())

    @UseExperimental(MiraiInternalAPI::class, ExperimentalUnsignedTypes::class)
    override suspend fun getGroup(id: GroupId): Group = groups.delegate.getOrNull(id.value) ?: inline {
        val info: RawGroupInfo = try {
            when (val response =
                withSession { GroupPacket.QueryGroupInfo(qqAccount, id.toInternalId(), sessionKey).sendAndExpect<GroupPacket.InfoResponse>() }) {
                is RawGroupInfo -> response
                is GroupNotFound -> throw GroupNotFoundException("id=${id.value.toLong()}")
                else -> assertUnreachable()
            }
        } catch (e: Exception) {
            throw IllegalStateException("Cannot obtain group info for id ${id.value.toLong()}", e)
        }

        return groups.delegate.getOrAdd(id.value) { Group(this, id, info, coroutineContext) }
    }

    // NO INLINE!! to help Java
    @UseExperimental(MiraiInternalAPI::class)
    override suspend fun getGroup(@PositiveNumbers id: Long): Group = id.coerceAtLeastOrFail(0).toUInt().let {
        groups.delegate.getOrNull(it) ?: inline {
            val info: RawGroupInfo = try {
                withSession { GroupPacket.QueryGroupInfo(qqAccount, GroupId(it).toInternalId(), sessionKey).sendAndExpect() }
            } catch (e: Exception) {
                e.logStacktrace()
                error("Cannot obtain group info for id ${it.toLong()}")
            }

            return groups.delegate.getOrAdd(it) { Group(this, GroupId(it), info, coroutineContext) }
        }
    }
    // endregion

    @UseExperimental(MiraiInternalAPI::class)
    override fun close() {
        _network.close()
        this.supervisorJob.complete()
        groups.delegate.clear()
        qqs.delegate.clear()
    }
}
