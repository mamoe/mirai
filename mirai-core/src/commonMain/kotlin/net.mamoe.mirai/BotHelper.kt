@file:JvmMultifileClass
@file:JvmName("BotHelperKt")
@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import net.mamoe.mirai.utils.LoginFailedException
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/*
 * 在 [Bot] 中的方法的捷径
 */

//Contacts
/**
 * 登录, 返回 [this]
 *
 * @throws LoginFailedException
 */
suspend inline fun Bot.alsoLogin(): Bot = also { login() }

/**
 * 取得机器人的 QQ 号
 */
@Deprecated(message = "Use this.uin instead", replaceWith = ReplaceWith("this.uin"), level = DeprecationLevel.WARNING)
inline val Bot.qqAccount: Long
    get() = this.uin