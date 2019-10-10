package net.mamoe.mirai.utils

/**
 * @author Him188moe
 */
data class BotAccount(
        val qqNumber: Long,//实际上是 UInt
        val password: String//todo 不保存 password?
)