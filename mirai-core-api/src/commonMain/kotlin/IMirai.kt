/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INTERFACE_NOT_SUPPORTED", "PropertyName")
@file:JvmName("Mirai")
@file:OptIn(LowLevelApi::class, MiraiExperimentalApi::class, MiraiInternalApi::class)
@file:JvmBlockingBridge

package net.mamoe.mirai

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event._EventBroadcast
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.FileCacheStrategy
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * [IMirai] 实例.
 */
@get:JvmName("getInstance") // Java 调用: Mirai.getInstance()
public val Mirai: IMirai
    get() = _MiraiInstance.get()

/**
 * Mirai API 接口. 是 Mirai API 与 Mirai 协议实现对接的接口.
 *
 * ## 获取实例
 *
 * 通常在引用 `net.mamoe:mirai-core` 模块后就可以通过 [Mirai] 获取到 [IMirai] 实例.
 * 在 Kotlin 调用顶层定义 `Mirai`, 在 Java 调用 `Mirai.getInstance()`.
 *
 * ### 使用 [IMirai] 的接口
 *
 * [IMirai] 中的接口通常是稳定
 *
 * ### 手动提供实例
 *
 * 默认通过 [_MiraiInstance.get] 使用 [java.util.ServiceLoader] 寻找实例. 若某些环境下 [java.util.ServiceLoader] 不可用, 可在 Kotlin 手动设置实例:
 * ```
 * @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE") // 必要
 * net.mamoe.mirai._MiraiInstance.set(net.mamoe.mirai.internal.MiraiImpl())
 * ```
 *
 * 但通常都可用自动获取而不需要手动设置.
 *
 * ## 稳定性
 *
 * ### 使用稳定
 *
 * 所有接口默认是可以稳定使用的. 但 [LowLevelApiAccessor] 中的方法默认是非常不稳定的.
 *
 * ### 继承不稳定
 *
 * **[IMirai] 可能会增加新的抽象属性或函数. 因此不适合被继承或实现.**
 *
 * @see Mirai 获取实例
 */
public interface IMirai : LowLevelApiAccessor {
    /**
     * 请优先使用 [BotFactory.INSTANCE]
     *
     * @see BotFactory.INSTANCE
     */
    public val BotFactory: BotFactory

    /**
     * Mirai 全局使用的 [FileCacheStrategy].
     *
     * 覆盖后将会立即应用到全局.
     *
     * @see FileCacheStrategy
     */
    public var FileCacheStrategy: FileCacheStrategy

    /**
     * Mirai 上传好友图片等使用的 Ktor [HttpClient].
     * 默认使用 [OkHttp] 引擎, 连接超时为 30s.
     *
     * 覆盖后将会立即应用到全局.
     */
    public var Http: HttpClient

    /**
     * 获取 uin.
     *
     * - 用户的 uin 就是用户的 ID (QQ 号码, [User.id]).
     * - 部分旧群的 uin 需要通过算法计算 [calculateGroupUinByGroupCode]. 新群的 uin 与在客户端能看到的群号码 ([Group.id]) 相同.
     *
     * 除了一些偏底层的 API 如 [MessageSourceBuilder.id] 外, mirai 的所有其他 API 都使用在客户端能看到的用户 QQ 号码和群号码 ([Contact.id]). 并会在需要的时候进行合适转换.
     * 若需要使用 uin, 在特定方法的文档中会标出.
     */
    public fun getUin(contactOrBot: ContactOrBot): Long {
        return if (contactOrBot is Group)
            calculateGroupUinByGroupCode(contactOrBot.id)
        else contactOrBot.id
    }

    /**
     * 使用 groupCode 计算 groupUin. 这两个值仅在 mirai 内部协议区分, 一般人使用时无需在意.
     * @see getUin
     */
    public fun calculateGroupUinByGroupCode(groupCode: Long): Long {
        var left: Long = groupCode / 1000000L
        when (left) {
            in 0..10 -> left += 202
            in 11..19 -> left += 480 - 11
            in 20..66 -> left += 2100 - 20
            in 67..156 -> left += 2010 - 67
            in 157..209 -> left += 2147 - 157
            in 210..309 -> left += 4100 - 210
            in 310..499 -> left += 3800 - 310
        }
        return left * 1000000L + groupCode % 1000000L
    }

    /**
     * 使用 groupUin 计算 groupCode. 这两个值仅在 mirai 内部协议区分, 一般人使用时无需在意.
     * @see getUin
     */
    public fun calculateGroupCodeByGroupUin(groupUin: Long): Long {
        var left: Long = groupUin / 1000000L
        when (left) {
            in 0 + 202..10 + 202 -> left -= 202
            in 11 + 480 - 11..19 + 480 - 11 -> left -= 480 - 11
            in 20 + 2100 - 20..66 + 2100 - 20 -> left -= 2100 - 20
            in 67 + 2010 - 67..156 + 2010 - 67 -> left -= 2010 - 67
            in 157 + 2147 - 157..209 + 2147 - 157 -> left -= 2147 - 157
            in 210 + 4100 - 210..309 + 4100 - 210 -> left -= 4100 - 210
            in 310 + 3800 - 310..499 + 3800 - 310 -> left -= 3800 - 310
        }
        return left * 1000000L + groupUin % 1000000L
    }

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
     * @see IMirai.recallMessage (扩展函数) 接受参数 [MessageChain]
     * @see MessageSource.recall 撤回消息扩展
     */
    public suspend fun recallMessage(bot: Bot, source: MessageSource)

    /**
     * 发送戳一戳消息
     */
    public suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean

    /**
     * 构造 [Image]
     *
     * @see Image
     * @see Image.fromId
     */
    public fun createImage(imageId: String): Image

    /**
     * 创建一个 [FileMessage]. [name] 与 [size] 只供本地使用, 发送消息时只会使用 [id] 和 [internalId].
     * @since 2.5
     */
    public fun createFileMessage(id: String, internalId: Int, name: String, size: Long): FileMessage

    /**
     * 创建 [UnsupportedMessage]
     * @since 2.6
     */
    public fun createUnsupportedMessage(struct: ByteArray): UnsupportedMessage

    /**
     * 获取图片下载链接
     *
     * @see Image.queryUrl [Image] 的扩展函数
     */
    public suspend fun queryImageUrl(bot: Bot, image: Image): String

    /**
     * 查询某个用户的信息
     *
     * @since 2.1
     */
    public suspend fun queryProfile(bot: Bot, targetId: Long): UserProfile

    /**
     * 构造一个 [OfflineMessageSource].
     *
     * 更推荐使用 [MessageSourceBuilder] 和 [MessageSource.copyAmend] 创建 [OfflineMessageSource].
     *
     * @param ids 即 [MessageSource.ids]
     * @param internalIds 即 [MessageSource.internalIds]
     */
    public fun constructMessageSource(
        botId: Long,
        kind: MessageSourceKind,
        fromId: Long, targetId: Long,
        ids: IntArray, time: Int, internalIds: IntArray,
        originalMessage: MessageChain
    ): OfflineMessageSource

    /**
     * @since 2.3
     */
    public suspend fun downloadLongMessage(
        bot: Bot,
        resourceId: String,
    ): MessageChain

    /**
     * @since 2.3
     */
    public suspend fun downloadForwardMessage(
        bot: Bot,
        resourceId: String,
    ): List<ForwardMessage.Node>

    /**
     * 通过好友验证
     *
     * @param event 好友验证的事件对象
     */
    public suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent)

    /**
     * 拒绝好友验证
     *
     * @param event 好友验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    public suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean = false)

    /**
     * 通过加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     */
    public suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent)

    /**
     * 拒绝加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    public suspend fun rejectMemberJoinRequest(
        event: MemberJoinRequestEvent,
        blackList: Boolean = false,
        message: String = ""
    )

    /**
     * 获取在线的 [OtherClient] 列表
     * @param mayIncludeSelf 服务器返回的列表可能包含 [Bot] 自己. [mayIncludeSelf] 为 `false` 会排除自己
     */
    public suspend fun getOnlineOtherClientsList(
        bot: Bot,
        mayIncludeSelf: Boolean = false
    ): List<OtherClientInfo>

    /**
     * 忽略加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 忽略后是否拉入黑名单
     */
    public suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

    /**
     * 接收邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    public suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)

    /**
     * 忽略邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    public suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)

    /**
     * 广播一个事件. 由 [Event.broadcast] 调用.
     */
    public suspend fun broadcastEvent(event: Event) {
        _EventBroadcast.implementation.broadcastImpl(event)
    }
}

/**
 * 撤回这条消息.
 *
 * [Bot] 撤回自己的消息不需要权限, 但需要在发出后 2 分钟内撤回.
 * [Bot] 撤回群员的消息需要管理员权限, 可在任意时间撤回.
 *
 * @throws PermissionDeniedException 当 [Bot] 无权限操作时
 * @see IMirai.recallMessage
 */
@JvmSynthetic
public suspend inline fun IMirai.recallMessage(bot: Bot, message: MessageChain): Unit =
    this.recallMessage(bot, message.source)

/**
 * @since 2.6-RC
 */
@PublishedApi // for tests and potential public uses.
@Suppress("ClassName")
internal object _MiraiInstance {
    private var instance: IMirai? = null

    @JvmStatic
    fun set(instance: IMirai) {
        this.instance = instance
    }

    /**
     * 获取通过 [set] 设置的实例, 或使用 [findMiraiInstance] 寻找一个实例.
     */
    @JvmStatic
    fun get(): IMirai {
        return instance ?: findMiraiInstance().also { instance = it }
    }
}

@JvmSynthetic
internal expect fun findMiraiInstance(): IMirai