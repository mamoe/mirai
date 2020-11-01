/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.data

/**
 * 在线状态
 */
public enum class OnlineStatus(public val id: Int) {
    /**
     * 我在线上
     */
    ONLINE(11),

    /**
     * 离线
     */
    OFFLINE(21),

    /**
     * 离开
     */
    AWAY(31),

    /**
     * 隐身
     */
    INVISIBLE(41),

    /**
     * 忙碌
     */
    BUSY(50),

    /**
     * Q 我吧
     */
    Q_ME(60),

    /**
     * 请勿打扰
     */
    DND(70),

    /**
     * 离线但接收消息
     */
    RECEIVE_OFFLINE_MESSAGE(95),

    /**
     * 解析错误等
     */
    UNKNOWN(-1);

    public companion object {
        public fun ofId(id: Int): OnlineStatus = values().first { it.id == id }
        public fun ofIdOrNull(id: Int): OnlineStatus? = values().firstOrNull { it.id == id }
    }
}