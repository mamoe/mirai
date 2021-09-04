/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.roaming

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.utils.JavaFriendlyAPI
import net.mamoe.mirai.utils.runBIO

/**
 * 漫游消息授权器. 用于在查询漫游消息时获取独立密码.
 *
 * @see RoamingSupported
 * @since 2.8
 */
public interface RoamingAuthenticator {
    /**
     * 获取漫游消息独立密码. 注意, 该密码不会保存, 而每次查询漫游消息时都需要此密码. 请自行考虑如何实现密码缓存.
     */
    @JvmBlockingBridge
    public suspend fun authenticate(bot: Bot, contact: Contact): String
}

/**
 * 供 Java 实现的 [RoamingAuthenticator].
 *
 * @see RoamingAuthenticator
 * @since 2.8
 */
@JavaFriendlyAPI
public abstract class JavaRoamingAuthenticator : RoamingAuthenticator {
    /**
     * 获取漫游消息独立密码. 注意, 该密码不会保存, 而每次查询漫游消息时都需要此密码. 请自行考虑如何实现密码缓存.
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("authenticate")
    public abstract fun authenticateJava(bot: Bot, contact: Contact): String // 'overrides' blocking bridge

    // no @JvmBlockingBridge
    public final override suspend fun authenticate(bot: Bot, contact: Contact): String {
        return runBIO { authenticateJava(bot, contact) }
    }
}