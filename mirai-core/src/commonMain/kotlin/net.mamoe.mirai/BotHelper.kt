/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotHelperKt")
@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/*
 * 在 [Bot] 中的方法的捷径
 */

//Contacts
/**
 * 登录, 返回 [this]
 */
suspend inline fun <B: Bot> B.alsoLogin(): B = also { login() }

/**
 * 取得机器人的 QQ 号
 */
@Deprecated(message = "Use this.uin instead", replaceWith = ReplaceWith("this.uin"), level = DeprecationLevel.WARNING)
inline val Bot.qqAccount: Long
    get() = this.uin