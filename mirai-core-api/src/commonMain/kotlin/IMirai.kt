/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INTERFACE_NOT_SUPPORTED")
@file:JvmName("Mirai")
@file:OptIn(LowLevelApi::class, MiraiExperimentalApi::class, MiraiInternalApi::class)

package net.mamoe.mirai

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.*
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
 * [IMirai] 实例
 */
@get:JvmName("getInstance") // Java 调用: Mirai.getInstance()
public val Mirai: IMirai by lazy { findMiraiInstance() }

/**
 * Mirai API 接口.
 *
 * @see Mirai 获取实例
 */
public interface IMirai : LowLevelApiAccessor {
    /**
     * 此 API 不稳定. 请优先使用 [BotFactory.INSTANCE]
     *
     * @see BotFactory.INSTANCE
     */
    @Suppress("PropertyName")
    @MiraiExperimentalApi
    public val BotFactory: BotFactory

    /**
     * Mirai 全局使用的 [FileCacheStrategy].
     */
    @Suppress("PropertyName")
    @MiraiExperimentalApi
    public var FileCacheStrategy: FileCacheStrategy

    /**
     * 使用 groupCode 计算 groupUin. 这两个值仅在 mirai 内部协议区分, 一般人使用时无需在意.
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
    @JvmBlockingBridge
    public suspend fun recallMessage(bot: Bot, source: MessageSource)

    /**
     * 发送戳一戳消息
     */
    @JvmBlockingBridge
    public suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean

    /**
     * 构造 [Image]
     *
     * @see Image
     * @see Image.fromId
     */
    public fun createImage(imageId: String): Image

    /**
     * 获取图片下载链接
     *
     * @see Image.queryUrl [Image] 的扩展函数
     */
    @JvmBlockingBridge
    public suspend fun queryImageUrl(bot: Bot, image: Image): String

    /**
     * 构造一个 [OfflineMessageSource]
     *
     * @param ids 即 [MessageSource.ids]
     * @param internalIds 即 [MessageSource.internalIds]
     *
     * @param fromUin 为用户时为 [Friend.id], 为群时需使用 [IMirai.calculateGroupUinByGroupCode] 计算
     * @param targetUin 为用户时为 [Friend.id], 为群时需使用 [IMirai.calculateGroupUinByGroupCode] 计算
     */
    @MiraiExperimentalApi("This is very experimental and is subject to change.")
    public fun constructMessageSource(
        botId: Long,
        kind: MessageSourceKind,
        fromUin: Long, targetUin: Long,
        ids: IntArray, time: Int, internalIds: IntArray,
        originalMessage: MessageChain
    ): OfflineMessageSource


    /**
     * 通过好友验证
     *
     * @param event 好友验证的事件对象
     */
    @JvmBlockingBridge
    public suspend fun acceptNewFriendRequest(event: NewFriendRequestEvent)

    /**
     * 拒绝好友验证
     *
     * @param event 好友验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @JvmBlockingBridge
    public suspend fun rejectNewFriendRequest(event: NewFriendRequestEvent, blackList: Boolean = false)

    /**
     * 通过加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     */
    @JvmBlockingBridge
    public suspend fun acceptMemberJoinRequest(event: MemberJoinRequestEvent)

    /**
     * 拒绝加群验证（需管理员权限）
     *
     * @param event 加群验证的事件对象
     * @param blackList 拒绝后是否拉入黑名单
     */
    @JvmBlockingBridge
    public suspend fun rejectMemberJoinRequest(
        event: MemberJoinRequestEvent,
        blackList: Boolean = false,
        message: String = ""
    )

    /**
     * 获取在线的 [OtherClient] 列表
     * @param mayIncludeSelf 服务器返回的列表可能包含 [Bot] 自己. [mayIncludeSelf] 为 `false` 会排除自己
     */
    @JvmBlockingBridge
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
    @JvmBlockingBridge
    public suspend fun ignoreMemberJoinRequest(event: MemberJoinRequestEvent, blackList: Boolean = false)

    /**
     * 接收邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    @JvmBlockingBridge
    public suspend fun acceptInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)

    /**
     * 忽略邀请入群（需管理员权限）
     *
     * @param event 邀请入群的事件对象
     */
    @JvmBlockingBridge
    public suspend fun ignoreInvitedJoinGroupRequest(event: BotInvitedJoinGroupRequestEvent)
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

@JvmSynthetic
internal expect fun findMiraiInstance(): IMirai