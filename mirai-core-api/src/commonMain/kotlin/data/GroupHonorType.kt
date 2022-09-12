/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data


import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.event.events.MemberHonorChangeEvent
import kotlin.jvm.JvmInline
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
@JvmInline
@Serializable
public value class GroupHonorType internal constructor(public val value: Int) {
    public companion object {
        @JvmStatic
        public val TALKATIVE: GroupHonorType = GroupHonorType(1)
        @JvmStatic
        public val PERFORMER: GroupHonorType = GroupHonorType(2)
        @JvmStatic
        public val LEGEND: GroupHonorType = GroupHonorType(3)
        @JvmStatic
        public val STRONG_NEWBIE: GroupHonorType = GroupHonorType(5)
        @JvmStatic
        public val EMOTION: GroupHonorType = GroupHonorType(6)

        @JvmStatic
        public val BRONZE: GroupHonorType = GroupHonorType(7)
        @JvmStatic
        public val SILVER: GroupHonorType = GroupHonorType(8)
        @JvmStatic
        public val GOLDEN: GroupHonorType = GroupHonorType(9)
        @JvmStatic
        public val WHIRLWIND: GroupHonorType = GroupHonorType(10)

        @JvmStatic
        public val RICHER: GroupHonorType = GroupHonorType(11)
        @JvmStatic
        public val RED_PACKET: GroupHonorType = GroupHonorType(14)
    }
}