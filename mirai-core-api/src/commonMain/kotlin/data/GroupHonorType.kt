/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data


import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.MemberActive
import net.mamoe.mirai.event.events.MemberHonorChangeEvent
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.jvm.JvmStatic

/**
 * 群荣誉信息
 * @property TALKATIVE 龙王
 * @property PERFORMER 群聊之火
 * @property LEGEND 群聊炽焰
 * @property STRONG_NEWBIE 冒尖小春笋
 * @property EMOTION 快乐源泉
 * @property BRONZE 学术新星
 * @property SILVER 顶尖学霸
 * @property GOLDEN 至尊学神
 * @property WHIRLWIND 一笔当先
 * @property RICHER 壕礼皇冠
 * @property RED_PACKET 善财福禄寿
 * @see GroupActive
 * @see MemberActive
 * @see MemberHonorChangeEvent
 */
@MiraiExperimentalApi
public enum class GroupHonorType(public val value: Int) {
    TALKATIVE(1),
    PERFORMER(2),
    LEGEND(3),
    STRONG_NEWBIE(5),
    EMOTION(6),

    BRONZE(7),
    SILVER(8),
    GOLDEN(9),
    WHIRLWIND(10),

    RICHER(11),
    RED_PACKET(14);

    internal companion object {
        @JvmStatic
        private val values = values()

        @JvmStatic
        internal fun deserializeFromInt(value: Int): GroupHonorType = values.first { it.value == value }
    }
}