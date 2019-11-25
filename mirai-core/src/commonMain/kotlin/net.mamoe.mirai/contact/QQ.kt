@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.data.Profile
import net.mamoe.mirai.network.BotSession

/**
 * QQ 对象.
 * 注意: 一个 [QQ] 实例并不是独立的, 它属于一个 [Bot].
 * 它不能被直接构造. 任何时候都应从 [Bot.qq], [Bot.ContactSystem.getQQ], [BotSession.qq] 或事件中获取.
 *
 * 对于同一个 [Bot] 任何一个人的 [QQ] 实例都是单一的.
 *
 * A QQ instance helps you to receive event from or sendPacket event to.
 * Notice that, one QQ instance belong to one [Bot], that is, QQ instances from different [Bot] are NOT the same.
 *
 * @author Him188moe
 */
interface QQ : Contact {
    /**
     * 用户资料.
     * 第一次获取时将会向服务器查询.
     * 第一次以后则是直接得到一个用 [CompletableDeferred] 包装的值
     */
    val profile: Deferred<Profile>

    /**
     * 更新个人资料.
     * 将会同步更新 property [profile]
     */
    suspend fun updateProfile(): Profile
}