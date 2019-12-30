@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.utils.md5

data class BotAccount(
    val id: Long,
    val passwordMd5: ByteArray // md5
){
    constructor(id: Long, passwordPlainText: String) : this(id, md5(passwordPlainText.toByteArray()))
}