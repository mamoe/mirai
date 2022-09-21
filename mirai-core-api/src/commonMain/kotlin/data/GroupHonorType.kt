/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.data


import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.active.GroupActive
import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.data.GroupHonorType.Companion.BRONZE
import net.mamoe.mirai.data.GroupHonorType.Companion.EMOTION
import net.mamoe.mirai.data.GroupHonorType.Companion.GOLDEN
import net.mamoe.mirai.data.GroupHonorType.Companion.LEGEND
import net.mamoe.mirai.data.GroupHonorType.Companion.PERFORMER
import net.mamoe.mirai.data.GroupHonorType.Companion.RED_PACKET
import net.mamoe.mirai.data.GroupHonorType.Companion.RICHER
import net.mamoe.mirai.data.GroupHonorType.Companion.SILVER
import net.mamoe.mirai.data.GroupHonorType.Companion.STRONG_NEWBIE
import net.mamoe.mirai.data.GroupHonorType.Companion.TALKATIVE
import net.mamoe.mirai.data.GroupHonorType.Companion.WHIRLWIND
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
public value class GroupHonorType public constructor(public val id: Int) { // public for potential usages from Java. little compatibility burden.
    public companion object {
        // ID fields

        /**
         * 龙王
         * @see TALKATIVE
         */
        public const val TALKATIVE_ID: Int = 1

        /**
         * 群聊之火
         * @see PERFORMER
         */
        public const val PERFORMER_ID: Int = 2

        /**
         * 群聊炽焰
         * @see LEGEND
         */
        public const val LEGEND_ID: Int = 3

        /**
         * 冒尖小春笋
         * @see STRONG_NEWBIE
         */
        public const val STRONG_NEWBIE_ID: Int = 4

        /**
         * 快乐源泉
         * @see EMOTION
         */
        public const val EMOTION_ID: Int = 5

        /**
         * 学术新星
         * @see BRONZE
         */
        public const val BRONZE_ID: Int = 6

        /**
         * 顶尖学霸
         * @see SILVER
         */
        public const val SILVER_ID: Int = 7

        /**
         * 至尊学神
         * @see GOLDEN
         */
        public const val GOLDEN_ID: Int = 8

        /**
         * 一笔当先
         * @see WHIRLWIND
         */
        public const val WHIRLWIND_ID: Int = 9

        /**
         * 壕礼皇冠
         * @see RICHER
         */
        public const val RICHER_ID: Int = 10

        /**
         * 善财福禄寿
         * @see RED_PACKET
         */
        public const val RED_PACKET_ID: Int = 11

        // Inline class 'instance's, invisible from Java.

        /**
         * 龙王
         */
        @JvmStatic
        public val TALKATIVE: GroupHonorType = GroupHonorType(TALKATIVE_ID)

        /**
         * 群聊之火
         */
        @JvmStatic
        public val PERFORMER: GroupHonorType = GroupHonorType(PERFORMER_ID)

        /**
         * 群聊炽焰
         */
        @JvmStatic
        public val LEGEND: GroupHonorType = GroupHonorType(LEGEND_ID)

        /**
         * 冒尖小春笋
         */
        @JvmStatic
        public val STRONG_NEWBIE: GroupHonorType = GroupHonorType(STRONG_NEWBIE_ID)

        /**
         * 快乐源泉
         */
        @JvmStatic
        public val EMOTION: GroupHonorType = GroupHonorType(EMOTION_ID)

        /**
         * 学术新星
         */
        @JvmStatic
        public val BRONZE: GroupHonorType = GroupHonorType(BRONZE_ID)

        /**
         * 顶尖学霸
         */
        @JvmStatic
        public val SILVER: GroupHonorType = GroupHonorType(SILVER_ID)

        /**
         * 至尊学神
         */
        @JvmStatic
        public val GOLDEN: GroupHonorType = GroupHonorType(GOLDEN_ID)

        /**
         * 一笔当先
         */
        @JvmStatic
        public val WHIRLWIND: GroupHonorType = GroupHonorType(WHIRLWIND_ID)

        /**
         * 壕礼皇冠
         */
        @JvmStatic
        public val RICHER: GroupHonorType = GroupHonorType(RICHER_ID)

        /**
         * 善财福禄寿
         */
        @JvmStatic
        public val RED_PACKET: GroupHonorType = GroupHonorType(RED_PACKET_ID)
    }
}