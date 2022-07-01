/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.active

/**
 * 群荣誉标志
 */
public enum class GroupHonorFlag(private val value: Int) {
    TALKATIVE(1),       // 龙王
    PERFORMER(2),       // 群聊之火
    LEGEND(3),          // 群聊炽焰
    STRONG_NEWBIE(5),   // 冒尖小春笋
    EMOTION(6);         // 快乐源泉

    public companion object {
        @JvmStatic
        public fun deserializeFromInt(value: Int): GroupHonorFlag? = values().find { it.value == value }
    }
}