/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "EXPERIMENTAL_API_USAGE", "unused", "FunctionName", "NOTHING_TO_INLINE", "UnusedImport",
    "EXPERIMENTAL_OVERRIDE"
)

package net.mamoe.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 登录, 返回 [this]
 */
@JvmSynthetic
suspend inline fun <B : Bot> B.alsoLogin(): B = also { login() }

/**
 * 机器人对象. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * 有关 [Bot] 生命管理, 请查看 [BotConfiguration.inheritCoroutineContext]
 *
 * @see Contact 联系人
 * @see kotlinx.coroutines.isActive 判断 [Bot] 是否正常运行中. (在线, 且没有被 [close])
 *
 * @see BotFactory 构造 [Bot] 的工厂, [Bot] 唯一的构造方式.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
abstract class Bot : CoroutineScope, LowLevelBotAPIAccessor, BotJavaFriendlyAPI(), ContactOrBot {
    companion object {
        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @PlannedRemoval("1.2.0")
        @Deprecated("use botInstances instead", replaceWith = ReplaceWith("botInstances"))
        @JvmStatic
        val instances: List<WeakRef<Bot>>
            get() = BotImpl.instances.toList()

        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @JvmStatic
        val botInstances: List<Bot>
            get() = BotImpl.instances.asSequence().mapNotNull { it.get() }.toList()

        /**
         * 遍历每一个 [Bot] 实例
         */
        fun forEachInstance(block: (Bot) -> Unit) = BotImpl.forEachInstance(block)

        /**
         * 获取一个 [Bot] 实例, 找不到则 [NoSuchElementException]
         */
        @JvmStatic
        fun getInstance(qq: Long): Bot = BotImpl.getInstance(qq = qq)
    }

    /**
     * [Bot] 运行的 [Context].
     *
     * 在 JVM 的默认实现为 `class ContextImpl : Context`
     * 在 Android 实现为 `android.content.Context`
     */
    abstract val context: Context

    /**
     * QQ 号码. 实际类型为 uint
     */
    abstract override val id: Long

    /**
     * 昵称
     */
    abstract val nick: String

    /**
     * 日志记录器
     */
    abstract val logger: MiraiLogger

    // region contacts

    /**
     * [User.id] 与 [Bot.id] 相同的 [_lowLevelNewFriend] 实例
     */
    abstract val selfQQ: Friend


    /**
     * 机器人的好友列表. 与服务器同步更新
     */
    abstract val friends: ContactList<Friend>

    /**
     * 获取一个好友对象.
     * @throws [NoSuchElementException] 当不存在这个好友时抛出
     */
    fun getFriend(id: Long): Friend = friends.firstOrNull { it.id == id } ?: throw NoSuchElementException("friend $id")

    /**
     * 机器人加入的群列表. 与服务器同步更新
     */
    abstract val groups: ContactList<Group>

    /**
     * 获取一个机器人加入的群.
     * @throws NoSuchElementException 当不存在这个群时抛出
     */
    fun getGroup(id: Long): Group = groups.firstOrNull { it.id == id } ?: throw NoSuchElementException("group $id")

    // endregion

    // region network

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
    abstract suspend fun login()
    // endregion


    // region actions

    /**
     * 撤回这条消息. 可撤回自己 2 分钟内发出的消息, 和任意时间的群成员的消息.
     *
     * [Bot] 撤回自己的消息不需要权限.
     * [Bot] 撤回群员的消息需要管理员权限.
     *
     * @param source 消息源. 可从 [MessageReceipt.source] 获得, 或从消息事件中的 [MessageChain] 获得.
     *
     * @throws PermissionDeniedException 当 [Bot] 无权限操作时
     * @throws IllegalStateException 当这条消息已经被撤回时 (仅同步主动操作)
     *
     * @see Bot.recall (扩展函数) 接受参数 [MessageChain]
     * @see MessageSource.recall
     */
    @JvmSynthetic
    abstract suspend fun recall(source: MessageSource)

    /**
     * 获取图片下载链接
     *
     * @see Image.queryUrl [Image] 的扩展函数
     */
    @JvmSynthetic
    abstract suspend fun queryImageUrl(image: Image): String

    /**
     * 构造一个 [OfflineMessageSource]
     *
     * @param id 即 [MessageSource.id]
     * @param internalId 即 [MessageSource.internalId]
     *
     * @param fromUin 为用户时为 [Friend.id], 为群时需使用 [Group.calculateGroupUinByGroupCode] 计算
     * @param targetUin 为用户时为 [Friend.id], 为群时需使用 [Group.calculateGroupUinByGroupCode] 计算
     */
    @MiraiExperimentalAPI
    abstract fun constructMessageSource(
        kind: OfflineMessageSource.Kind,
        fromUin: Long, targetUin: Long,
        id: Int, time: Int, internalId: Int,
        originalMessage: MessageChain
    ): OfflineMessageSource


    /**
     * 通过好友验证
     *
     * @param event 好友验证的事件对象
     */
    @JvmSynthetic
    abstract suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent)

    /**
     * 拒绝好友验证
     *
     * @param event 好友验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @JvmSynthetic
    abstract suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean = false)

    /**
     * 通过加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     */
    @JvmSynthetic
    abstract suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent)

    /**
     * 拒绝加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @JvmSynthetic
    abstract suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

    /**
     * 忽略加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 忽略后是否拉入黑名单
     */
    @JvmSynthetic
    abstract suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

    /**
     * 接收邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    @JvmSynthetic
    abstract suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)

    /**
     * 忽略邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    @JvmSynthetic
    abstract suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)

    // endregion

    /**
     * 关闭这个 [Bot], 立即取消 [Bot] 的 [kotlinx.coroutines.SupervisorJob].
     * 之后 [kotlinx.coroutines.isActive] 将会返回 `false`.
     *
     * **注意:** 不可重新登录. 必须重新实例化一个 [Bot].
     *
     * @param cause 原因. 为 null 时视为正常关闭, 非 null 时视为异常关闭
     *
     * @see closeAndJoin 取消并 [Bot.join], 以确保 [Bot] 相关的活动被完全关闭
     */
    abstract fun close(cause: Throwable? = null)

    final override fun toString(): String = "Bot($id)"

    /**
     * 网络模块.
     * 此为内部 API: 它可能在任意时刻被改动.
     */
    @MiraiInternalAPI
    abstract val network: BotNetworkHandler
}

/**
 * 获取 [Job] 的协程 [Job]. 此 [Job] 为一个 [SupervisorJob]
 */
@get:JvmSynthetic
inline val Bot.supervisorJob: CompletableJob
    get() = this.coroutineContext[Job] as CompletableJob

/**
 * 挂起协程直到 [Bot] 下线.
 */
@JvmSynthetic
suspend inline fun Bot.join() = this.coroutineContext[Job]!!.join()

/**
 * 撤回这条消息.
 *
 * [Bot] 撤回自己的消息不需要权限, 但需要在发出后 2 分钟内撤回.
 * [Bot] 撤回群员的消息需要管理员权限, 可在任意时间撤回.
 *
 * @throws PermissionDeniedException 当 [Bot] 无权限操作时
 * @see Bot.recall
 */
@JvmSynthetic
suspend inline fun Bot.recall(message: MessageChain) =
    this.recall(message.source)

/**
 * 在一段时间后撤回这个消息源所指代的消息.
 *
 * @param millis 延迟的时间, 单位为毫秒
 * @param coroutineContext 额外的 [CoroutineContext]
 * @see recall
 */
@JvmSynthetic
inline fun CoroutineScope.recallIn(
    source: MessageSource,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.launch(coroutineContext + CoroutineName("MessageRecall")) {
    delay(millis)
    source.recall()
}

/**
 * 在一段时间后撤回这条消息.
 *
 * @param millis 延迟的时间, 单位为毫秒
 * @param coroutineContext 额外的 [CoroutineContext]
 * @see recall
 */
@JvmSynthetic
inline fun CoroutineScope.recallIn(
    message: MessageChain,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.launch(coroutineContext + CoroutineName("MessageRecall")) {
    delay(millis)
    message.recall()
}

/**
 * 关闭这个 [Bot], 停止一切相关活动. 所有引用都会被释放.
 *
 * 注: 不可重新登录. 必须重新实例化一个 [Bot].
 *
 * @param cause 原因. 为 null 时视为正常关闭, 非 null 时视为异常关闭
 */
@JvmSynthetic
suspend inline fun Bot.closeAndJoin(cause: Throwable? = null) {
    close(cause)
    coroutineContext[Job]?.join()
}

@JvmSynthetic
inline fun Bot.containsFriend(id: Long): Boolean = this.friends.contains(id)

@JvmSynthetic
inline fun Bot.containsGroup(id: Long): Boolean = this.groups.contains(id)

@JvmSynthetic
inline fun Bot.getFriendOrNull(id: Long): Friend? = this.friends.getOrNull(id)

@JvmSynthetic
inline fun Bot.getGroupOrNull(id: Long): Group? = this.groups.getOrNull(id)
