/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.MiraiExperimentalAPI

/**
 * QQ 对象.
 * 注意: 一个 [QQ] 实例并不是独立的, 它属于一个 [Bot].
 * 它不能被直接构造. 任何时候都应从 [Bot.getFriend] 或事件中获取.
 *
 * 对于同一个 [Bot] 任何一个人的 [QQ] 实例都是单一的.
 *
 * A QQ instance helps you to receive event from or sendPacket event to.
 * Notice that, one QQ instance belong to one [Bot], that is, QQ instances from different [Bot] are NOT the same.
 *
 * @author Him188moe
 */
interface QQ : Contact, CoroutineScope {
    /**
     * QQ 号码
     */
    override val id: Long

    /**
     * 昵称
     */
    val nick: String

    /**
     * 请求头像下载链接
     */
    // @MiraiExperimentalAPI
    //suspend fun queryAvatar(): AvatarLink

    /**
     * 查询用户资料
     */
    @MiraiExperimentalAPI("还未支持")
    suspend fun queryProfile(): Profile

    /**
     * 查询曾用名.
     *
     * 曾用名可能是:
     * - 昵称
     * - 共同群内的群名片
     */
    @MiraiExperimentalAPI("还未支持")
    suspend fun queryPreviousNameList(): PreviousNameList

    /**
     * 查询机器人账号给这个人设置的备注
     */
    @MiraiExperimentalAPI("还未支持")
    suspend fun queryRemark(): FriendNameRemark

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
    override suspend fun sendMessage(message: MessageChain): MessageReceipt<QQ>
}