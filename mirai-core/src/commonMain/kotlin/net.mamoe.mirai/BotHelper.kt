@file:JvmMultifileClass
@file:JvmName("BotHelperKt")
@file:Suppress("unused", "EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import net.mamoe.mirai.utils.BotConfiguration
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/*
 * 在 [Bot] 中的方法的捷径
 */

//Contacts
/**
 * 使用默认的配置 ([BotConfiguration.Default]) 登录, 返回 [this]
 */
suspend inline fun Bot.alsoLogin(configuration: BotConfiguration = BotConfiguration.Default): Bot =
    apply { login(configuration) }

/**
 * 使用在默认配置基础上修改的配置进行登录, 返回 [this]
 */
@UseExperimental(ExperimentalContracts::class)
suspend inline fun Bot.alsoLogin(configuration: BotConfiguration.() -> Unit): Bot {
    contract {
        callsInPlace(configuration, InvocationKind.EXACTLY_ONCE)
    }
    this.login(configuration)
    return this
}

/**
 * 取得机器人的 QQ 号
 */
inline val Bot.qqAccount: Long get() = this.account.id