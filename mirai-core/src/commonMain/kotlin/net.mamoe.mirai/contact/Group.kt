/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "UnusedImport")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.JavaFriendlyAPI
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.isContentEmpty
import net.mamoe.mirai.message.data.toMessage
import net.mamoe.mirai.message.recall
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.internal.runBlocking
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * 群.
 */
abstract class Group : Contact(), CoroutineScope {
    /**
     * 群名称.
     *
     * 在修改时将会异步上传至服务器, 也会广播事件 [GroupNameChangeEvent].
     * 频繁修改可能会被服务器拒绝.
     *
     * @see GroupNameChangeEvent 群名片修改事件
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    abstract var name: String

    /**
     * 群设置
     */
    abstract val settings: GroupSettings

    /**
     * 同为 groupCode, 用户看到的群号码.
     */
    abstract override val id: Long

    /**
     * 群主.
     *
     * @return 若机器人是群主, 返回 [botAsMember]. 否则返回相应的成员
     */
    abstract val owner: Member

    /**
     * [Bot] 在群内的 [Member] 实例
     */
    @MiraiExperimentalAPI
    abstract val botAsMember: Member

    /**
     * 机器人被禁言还剩余多少秒
     *
     * @see BotMuteEvent 机器人被禁言事件
     * @see isBotMuted 判断机器人是否正在被禁言
     */
    abstract val botMuteRemaining: Int

    /**
     * 机器人在这个群里的权限
     *
     * @see Group.checkBotPermission 检查 [Bot] 在这个群里的权限
     *
     * @see BotGroupPermissionChangeEvent 机器人群员修改
     */
    abstract val botPermission: MemberPermission

    /**
     * 群头像下载链接.
     */
    val avatarUrl: String
        get() = "https://p.qlogo.cn/gh/$id/${id}/640"

    /**
     * 群成员列表, 不含机器人自己, 含群主.
     * 在 [Group] 实例创建的时候查询一次. 并与事件同步事件更新
     */
    abstract val members: ContactList<Member>

    /**
     * 获取群成员实例. 不存在时抛出 [kotlin.NoSuchElementException]
     * 当 [id] 为 [Bot.id] 时返回 [botAsMember]
     */
    @Throws(NoSuchElementException::class)
    abstract operator fun get(id: Long): Member

    /**
     * 获取群成员实例, 不存在则 null
     * 当 [id] 为 [Bot.id] 时返回 [botAsMember]
     */
    abstract fun getOrNull(id: Long): Member?

    /**
     * 检查此 id 的群成员是否存在
     * 当 [id] 为 [Bot.id] 时返回 `true`
     */
    abstract operator fun contains(id: Long): Boolean


    /**
     * 让机器人退出这个群.
     * @throws IllegalStateException 当机器人为群主时
     * @return 退出成功时 true; 已经退出时 false
     */
    @JvmSynthetic
    abstract suspend fun quit(): Boolean

    /**
     * 构造一个 [Member].
     * 非特殊情况请不要使用这个函数. 优先使用 [get].
     */
    @LowLevelAPI
    @MiraiExperimentalAPI("dangerous")
    abstract fun newMember(memberInfo: MemberInfo): Member

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
    @JvmSynthetic
    abstract override suspend fun sendMessage(message: Message): MessageReceipt<Group>

    /**
     * @see sendMessage
     */
    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "VIRTUAL_MEMBER_HIDDEN", "OVERRIDE_BY_INLINE")
    @kotlin.internal.InlineOnly
    @JvmSynthetic
    suspend inline fun sendMessage(message: String): MessageReceipt<Group> {
        return sendMessage(message.toMessage())
    }

    /**
     * 上传一个图片以备发送.
     *
     * @see Image 查看有关图片的更多信息, 如上传图片
     *
     * @see BeforeImageUploadEvent 图片上传前事件, cancellable
     * @see ImageUploadEvent 图片上传完成事件
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时. (最大大小约为 20 MB)
     */
    @JvmSynthetic
    abstract override suspend fun uploadImage(image: ExternalImage): Image

    companion object {
        /**
         * 使用 groupCode 计算 groupUin. 这两个值仅在 mirai 内部协议区分, 一般人使用时无需在意.
         * @suppress internal api
         */
        @MiraiExperimentalAPI
        @JvmStatic
        fun calculateGroupUinByGroupCode(groupCode: Long): Long =
            CommonGroupCalculations.calculateGroupUinByGroupCode(groupCode)

        /**
         * 使用 groupUin 计算 groupCode. 这两个值仅在 mirai 内部协议区分, 一般人使用时无需在意.
         * @suppress internal api
         */
        @MiraiExperimentalAPI
        @JvmStatic
        fun calculateGroupCodeByGroupUin(groupUin: Long): Long =
            CommonGroupCalculations.calculateGroupCodeByGroupUin(groupUin)
    }

    /**
     * @see quit
     */
    @Suppress("FunctionName")
    @JvmName("quit")
    @JavaFriendlyAPI
    fun __quitBlockingForJava__(): Boolean = runBlocking { quit() }
}

/**
 * 群设置
 *
 * @see Group.settings 获取群设置
 */
interface GroupSettings {
    /**
     * 入群公告, 没有时为空字符串.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupEntranceAnnouncementChangeEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    var entranceAnnouncement: String

    /**
     * 全体禁言状态. `true` 为开启.
     *
     * 当前仅能修改状态.
     *
     * @see GroupMuteAllEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    var isMuteAll: Boolean

    /**
     * 坦白说状态. `true` 为允许.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupAllowConfessTalkEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    @PlannedRemoval("1.3.0")
    @Deprecated("mirai 将不再支持此用例较少的设置", level = DeprecationLevel.WARNING)
    var isConfessTalkEnabled: Boolean

    /**
     * 允许群员邀请好友入群的状态. `true` 为允许
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupAllowMemberInviteEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    var isAllowMemberInvite: Boolean

    /**
     * 自动加群审批
     */
    @MiraiExperimentalAPI
    val isAutoApproveEnabled: Boolean

    /**
     * 匿名聊天
     */
    val isAnonymousChatEnabled: Boolean
}


/**
 * 返回机器人是否正在被禁言
 *
 * @see Group.botMuteRemaining 剩余禁言时间
 */
inline val Group.isBotMuted: Boolean get() = this.botMuteRemaining != 0


internal object CommonGroupCalculations {
    /**
     * by @kar98k
     */
    fun calculateGroupUinByGroupCode(groupCode: Long): Long {
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

    fun calculateGroupCodeByGroupUin(groupUin: Long): Long {
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
}
