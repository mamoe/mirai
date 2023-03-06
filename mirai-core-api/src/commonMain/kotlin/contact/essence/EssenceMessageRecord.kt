/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.essence

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 精华消息记录
 * @since 2.15
 * @param group 记录的群聊
 * @param sender 消息的发送者
 * @param senderId 消息的发送者的ID
 * @param senderNick 消息的发送者的Nick
 * @param senderTime 消息的发送的时间 *
 * @param operator 设置精华的操作者
 * @param operatorId 设置精华的操作者的ID
 * @param operatorNick 设置精华的操作者的Nick
 * @param operatorTime 设置精华的时间
 */
public class EssenceMessageRecord @MiraiInternalApi constructor(
    public val group: Group,
    public val sender: NormalMember?,
    public val senderId: Long,
    public val senderNick: String,
    public val senderTime: Int,
    public val operator: NormalMember?,
    public val operatorId: Long,
    public val operatorNick: String,
    public val operatorTime: Int,
    private val loadMessageSource: suspend (parse: Boolean) -> MessageSource
) {
    override fun toString(): String {
        return "EssenceMessageRecord(group=${group}, sender=${senderNick}(${senderId}), senderTime=${senderTime}, operator=${operatorNick}(${operatorId}), operatorTime=${operatorTime})"
    }

    /**
     * 获取消息源
     *
     * 其中的 [MessageSource.originalMessage] 将会尝试以加载为原消息格式
     *
     * **注意** 当精华消息中包含 图片 时，会尝试将其下载然后重新上传, 以保证可用性
     *
     * @see getSource
     */
    @JvmBlockingBridge
    public suspend fun getFullSource(): MessageSource {
        return loadMessageSource(true)
    }

    /**
     * 获取消息源
     *
     * @see getFullSource
     */
    @JvmBlockingBridge
    public suspend fun getSource(): MessageSource {
        return loadMessageSource(false)
    }
}