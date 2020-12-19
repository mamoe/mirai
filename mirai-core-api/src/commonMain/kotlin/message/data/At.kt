/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MessageUtils")

@file:Suppress("EXPERIMENTAL_API_USAGE", "NOTHING_TO_INLINE")

package net.mamoe.mirai.message.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.LowLevelApi
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.UserOrBot
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.utils.PlannedRemoval


/**
 * At 一个群成员. 只能发送给一个群.
 *
 * ## mirai 码支持
 * 格式: &#91;mirai:at:*[target]*&#93;
 *
 * @see AtAll 全体成员
 */
@Serializable
public data class At(
    public val target: Long,
) : MessageContent, CodableMessage {
    public override fun toString(): String = "[mirai:at:$target]"
    public override fun contentToString(): String = "@$target"

    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Use getDisplay", ReplaceWith("this.getDisplay()"), DeprecationLevel.ERROR)
    @PlannedRemoval("2.0-M2")
    val display: Nothing
        get() = error("At.display is no longer supported")

    /**
     * 获取 [At] 发送于指定 [Group] 时会显示的内容.
     *
     * 若 [group] 非 `null` 且包含成员 [target], 返回 `"@成员名片或昵称"`. 否则返回 `"@123456"` 其中 123456 表示 [target]
     */
    public fun getDisplay(group: Group?): String {
        val member = group?.get(this.target) ?: return "@$target"
        return "@${member.nameCardOrNick}"
    }

    public companion object {
        /**
         * 构造一个 [At], 仅供内部使用, 否则可能造成消息无法发出的问题.
         */
        @Suppress("FunctionName", "UNUSED_PARAMETER")
        @JvmStatic
        @LowLevelApi
        @Deprecated("Use constructor instead", ReplaceWith("At(target)", "net.mamoe.mirai.message.data.At"))
        @PlannedRemoval("2.0-M2")
        public fun _lowLevelConstructAtInstance(target: Long, display: String): At = At(target)
    }

    // 自动为消息补充 " "
    public override fun followedBy(tail: Message): MessageChain {
        if (tail is PlainText && tail.content.startsWith(' ')) {
            return super<MessageContent>.followedBy(tail)
        }
        return super<MessageContent>.followedBy(PlainText(" ")) + tail
    }
}

/**
 * 构造 [At]
 *
 * @see At
 * @see Member.at
 */
@JvmSynthetic
public inline fun At(user: UserOrBot): At = At(user.id)

/**
 * At 这个成员
 */
@JvmSynthetic
public inline fun Member.at(): At = At(this)