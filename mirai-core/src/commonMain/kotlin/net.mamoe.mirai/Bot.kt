/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "FunctionName", "NOTHING_TO_INLINE", "UnusedImport",
    "EXPERIMENTAL_OVERRIDE")

package net.mamoe.mirai

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.AddFriendResult
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
// 任何人都能看到这个方法

/**
 * 机器人对象. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * 注: Bot 为全协程实现, 没有其他任务时若不使用 [join], 主线程将会退出.
 *
 * @see Contact 联系人
 * @see kotlinx.coroutines.isActive 判断 [Bot] 是否正常运行中. (在线, 且没有被 [close])
 */
@Suppress("INAPPLICABLE_JVM_NAME")
@OptIn(MiraiInternalAPI::class, LowLevelAPI::class)
abstract class Bot : CoroutineScope, LowLevelBotAPIAccessor, BotJavaFriendlyAPI(), Identified {
    companion object {
        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        @JvmStatic
        val instances: List<WeakRef<Bot>>
            get() = BotImpl.instances.toList()

        /**
         * 遍历每一个 [Bot] 实例
         */
        inline fun forEachInstance(block: (Bot) -> Unit) = BotImpl.forEachInstance(block)

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

    @PlannedRemoval("1.0.0")
    @Deprecated("use id instead", replaceWith = ReplaceWith("id"))
    abstract val uin: Long

    /**
     * QQ 号码. 实际类型为 uint
     */
    @SinceMirai("0.32.0")
    abstract override val id: Long

    /**
     * 昵称
     */
    @SinceMirai("0.33.1")
    abstract val nick: String

    /**
     * 日志记录器
     */
    abstract val logger: MiraiLogger

    // region contacts

    /**
     * [QQ.id] 与 [Bot.uin] 相同的 [_lowLevelNewQQ] 实例
     */
    abstract val selfQQ: QQ

    /**
     * 机器人的好友列表. 与服务器同步更新
     */
    abstract val friends: ContactList<QQ>

    /**
     * 获取一个好友对象.
     * @throws [NoSuchElementException] 当不存在这个好友时抛出
     */
    fun getFriend(id: Long): QQ = friends.firstOrNull { it.id == id } ?: throw NoSuchElementException("friend $id")

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
     */
    @JvmSynthetic
    abstract suspend fun queryImageUrl(image: Image): String

    /**
     * 获取图片下载链接并开始下载.
     *
     * @see ByteReadChannel.copyAndClose
     * @see ByteReadChannel.copyTo
     */
    @PlannedRemoval("1.0.0")
    @Deprecated("use your own Http clients, this is going to be removed in 1.0.0", level = DeprecationLevel.WARNING)
    @MiraiExperimentalAPI
    @JvmSynthetic
    abstract suspend fun openChannel(image: Image): ByteReadChannel

    /**
     * 添加一个好友
     *
     * @param message 若需要验证请求时的验证消息.
     * @param remark 好友备注
     */
    @JvmSynthetic
    @MiraiExperimentalAPI("未支持")
    abstract suspend fun addFriend(id: Long, message: String? = null, remark: String? = null): AddFriendResult


    /**
     * 通过好友验证
     *
     * @param event 好友验证的事件对象
     */
    @SinceMirai("0.35.0")
    @JvmSynthetic
    abstract suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent)

    /**
     * 拒绝好友验证
     *
     * @param event 好友验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @SinceMirai("0.35.0")
    @JvmSynthetic
    abstract suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean = false)

    /**
     * 通过加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     */
    @SinceMirai("0.35.0")
    @JvmSynthetic
    abstract suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent)

    /**
     * 拒绝加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @SinceMirai("0.35.0")
    @JvmSynthetic
    abstract suspend fun rejectMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

    /**
     * 忽略加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 忽略后是否拉入黑名单
     */
    @SinceMirai("0.35.0")
    @JvmSynthetic
    abstract suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

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

    @OptIn(LowLevelAPI::class, MiraiExperimentalAPI::class)
    final override fun toString(): String = "Bot($id)"

    /**
     * 网络模块.
     * 此为内部 API: 它可能在任意时刻被改动.
     */
    @MiraiInternalAPI
    abstract val network: BotNetworkHandler

    @PlannedRemoval("1.0.0")
    @Deprecated("for binary compatibility until 1.0.0", level = DeprecationLevel.HIDDEN)
    suspend inline fun Bot.join() = this.coroutineContext[Job]!!.join()
}

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
inline fun Bot.recallIn(
    source: MessageSource,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.launch(coroutineContext + CoroutineName("MessageRecall")) {
    kotlinx.coroutines.delay(millis)
    recall(source)
}

/**
 * 在一段时间后撤回这条消息.
 *
 * @param millis 延迟的时间, 单位为毫秒
 * @param coroutineContext 额外的 [CoroutineContext]
 * @see recall
 */
@JvmSynthetic
inline fun Bot.recallIn(
    message: MessageChain,
    millis: Long,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
): Job = this.launch(coroutineContext + CoroutineName("MessageRecall")) {
    kotlinx.coroutines.delay(millis)
    recall(message)
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
inline fun Bot.getFriendOrNull(id: Long): QQ? = this.friends.getOrNull(id)

@JvmSynthetic
inline fun Bot.getGroupOrNull(id: Long): Group? = this.groups.getOrNull(id)
