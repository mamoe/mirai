/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OfflineGroupImage
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.OverFileSizeMaxException

/**
 * 群. 在 QQ Android 中叫做 "Troop"
 */
@Suppress("INAPPLICABLE_JVM_NAME")
actual abstract class Group : Contact(), CoroutineScope {
    /**
     * 群名称.
     *
     * 在修改时将会异步上传至服务器.
     * 频繁修改可能会被服务器拒绝.
     *
     * @see MemberPermissionChangeEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    actual abstract var name: String

    /**
     * 入群公告, 没有时为空字符串.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupEntranceAnnouncementChangeEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    actual abstract var entranceAnnouncement: String

    /**
     * 全体禁言状态. `true` 为开启.
     *
     * 当前仅能修改状态.
     *
     * @see GroupMuteAllEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    actual abstract var isMuteAll: Boolean

    /**
     * 坦白说状态. `true` 为允许.
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupAllowConfessTalkEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    actual abstract var isConfessTalkEnabled: Boolean

    /**
     * 允许群员邀请好友入群的状态. `true` 为允许
     *
     * 在修改时将会异步上传至服务器.
     *
     * @see GroupAllowMemberInviteEvent
     * @throws PermissionDeniedException 无权限修改时将会抛出异常
     */
    actual abstract var isAllowMemberInvite: Boolean

    /**
     * 自动加群审批
     */
    actual abstract val isAutoApproveEnabled: Boolean

    /**
     * 匿名聊天
     */
    actual abstract val isAnonymousChatEnabled: Boolean

    /**
     * 同为 groupCode, 用户看到的群号码.
     */
    actual abstract override val id: Long

    /**
     * 群主.
     *
     * @return 若机器人是群主, 返回 [botAsMember]. 否则返回相应的成员
     */
    actual abstract val owner: Member

    /**
     * [Bot] 在群内的 [Member] 实例
     */
    @MiraiExperimentalAPI
    actual abstract val botAsMember: Member

    /**
     * 机器人被禁言还剩余多少秒
     *
     * @see BotMuteEvent 机器人被禁言事件
     * @see isBotMuted 判断机器人是否正在被禁言
     */
    actual abstract val botMuteRemaining: Int

    /**
     * 机器人在这个群里的权限
     *
     * @see Group.checkBotPermission 检查 [Bot] 在这个群里的权限
     * @see Group.checkBotPermissionOperator 要求 [Bot] 在这个群里的权限为 [管理员或群主][MemberPermission.isOperator]
     *
     * @see BotGroupPermissionChangeEvent 机器人群员修改
     */
    actual abstract val botPermission: MemberPermission

    /**
     * 群头像下载链接.
     */
    actual val avatarUrl: String
        get() = "https://p.qlogo.cn/gh/$id/${id}_1/640"

    /**
     * 群成员列表, 不含机器人自己, 含群主.
     * 在 [Group] 实例创建的时候查询一次. 并与事件同步事件更新
     */
    actual abstract val members: ContactList<Member>

    /**
     * 获取群成员实例. 不存在时抛出 [kotlin.NoSuchElementException]
     */
    actual abstract operator fun get(id: Long): Member

    /**
     * 获取群成员实例, 不存在则 null
     */
    actual abstract fun getOrNull(id: Long): Member?


    /**
     * 检查此 id 的群成员是否存在
     */
    actual abstract operator fun contains(id: Long): Boolean

    /**
     * 让机器人退出这个群. 机器人必须为非群主才能退出. 否则将会失败
     */
    @MiraiExperimentalAPI("还未支持")
    actual abstract suspend fun quit(): Boolean

    /**
     * 构造一个 [Member].
     * 非特殊情况请不要使用这个函数. 优先使用 [get].
     */
    @JvmName("newMember")
    @Suppress("INAPPLICABLE_JVM_NAME", "FunctionName")
    @MiraiExperimentalAPI("dangerous")
    actual abstract fun Member(memberInfo: MemberInfo): Member

    /**
     * 向这个对象发送消息.
     *
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws IllegalStateException 发送群消息时若 [Bot] 被禁言抛出
     *
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    @JvmName("sendMessageSuspend")
    @JvmSynthetic
    actual abstract override suspend fun sendMessage(message: Message): MessageReceipt<Group>

    @JvmName("sendMessageSuspend")
    @Deprecated("for binary compatibility", level = DeprecationLevel.HIDDEN)
    @JvmSynthetic
    actual abstract override suspend fun sendMessage(message: MessageChain): MessageReceipt<Group>

    /**
     * 上传一个图片以备发送.
     *
     * @see BeforeImageUploadEvent 图片发送前事件, cancellable
     * @see ImageUploadEvent 图片发送完成事件
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时. (最大大小约为 20 MB)
     */
    @JvmName("uploadImageSuspend")
    @JvmSynthetic
    actual abstract override suspend fun uploadImage(image: ExternalImage): OfflineGroupImage

    actual companion object {
        /**
         * by @kar98k
         */
        actual fun calculateGroupUinByGroupCode(groupCode: Long): Long {
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

        actual fun calculateGroupCodeByGroupUin(groupUin: Long): Long {
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

    @MiraiExperimentalAPI
    actual fun toFullString(): String {
        return "Group(id=${this.id}, name=$name, owner=${owner.id}, members=${members.idContentString})"
    }

}