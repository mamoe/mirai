/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
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
import net.mamoe.mirai.network.LoginFailedException
import net.mamoe.mirai.utils.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.jvm.JvmField
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
 * @see isActive 判断 [Bot] 是否正常运行中. (协程正常运行) (但不能判断是否在线, 需使用 [isOnline])
 *
 * @see BotFactory 构造 [Bot] 的工厂, [Bot] 唯一的构造方式.
 */
@Suppress("INAPPLICABLE_JVM_NAME", "EXPOSED_SUPER_CLASS")
abstract class Bot internal constructor(
    val configuration: BotConfiguration
) : CoroutineScope, LowLevelBotAPIAccessor, BotJavaFriendlyAPI, ContactOrBot {
    final override val coroutineContext: CoroutineContext = // for id
        configuration.parentCoroutineContext
            .plus(SupervisorJob(configuration.parentCoroutineContext[Job]))
            .plus(configuration.parentCoroutineContext[CoroutineExceptionHandler]
                ?: CoroutineExceptionHandler { _, e ->
                    logger.error("An exception was thrown under a coroutine of Bot", e)
                }
            )
            .plus(CoroutineName("Mirai Bot"))


    companion object {
        @JvmField
        @Suppress("ObjectPropertyName")
        internal val _instances: LockFreeLinkedList<WeakRef<Bot>> = LockFreeLinkedList()

        @PlannedRemoval("1.2.0")
        @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
        @JvmStatic
        val instances: List<WeakRef<Bot>>
            get() = _instances.toList()

        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @JvmStatic
        val botInstances: List<Bot>
            get() = _instances.asSequence().mapNotNull { it.get() }.toList()

        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @SinceMirai("1.1.0")
        @JvmStatic
        val botInstancesSequence: Sequence<Bot>
            get() = _instances.asSequence().mapNotNull { it.get() }

        /**
         * 遍历每一个 [Bot] 实例
         */
        @JvmSynthetic
        fun forEachInstance(block: (Bot) -> Unit) = _instances.forEach { it.get()?.let(block) }

        /**
         * 获取一个 [Bot] 实例, 无对应实例时抛出 [NoSuchElementException]
         */
        @JvmStatic
        @Throws(NoSuchElementException::class)
        fun getInstance(qq: Long): Bot =
            getInstanceOrNull(qq) ?: throw NoSuchElementException(qq.toString())

        /**
         * 获取一个 [Bot] 实例, 无对应实例时返回 `null`
         */
        @JvmStatic
        fun getInstanceOrNull(qq: Long): Bot? =
            _instances.asSequence().mapNotNull { it.get() }.firstOrNull { it.id == qq }
    }

    init {
        _instances.addLast(this.weakRef())
        supervisorJob.invokeOnCompletion {
            _instances.removeIf { it.get()?.id == this.id }
        }
    }

    /**
     * [Bot] 运行的 [Context].
     *
     * 在 JVM 的默认实现为 `class ContextImpl : Context`
     * 在 Android 实现为 `android.content.Context`
     */
    @MiraiExperimentalAPI
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

    /**
     * 判断 Bot 是否在线 (可正常收发消息)
     */
    @SinceMirai("1.0.1")
    abstract val isOnline: Boolean

    // region contacts

    /**
     * [User.id] 与 [Bot.id] 相同的 [_lowLevelNewFriend] 实例
     */
    @MiraiExperimentalAPI
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
     * @param source 消息源. 可从 [MessageReceipt.source] 获得, 或从消息事件中的 [MessageChain] 获得, 或通过 [buildMessageSource] 构建.
     *
     * @throws PermissionDeniedException 当 [Bot] 无权限操作时抛出
     * @throws IllegalStateException 当这条消息已经被撤回时抛出 (仅同步主动操作)
     *
     * @see Bot.recall (扩展函数) 接受参数 [MessageChain]
     * @see MessageSource.recall 撤回消息扩展
     */
    @JvmSynthetic
    abstract suspend fun recall(source: MessageSource)

    /**
     * 获取图片下载链接
     *
     * @see Image.queryUrl [Image] 的扩展函数
     */
    @PlannedRemoval("1.2.0")
    @Deprecated(
        "use extension.",
        replaceWith = ReplaceWith("image.queryUrl()", imports = ["net.mamoe.mirai.message.data.queryUrl"]),
        level = DeprecationLevel.ERROR
    )
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
    @MiraiExperimentalAPI("This is very experimental and is subject to change.")
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
    @PlannedRemoval("1.2.0")
    @Deprecated("use member function.", replaceWith = ReplaceWith("event.accept()"), level = DeprecationLevel.ERROR)
    @JvmSynthetic
    abstract suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent)

    /**
     * 拒绝好友验证
     *
     * @param event 好友验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @PlannedRemoval("1.2.0")
    @Deprecated(
        "use member function.",
        replaceWith = ReplaceWith("event.reject(blackList)"),
        level = DeprecationLevel.ERROR
    )
    @JvmSynthetic
    abstract suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean = false)

    /**
     * 通过加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     */
    @PlannedRemoval("1.2.0")
    @Deprecated("use member function.", replaceWith = ReplaceWith("event.accept()"), level = DeprecationLevel.ERROR)
    @JvmSynthetic
    abstract suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent)

    /**
     * 拒绝加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @PlannedRemoval("1.2.0")
    @Deprecated(
        "use member function.",
        replaceWith = ReplaceWith("event.reject(blackList)"),
        level = DeprecationLevel.HIDDEN
    )
    abstract suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

    @JvmSynthetic
    abstract suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false, message: String = "")

    /**
     * 忽略加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 忽略后是否拉入黑名单
     */
    @PlannedRemoval("1.2.0")
    @Deprecated(
        "use member function.",
        replaceWith = ReplaceWith("event.ignore(blackList)"),
        level = DeprecationLevel.ERROR
    )
    @JvmSynthetic
    abstract suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

    /**
     * 接收邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    @PlannedRemoval("1.2.0")
    @Deprecated("use member function.", replaceWith = ReplaceWith("event.accept()"), level = DeprecationLevel.ERROR)
    @JvmSynthetic
    abstract suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)

    /**
     * 忽略邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    @PlannedRemoval("1.2.0")
    @Deprecated("use member function.", replaceWith = ReplaceWith("event.ignore()"), level = DeprecationLevel.ERROR)
    @JvmSynthetic
    abstract suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)

    // endregion

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
    abstract fun close(cause: Throwable? = null)

    final override fun toString(): String = "Bot($id)"
}

/**
 * 获取 [Job] 的协程 [Job]. 此 [Job] 为一个 [SupervisorJob]
 */
@get:JvmSynthetic
val Bot.supervisorJob: CompletableJob
    get() = this.coroutineContext[Job] as CompletableJob

/**
 * 挂起协程直到 [Bot] 协程被关闭 ([Bot.close]).
 * 即使 [Bot] 离线, 也会等待直到协程关闭.
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
