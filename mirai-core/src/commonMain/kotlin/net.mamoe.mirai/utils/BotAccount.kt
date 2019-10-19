@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils


data class BotAccount(
        val account: UInt,
        val password: String//todo 不保存 password?
)