/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "UnusedImport", "NOTHING_TO_INLINE")
@file:JvmBlockingBridge

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.announcement.Announcements
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.DeprecatedSinceMirai
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.NotStableForInheritance
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 群.
 *
 * ## 群员操作
 *
 * ### 获取群成员
 *
 * 使用 [Group.members] 可获取除 [Bot] 自身外的所有群成员 (包括管理员和群主) 列表. [Bot] 自身在群内的对象可通过 [botAsMember] 获取.
 *
 * [get] 可以按 QQ 号码获取一个群成员对象, 在不存在时返回 `null`. [getOrFail] 则在不存在时抛出 [NoSuchElementException].
 *
 * ### 判断管理员权限
 *
 * 首先获取到目标群成员对象, 然后使用 [NormalMember.permission] 获取其权限.
 *
 * [Bot] 在群内的权限可通过 [Group.botPermission] 或其 [Member 对象][botAsMember] 的 [NormalMember.permission] 获取.
 *
 * ### 其他动作
 *
 * - 设置管理员权限: [NormalMember.modifyAdmin]
 * - 禁言: [NormalMember.mute]
 * - 戳一戳: [NormalMember.nudge]
 *
 * ## 群公告
 *
 * 可通过 [Group.announcements] 获取公告支持. 可在 [Announcements] 查看详细文档.
 *
 */
@NotStableForInheritance
public interface Group : Contact, CoroutineScope, FileSupported, AudioSupported {
    /**
     * 群名称.
     *
     * 在修改时将会异步上传至服务器, 也会广播事件 [GroupNameChangeEvent].
     * 频繁修改可能会被服务器拒绝.
     *
     * @see GroupNameChangeEvent 群名片修改事件
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    public var name: String

    /**
     * 群设置
     */
    public val settings: GroupSettings

    /**
     * 同为 groupCode, 用户看到的群号码.
     */
    public override val id: Long

    /**
     * 群主.
     *
     * @return 若机器人是群主, 返回 [botAsMember]. 否则返回相应的成员
     */
    public val owner: NormalMember

    /**
     * [Bot] 在群内的 [Member] 实例
     */
    public val botAsMember: NormalMember

    /**
     * 机器人被禁言还剩余多少秒
     *
     * @see BotMuteEvent 机器人被禁言事件
     * @see isBotMuted 判断机器人是否正在被禁言
     */
    public val botMuteRemaining: Int get() = botAsMember.muteTimeRemaining

    /**
     * 机器人在这个群里的权限
     *
     * @see Group.checkBotPermission 检查 [Bot] 在这个群里的权限
     *
     * @see BotGroupPermissionChangeEvent 机器人群员修改
     */
    public val botPermission: MemberPermission get() = botAsMember.permission

    /**
     * 群头像下载链接, 规格默认为 [AvatarSpec.LARGEST]
     * @see avatarUrl
     */
    public override val avatarUrl: String
        get() = avatarUrl(spec = AvatarSpec.LARGEST)

    /**
     * 群头像下载链接.
     * @param spec 头像的规格.
     * @since 2.11
     */
    public override fun avatarUrl(spec: AvatarSpec): String = "http://p.qlogo.cn/gh/${id}/${id}/${spec.size}"

    /**
     * 群成员列表, 不含机器人自己, 含群主.
     *
     * 在 [Group] 实例创建的时候查询一次. 并与事件同步事件更新.
     */
    public val members: ContactList<NormalMember>

    /**
     * 获取群公告相关功能接口
     * @since 2.7
     */
    public val announcements: Announcements

    /**
     * 获取群成员实例. 不存在时返回 `null`.
     *
     * 当 [id] 为 [Bot.id] 时返回 [botAsMember].
     */
    public operator fun get(id: Long): NormalMember?

    /**
     * 获取群成员实例. 不存在时抛出 [kotlin.NoSuchElementException].
     *
     * 当 [id] 为 [Bot.id] 时返回 [botAsMember].
     */
    public fun getOrFail(id: Long): NormalMember =
        get(id) ?: throw NoSuchElementException("member $id not found in group ${this.id}")


    /**
     * 当本群存在 [Member.id] 为 [id] 的群员时返回 `true`.
     *
     * 当 [id] 为 [Bot.id] 时返回 `true`
     */
    public operator fun contains(id: Long): Boolean

    /**
     * 当 [member] 是本群成员时返回 `true`. 将同时成员 [所属群][Member.group]. 同一个用户在不同群内的 [Member] 对象不相等.
     */
    public operator fun contains(member: NormalMember): Boolean = member in members


    /**
     * 让机器人退出这个群.
     * @throws IllegalStateException 当机器人为群主时
     * @return 退出成功时 true; 已经退出时 false
     */
    public suspend fun quit(): Boolean

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see GroupMessagePreSendEvent 发送消息前事件
     * @see GroupMessagePostSendEvent 发送消息后事件
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     * @throws IllegalArgumentException 当消息内容为空时抛出 (详见 [Message.isContentEmpty])
     *
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    public override suspend fun sendMessage(message: Message): MessageReceipt<Group>

    /**
     * 发送纯文本消息
     * @see sendMessage
     */
    public override suspend fun sendMessage(message: String): MessageReceipt<Group> =
        this.sendMessage(message.toPlainText())


    /**
     * 上传一个语音消息以备发送. 该方法已弃用且将在未来版本删除, 请使用 [uploadAudio].
     */
    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Deprecated(
        "use uploadAudio",
        replaceWith = ReplaceWith("uploadAudio(resource)"),
        level = DeprecationLevel.HIDDEN
    ) // deprecated since 2.7
    @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
    public suspend fun uploadVoice(resource: ExternalResource): net.mamoe.mirai.message.data.Voice

    /**
     * 将一条消息设置为群精华消息, 需要管理员或群主权限.
     * 操作成功返回 `true`.
     *
     * @throws PermissionDeniedException 没有权限时抛出
     *
     * @since 2.2
     */
    public suspend fun setEssenceMessage(source: MessageSource): Boolean

    public companion object {
        /**
         * 将一条消息设置为群精华消息, 需要管理员或群主权限.
         * 操作成功返回 `true`.
         *
         * @throws PermissionDeniedException 没有权限时抛出
         *
         * @see Group.setEssenceMessage
         * @since 2.2
         */
        @JvmBlockingBridge
        @JvmStatic
        public suspend fun Group.setEssenceMessage(chain: MessageChain): Boolean = setEssenceMessage(chain.source)
    }
}

/**
 * 群设置
 *
 * @see Group.settings 获取群设置
 */
public interface GroupSettings {
    /**
     * 入群公告, 没有时为空字符串.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupEntranceAnnouncementChangeEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     * @see Announcements
     * @see Group.announcements
     */
    @Deprecated(
        level = DeprecationLevel.HIDDEN,
        message = "group.announcements.asFlow().filter { it.parameters.sendToNewMember }.firstOrNull()",
    ) // deprecated since 2.7
    @DeprecatedSinceMirai(warningSince = "2.7", errorSince = "2.10", hiddenSince = "2.11")
    public var entranceAnnouncement: String

    /**
     * 全体禁言状态. `true` 为开启.
     *
     * 当前仅能修改状态.
     *
     * @see GroupMuteAllEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    public var isMuteAll: Boolean

    /**
     * 允许群员邀请好友入群的状态. `true` 为允许
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupAllowMemberInviteEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    public var isAllowMemberInvite: Boolean

    /**
     * 自动加群审批
     */
    @MiraiExperimentalApi
    public val isAutoApproveEnabled: Boolean

    /**
     * 匿名聊天
     */
    public var isAnonymousChatEnabled: Boolean
}

/**
 * 同 [get]. 在一些不适合使用 [get] 的情境下使用 [getMember].
 */
@JvmSynthetic
public inline fun Group.getMember(id: Long): NormalMember? = get(id)

/**
 * 同 [getMemberOrFail]. 在一些不适合使用 [getOrFail] 的情境下使用 [getMemberOrFail].
 */
@JvmSynthetic
public inline fun Group.getMemberOrFail(id: Long): NormalMember = getOrFail(id)


/**
 * 返回机器人是否正在被禁言
 *
 * @see Group.botMuteRemaining 剩余禁言时间
 */
public inline val Group.isBotMuted: Boolean get() = this.botMuteRemaining != 0

