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
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.message.action.BotNudge
import net.mamoe.mirai.message.action.MemberNudge
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.MiraiLogger
import java.util.concurrent.ConcurrentHashMap

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
public interface Bot : CoroutineScope, ContactOrBot, UserOrBot {
    /**
     * Bot 配置
     */
    public val configuration: BotConfiguration

    /**
     * QQ 号码. 实际类型为 uint
     */
    public override val id: Long

    /**
     * 返回 `this`
     */
    public override val bot: Bot get() = this

    /**
     * 昵称
     */
    public val nick: String

    /**
     * 日志记录器
     */
    public val logger: MiraiLogger

    /**
     * 当 Bot 在线 (可正常收发消息) 时返回 `true`.
     */
    public val isOnline: Boolean

    /**
     * 来自这个 [Bot] 的 [BotEvent] 的事件通道.
     * @see EventChannel
     */
    public val eventChannel: EventChannel<BotEvent>

    // region contacts

    /**
     * 其他设备列表
     */
    public val otherClients: OtherClientList


    /**
     * [User.id] 与 [Bot.id] 相同的 [Friend] 实例
     */
    public val asFriend: Friend

    /**
     * [User.id] 与 [Bot.id] 相同的 [Stranger] 实例
     */
    public val asStranger: Stranger


    /**
     * 陌生人列表. 与服务器同步更新.
     */
    public val strangers: ContactList<Stranger>

    /**
     * 以 [对方 QQ 号码][id] 获取一个陌生人对象, 在获取失败时返回 `null`.
     */
    public fun getStranger(id: Long): Stranger? =
        strangers.firstOrNull { it.id == id }

    /**
     * 以 [对方 QQ 号码][id] 获取一个陌生人对象, 在获取失败时抛出 [NoSuchElementException].
     */
    public fun getStrangerOrFail(id: Long): Stranger = getStranger(id) ?: throw NoSuchElementException("stranger $id")

    /**
     * 好友列表. 与服务器同步更新.
     */
    public val friends: ContactList<Friend>


    /**
     * 以 [对方 QQ 号码][id] 获取一个好友对象, 在获取失败时返回 `null`.
     * 在 [id] 与 [Bot.id] 相同时返回 [Bot.asFriend]
     */
    public fun getFriend(id: Long): Friend? =
        friends.firstOrNull { it.id == id } ?: if (id == this.id) asFriend else null

    /**
     * 以 [对方 QQ 号码][id] 获取一个好友对象, 在获取失败时抛出 [NoSuchElementException].
     */
    public fun getFriendOrFail(id: Long): Friend = getFriend(id) ?: throw NoSuchElementException("friend $id")

    /**
     * 加入的群列表. 与服务器同步更新.
     */
    public val groups: ContactList<Group>

    /**
     * 以 [群号码][id] 获取一个群对象, 在获取失败时返回 `null`.
     */
    public fun getGroup(id: Long): Group? =
        groups.firstOrNull { it.id == id }

    /**
     * 以 [群号码][id] 获取一个群对象, 在获取失败时抛出 [NoSuchElementException].
     */
    public fun getGroupOrFail(id: Long): Group = getGroup(id) ?: throw NoSuchElementException("group $id")

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
    @JvmBlockingBridge
    public suspend fun login()

    /**
     * 创建一个 "戳一戳" 消息
     *
     * @see MemberNudge.sendTo 发送这个戳一戳消息
     */
    public override fun nudge(): BotNudge = BotNudge(this)

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
    public fun close(cause: Throwable? = null)


    public companion object {
        @Suppress("ObjectPropertyName")
        internal val _instances: ConcurrentHashMap<Long, Bot> = ConcurrentHashMap()

        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @JvmStatic
        public val instances: List<Bot>
            get() = _instances.values.toList()

        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @JvmStatic
        public val instancesSequence: Sequence<Bot>
            get() = _instances.values.asSequence().filterNotNull()

        /**
         * 获取一个 [Bot] 实例, 无对应实例时抛出 [NoSuchElementException]
         */
        @JvmStatic
        @Throws(NoSuchElementException::class)
        public fun getInstance(qq: Long): Bot =
            findInstance(qq) ?: throw NoSuchElementException(qq.toString())

        /**
         * 获取一个 [Bot] 实例, 无对应实例时返回 `null`
         */
        @JvmStatic
        public inline fun getInstanceOrNull(qq: Long): Bot? = findInstance(qq)

        /**
         * 获取一个 [Bot] 实例, 无对应实例时返回 `null`
         */
        @JvmStatic
        public fun findInstance(qq: Long): Bot? = _instances[qq]
    }

    /**
     * 挂起协程直到 [Bot] 协程被关闭 ([Bot.close]).
     * 即使 [Bot] 离线, 也会等待直到协程关闭.
     */
    @JvmBlockingBridge
    public suspend fun join(): Unit = supervisorJob.join()


    /**
     * 关闭这个 [Bot], 停止一切相关活动. 所有引用都会被释放.
     *
     * 注: 不可重新登录. 必须重新实例化一个 [Bot].
     *
     * @param cause 原因. 为 null 时视为正常关闭, 非 null 时视为异常关闭
     */
    @JvmBlockingBridge
    public suspend fun closeAndJoin(cause: Throwable? = null) {
        close(cause)
        join()
    }
}

/**
 * 获取 [Job] 的协程 [Job]. 此 [Job] 为一个 [SupervisorJob]
 */
@get:JvmSynthetic
public inline val Bot.supervisorJob: CompletableJob
    get() = this.coroutineContext[Job] as CompletableJob

/**
 * 当 [Bot] 拥有 [Friend.id] 为 [id] 的好友时返回 `true`.
 */
@JvmSynthetic
public inline fun Bot.containsFriend(id: Long): Boolean = this.friends.contains(id)

/**
 * 当 [Bot] 拥有 [Group.id] 为 [id] 的群时返回 `true`.
 */
@JvmSynthetic
public inline fun Bot.containsGroup(id: Long): Boolean = this.groups.contains(id)