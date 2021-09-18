/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact.roaming

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.contact.Contact

/**
 * 支持查询漫游消息记录的 [Contact].
 * @since 2.8
 */
public interface RoamingSupported : Contact {
    /**
     * 获取漫游消息记录管理器.
     */
    public val roamingMessages: RoamingMessages
}