/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.LowLevelAPI
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineGroupImage
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.OverFileSizeMaxException
import net.mamoe.mirai.utils.SinceMirai

/**
 * 群. 在 QQ Android 中叫做 "Troop"
 */
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
     * 群设置
     */
    actual abstract val settings: GroupSettings

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
     * [Bot] 在群内的 [newMember] 实例
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
    @LowLevelAPI
    @MiraiExperimentalAPI("dangerous")
    actual abstract fun newMember(memberInfo: MemberInfo): Member

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     *
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    @JvmSynthetic
    actual abstract override suspend fun sendMessage(message: Message): MessageReceipt<Group>

    /**
     * 上传一个图片以备发送.
     *
     * @see BeforeImageUploadEvent 图片发送前事件, cancellable
     * @see ImageUploadEvent 图片发送完成事件
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时. (最大大小约为 20 MB)
     */
    @JvmSynthetic
    actual abstract override suspend fun uploadImage(image: ExternalImage): OfflineGroupImage

    actual companion object {
        actual fun calculateGroupUinByGroupCode(groupCode: Long): Long =
            CommonGroupCalculations.calculateGroupUinByGroupCode(groupCode)

        actual fun calculateGroupCodeByGroupUin(groupUin: Long): Long =
            CommonGroupCalculations.calculateGroupCodeByGroupUin(groupUin)
    }

    @MiraiExperimentalAPI
    actual fun toFullString(): String {
        return "Group(id=${this.id}, name=$name, owner=${owner.id}, members=${members.idContentString})"
    }
}