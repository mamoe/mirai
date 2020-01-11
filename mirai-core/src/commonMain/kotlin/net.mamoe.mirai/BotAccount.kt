@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.md5
import kotlin.annotation.AnnotationTarget.*

data class BotAccount(
    /**
     * **注意**: 在 Android 协议, 总是使用 `QQAndroidClient.uin` 或 [Bot.uin], 而不要使用 [BotAccount.id]. 将来 [BotAccount.id] 可能会变为 [String]
     */
    @MiraiExperimentalAPI
    val id: Long,
    val passwordMd5: ByteArray // md5
){
    constructor(id: Long, passwordPlainText: String) : this(id, md5(passwordPlainText.toByteArray()))
}

/**
 * 标记直接访问 [BotAccount.id], 而不是访问 [Bot.uin]. 这将可能会不兼容未来的 API 修改.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@Experimental
annotation class RawAccountIdUse