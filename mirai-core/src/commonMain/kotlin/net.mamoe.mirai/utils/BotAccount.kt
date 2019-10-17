package net.mamoe.mirai.utils


data class BotAccount(
        val account: Long,//实际上是 UInt
        val password: String//todo 不保存 password?
)