@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.WeakRefProperty


/**
 * 联系人. 虽然叫做联系人, 但他的子类有 [QQ] 和 [群][Group].
 *
 * @author Him188moe
 */
interface Contact : CoroutineScope {
    /**
     * 这个联系人所属 [Bot].
     */
    @WeakRefProperty
    val bot: Bot // weak ref

    /**
     * 可以是 QQ 号码或者群号码.
     *
     * 对于 QQ, `uin` 与 `id` 是相同的意思.
     * 对于 Group, `groupCode` 与 `id` 是相同的意思.
     */
    val id: Long

    /**
     * 向这个对象发送消息.
     */
    suspend fun sendMessage(message: MessageChain)

    /**
     * 上传一个图片以备发送.
     * TODO: 群图片与好友图片之间是否通用还不确定.
     * TODO: 好友之间图片是否通用还不确定.
     */
    suspend fun uploadImage(image: ExternalImage): Image

    /**
     * 判断 `this` 和 [other] 是否是相同的类型, 并且 [id] 相同.
     *
     * 注:
     * [id] 相同的 [Member] 和 [QQ], 他们并不 [equals].
     * 因为, [Member] 含义为群员, 必属于一个群.
     * 而 [QQ] 含义为一个独立的人, 可以是好友, 也可以是陌生人.
     */
    override fun equals(other: Any?): Boolean
}

suspend inline fun Contact.sendMessage(message: Message) = sendMessage(message.toChain())

suspend inline fun Contact.sendMessage(plain: String) = sendMessage(plain.singleChain())