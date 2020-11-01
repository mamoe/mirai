/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("INTERFACE_NOT_SUPPORTED")

package net.mamoe.mirai

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.MemberJoinRequestEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.action.Nudge
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.jvm.JvmSynthetic

@OptIn(LowLevelApi::class, MiraiExperimentalApi::class)
public interface Mirai : LowLevelApiAccessor {
    @Suppress("PropertyName")
    @MiraiExperimentalApi
    public val BotFactory: BotFactory

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
     * @see Mirai.recall (扩展函数) 接受参数 [MessageChain]
     * @see MessageSource.recall 撤回消息扩展
     */
    @JvmBlockingBridge
    public suspend fun recall(bot: Bot, source: MessageSource)

    @JvmBlockingBridge
    public suspend fun sendNudge(bot: Bot, nudge: Nudge, receiver: Contact): Boolean

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
     * @param id 即 [MessageSource.id]
     * @param internalId 即 [MessageSource.internalId]
     *
     * @param fromUin 为用户时为 [Friend.id], 为群时需使用 [Group.calculateGroupUinByGroupCode] 计算
     * @param targetUin 为用户时为 [Friend.id], 为群时需使用 [Group.calculateGroupUinByGroupCode] 计算
     */
    @MiraiExperimentalApi("This is very experimental and is subject to change.")
    public fun constructMessageSource(
        bot: Bot,
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


    public companion object INSTANCE : Mirai by findMiraiInstance()
}

/**
 * 撤回这条消息.
 *
 * [Bot] 撤回自己的消息不需要权限, 但需要在发出后 2 分钟内撤回.
 * [Bot] 撤回群员的消息需要管理员权限, 可在任意时间撤回.
 *
 * @throws PermissionDeniedException 当 [Bot] 无权限操作时
 * @see Mirai.recall
 */
@JvmSynthetic
public suspend inline fun Mirai.recall(bot: Bot, message: MessageChain): Unit =
    this.recall(bot, message.source)

internal expect fun findMiraiInstance(): Mirai