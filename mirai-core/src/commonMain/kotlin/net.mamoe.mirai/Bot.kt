@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot.ContactSystem
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.internal.Group
import net.mamoe.mirai.contact.internal.QQ
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMBotNetworkHandler
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
import kotlin.coroutines.coroutineContext
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

data class BotAccount(
    val id: UInt,
    val password: String
) {
    constructor(id: Long, password: String) : this(id.toUInt(), password)
}

// These pseudo 'constructors' are suspend, therefore they should not be inside the object

@Suppress("FunctionName")
suspend inline fun Bot(account: BotAccount, logger: MiraiLogger): Bot = Bot(account, logger, coroutineContext)

@Suppress("FunctionName")
suspend inline fun Bot(account: BotAccount): Bot = Bot(account, coroutineContext)

@JvmSynthetic
@Suppress("FunctionName")
suspend inline fun Bot(qq: UInt, password: String): Bot = Bot(BotAccount(qq, password), coroutineContext)

@Suppress("FunctionName")
suspend inline fun Bot(qq: Long, password: String): Bot = Bot(BotAccount(qq.toUInt(), password), coroutineContext)

/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * [Bot] 由 3 个模块组成.
 * [联系人管理][ContactSystem]: 可通过 [Bot.contacts] 访问
 * [网络处理器][TIMBotNetworkHandler]: 可通过 [Bot.network] 访问
 * [机器人账号信息][BotAccount]: 可通过 [Bot.qqAccount] 访问
 *
 * 若需要得到机器人的 QQ 账号, 请访问 [Bot.qqAccount]
 * 若需要得到服务器上所有机器人列表, 请访问 [Bot.instances]
 *
 * 在 BotHelper.kt 中有一些访问的捷径. 如 [Bot.getGroup]
 *
 *
 *
 * Bot that is the base of the whole program.
 * It consists of
 * a [ContactSystem], which manage contacts such as [QQ] and [Group];
 * a [TIMBotNetworkHandler], which manages the connection to the server;
 * a [BotAccount], which stores the account information(e.g. qq number the bot)
 *
 * To of all the QQ contacts, access [Bot.qqAccount]
 * To of all the Robot instance, access [Bot.instances]
 *
 *
 * @author Him188moe
 * @author NaturalHG
 * @see Contact
 */
class Bot(val account: BotAccount, val logger: MiraiLogger, context: CoroutineContext) : CoroutineScope {
    private val supervisorJob = SupervisorJob(context[Job])
    override val coroutineContext: CoroutineContext =
        context + supervisorJob + CoroutineExceptionHandler { _, e -> e.logStacktrace("An exception was thrown under a coroutine of Bot") }

    constructor(qq: Long, password: String, context: CoroutineContext) : this(BotAccount(qq, password), context)
    constructor(qq: UInt, password: String, context: CoroutineContext) : this(BotAccount(qq, password), context)
    constructor(account: BotAccount, context: CoroutineContext) : this(account, DefaultLogger("Bot(" + account.id + ")"), context)

    val contacts = ContactSystem()

    val network: BotNetworkHandler<*> get() = _network

    private lateinit var _network: BotNetworkHandler<*>

    init {
        launch {
            addInstance(this@Bot)
        }
    }

    override fun toString(): String = "Bot(${account.id})"

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler] 下的协程.
     * 然后重新启动并尝试登录
     */
    @JvmOverloads // shouldn't be suspend!! This function MUST NOT inherit the context from the caller because the caller(NetworkHandler) is going to close
    fun tryReinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable? = null
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

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler] 下的协程.
     * 然后重新启动并尝试登录
     */
    @JvmOverloads // shouldn't be suspend!! This function MUST NOT inherit the context from the caller because the caller(NetworkHandler) is going to close
    suspend fun reinitializeNetworkHandler(
        configuration: BotConfiguration,
        cause: Throwable? = null
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
        _network = TIMBotNetworkHandler(this@Bot.coroutineContext + configuration, this@Bot)

        return _network.login()
    }

    /**
     * [关闭][BotNetworkHandler.close]网络处理器, 取消所有运行在 [BotNetworkHandler] 下的协程.
     * 然后重新启动并尝试登录
     */
    @JvmOverloads // shouldn't be suspend!! This function MUST NOT inherit the context from the caller because the caller(NetworkHandler) is going to close
    fun reinitializeNetworkHandlerAsync(
        configuration: BotConfiguration,
        cause: Throwable? = null
    ): Deferred<LoginResult> = async { reinitializeNetworkHandler(configuration, cause) }

    /**
     * Bot 联系人管理.
     *
     * @see Bot.contacts
     */
    inner class ContactSystem internal constructor() {
        val bot: Bot get() = this@Bot

        @UseExperimental(MiraiInternalAPI::class)
        val groups: ContactList<Group> = ContactList(MutableContactList())

        @UseExperimental(MiraiInternalAPI::class)
        val qqs: ContactList<QQ> = ContactList(MutableContactList())

        /**
         * 线程安全地获取缓存的 QQ 对象. 若没有对应的缓存, 则会创建一个.
         */
        @UseExperimental(MiraiInternalAPI::class)
        @JvmSynthetic
        fun getQQ(id: UInt): QQ = qqs.delegate.getOrAdd(id) { QQ(bot, id, coroutineContext) }

        /**
         * 线程安全地获取缓存的 QQ 对象. 若没有对应的缓存, 则会创建一个.
         */
        // NO INLINE!! to help Java
        @UseExperimental(MiraiInternalAPI::class)
        fun getQQ(@PositiveNumbers id: Long): QQ = getQQ(id.coerceAtLeastOrFail(0).toUInt())

        /**
         * 线程安全地获取缓存的群对象. 若没有对应的缓存, 则会创建一个.
         */
        suspend fun getGroup(internalId: GroupInternalId): Group = getGroup(internalId.toId())

        /**
         * 线程安全地获取缓存的群对象. 若没有对应的缓存, 则会创建一个.
         */
        @UseExperimental(MiraiInternalAPI::class, ExperimentalUnsignedTypes::class)
        suspend fun getGroup(id: GroupId): Group = groups.delegate.getOrNull(id.value) ?: inline {
            val info: RawGroupInfo = try {
                when (val response =
                    bot.withSession { GroupPacket.QueryGroupInfo(qqAccount, id.toInternalId(), sessionKey).sendAndExpect<GroupPacket.InfoResponse>() }) {
                    is RawGroupInfo -> response
                    is GroupNotFound -> throw GroupNotFoundException("id=${id.value.toLong()}")
                    else -> assertUnreachable()
                }
            } catch (e: Exception) {
                throw IllegalStateException("Cannot obtain group info for id ${id.value.toLong()}", e)
            }

            return groups.delegate.getOrAdd(id.value) { Group(bot, id, info, coroutineContext) }
        }

        /**
         * 线程安全地获取缓存的群对象. 若没有对应的缓存, 则会创建一个.
         */
        // NO INLINE!! to help Java
        @UseExperimental(MiraiInternalAPI::class)
        suspend fun getGroup(@PositiveNumbers id: Long): Group = id.coerceAtLeastOrFail(0).toUInt().let {
            groups.delegate.getOrNull(it) ?: inline {
                val info: RawGroupInfo = try {
                    bot.withSession { GroupPacket.QueryGroupInfo(qqAccount, GroupId(it).toInternalId(), sessionKey).sendAndExpect() }
                } catch (e: Exception) {
                    e.logStacktrace()
                    error("Cannot obtain group info for id ${it.toLong()}")
                }

                return groups.delegate.getOrAdd(it) { Group(bot, GroupId(it), info, coroutineContext) }
            }
        }
    }

    @UseExperimental(MiraiInternalAPI::class)
    fun close() {
        _network.close()
        this.coroutineContext.cancelChildren()
        contacts.groups.delegate.clear()
        contacts.qqs.delegate.clear()
    }

    companion object {
        @Suppress("ObjectPropertyName")
        private val _instances: MutableList<Bot> = mutableListOf()
        private val instanceLock: Mutex = Mutex()

        private val instances: List<Bot> get() = _instances

        suspend fun instanceWhose(qq: UInt) = instanceLock.withLock {
            instances.first { it.qqAccount == qq }
        }

        internal suspend fun addInstance(bot: Bot) = instanceLock.withLock {
            _instances += bot
        }
    }
}