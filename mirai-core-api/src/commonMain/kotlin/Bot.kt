/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
@file:Suppress(
    "EXPERIMENTAL_API_USAGE", "unused", "FunctionName", "NOTHING_TO_INLINE", "UnusedImport",
    "EXPERIMENTAL_OVERRIDE", "CanBeParameter", "MemberVisibilityCanBePrivate", "INAPPLICABLE_JVM_NAME",
    "EXPOSED_SUPER_CLASS"
)

package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.action.BotNudge
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext

/**
 * 登录, 返回 [this]
 */
@JvmSynthetic
public suspend inline fun <B : Bot> B.alsoLogin(): B = also { login() }

/**
 * 机器人对象. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * 有关 [Bot] 生命管理, 请查看 [BotConfiguration.inheritCoroutineContext]
 *
 * @see Contact 联系人
 * @see isActive 判断 [Bot] 是否正常运行中. (协程正常运行) (但不能判断是否在线, 需使用 [isOnline])
 *
 * @see BotFactory 构造 [Bot] 的工厂, [Bot] 唯一的构造方式.
 */
public abstract class Bot internal constructor(
    public val configuration: BotConfiguration
) : CoroutineScope, ContactOrBot {
    public final override val coroutineContext: CoroutineContext = // for id
        configuration.parentCoroutineContext
            .plus(SupervisorJob(configuration.parentCoroutineContext[Job]))
            .plus(configuration.parentCoroutineContext[CoroutineExceptionHandler]
                ?: CoroutineExceptionHandler { _, e ->
                    logger.error("An exception was thrown under a coroutine of Bot", e)
                }
            )
            .plus(CoroutineName("Mirai Bot"))


    public companion object {
        @JvmField
        @Suppress("ObjectPropertyName")
        internal val _instances: LockFreeLinkedList<WeakRef<Bot>> = LockFreeLinkedList()

        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @JvmStatic
        public val botInstances: List<Bot>
            get() = _instances.asSequence().mapNotNull { it.get() }.toList()

        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @JvmStatic
        public val botInstancesSequence: Sequence<Bot>
            get() = _instances.asSequence().mapNotNull { it.get() }

        /**
         * 遍历每一个 [Bot] 实例
         */
        @JvmSynthetic
        public fun forEachInstance(block: (Bot) -> Unit): Unit = _instances.forEach { it.get()?.let(block) }

        /**
         * 获取一个 [Bot] 实例, 无对应实例时抛出 [NoSuchElementException]
         */
        @JvmStatic
        @Throws(NoSuchElementException::class)
        public fun getInstance(qq: Long): Bot =
            getInstanceOrNull(qq) ?: throw NoSuchElementException(qq.toString())

        /**
         * 获取一个 [Bot] 实例, 无对应实例时返回 `null`
         */
        @JvmStatic
        public fun getInstanceOrNull(qq: Long): Bot? =
            _instances.asSequence().mapNotNull { it.get() }.firstOrNull { it.id == qq }
    }

    init {
        _instances.addLast(this.weakRef())
        supervisorJob.invokeOnCompletion {
            _instances.removeIf { it.get()?.id == this.id }
        }
    }

    /**
     * QQ 号码. 实际类型为 uint
     */
    public abstract override val id: Long

    /**
     * 昵称
     */
    public abstract val nick: String

    /**
     * 日志记录器
     */
    public abstract val logger: MiraiLogger

    /**
     * 判断 Bot 是否在线 (可正常收发消息)
     */
    public abstract val isOnline: Boolean

    // region contacts

    /**
     * [User.id] 与 [Bot.id] 相同的 [Friend] 实例
     */
    @MiraiExperimentalApi
    public abstract val asFriend: Friend


    /**
     * 机器人的好友列表. 与服务器同步更新
     */
    public abstract val friends: ContactList<Friend>

    /**
     * 获取一个好友对象.
     * @throws [NoSuchElementException] 当不存在这个好友时抛出
     */
    public fun getFriend(id: Long): Friend =
        friends.firstOrNull { it.id == id } ?: throw NoSuchElementException("friend $id")

    /**
     * 机器人加入的群列表. 与服务器同步更新
     */
    public abstract val groups: ContactList<Group>

    /**
     * 获取一个机器人加入的群.
     * @throws NoSuchElementException 当不存在这个群时抛出
     */
    public fun getGroup(id: Long): Group =
        groups.firstOrNull { it.id == id } ?: throw NoSuchElementException("group $id")

    // endregion

    /**
     * 登录, 或重新登录.
     * 这个函数总是关闭一切现有网路任务 (但不会关闭其他任务), 然后重新登录并重新缓存好友列表和群列表.
     *
     * 一般情况下不需要重新登录. Mirai 能够自动处理掉线情况.
     *
     * @throws LoginFailedException 正常登录失败时抛出
     * @see alsoLogin `.apply { login() }` 捷径
     */
    @JvmSynthetic
    public abstract suspend fun login()

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see MemberNudge.sendTo 发送这个戳一戳消息
     */
    @MiraiExperimentalApi
    public fun nudge(): BotNudge = BotNudge(this)

    /**
     * 关闭这个 [Bot], 立即取消 [Bot] 的 [SupervisorJob].
     * 之后 [isActive] 将会返回 `false`.
     *
     * **注意:** 不可重新登录. 必须重新实例化一个 [Bot].
     *
     * @param cause 原因. 为 null 时视为正常关闭, 非 null 时视为异常关闭
     *
     * @see closeAndJoin 取消并 [Bot.join], 以确保 [Bot] 相关的活动被完全关闭
     */
    public abstract fun close(cause: Throwable? = null)

    public final override fun toString(): String = "Bot($id)"
}

/**
 * 获取 [Job] 的协程 [Job]. 此 [Job] 为一个 [SupervisorJob]
 */
@get:JvmSynthetic
public val Bot.supervisorJob: CompletableJob
    get() = this.coroutineContext[Job] as CompletableJob

/**
 * 挂起协程直到 [Bot] 协程被关闭 ([Bot.close]).
 * 即使 [Bot] 离线, 也会等待直到协程关闭.
 */
@JvmSynthetic
public suspend inline fun Bot.join(): Unit = this.coroutineContext[Job]!!.join()

/**
 * 关闭这个 [Bot], 停止一切相关活动. 所有引用都会被释放.
 *
 * 注: 不可重新登录. 必须重新实例化一个 [Bot].
 *
 * @param cause 原因. 为 null 时视为正常关闭, 非 null 时视为异常关闭
 */
@JvmSynthetic
public suspend inline fun Bot.closeAndJoin(cause: Throwable? = null) {
    close(cause)
    coroutineContext[Job]?.join()
}

@JvmSynthetic
public inline fun Bot.containsFriend(id: Long): Boolean = this.friends.contains(id)

@JvmSynthetic
public inline fun Bot.containsGroup(id: Long): Boolean = this.groups.contains(id)

@JvmSynthetic
public inline fun Bot.getFriendOrNull(id: Long): Friend? = this.friends.getOrNull(id)

@JvmSynthetic
public inline fun Bot.getGroupOrNull(id: Long): Group? = this.groups.getOrNull(id)
