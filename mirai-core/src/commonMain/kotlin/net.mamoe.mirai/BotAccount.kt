@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

data class BotAccount(
    val id: UInt,
    val password: String
) {
    constructor(id: Long, password: String) : this(id.toUInt(), password)
}