@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "FunctionName")

package net.mamoe.mirai

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.OutputStream
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.use
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.GroupNotFoundException
import net.mamoe.mirai.utils.LoginFailedException
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.transferTo

/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * @see Contact
 */
abstract class Bot : CoroutineScope {
    @UseExperimental(MiraiInternalAPI::class)
    companion object {
        inline fun forEachInstance(block: (Bot) -> Unit) = BotImpl.forEachInstance(block)

        fun instanceWhose(qq: Long): Bot = BotImpl.instanceWhose(qq = qq)
    }

    /**
     * 账号信息
     */
    abstract val account: BotAccount

    /**
     * 日志记录器
     */
    abstract val logger: MiraiLogger

    // region contacts

    /**
     * 与这个机器人相关的 QQ 列表. 机器人与 QQ 不一定是好友
     */
    abstract val qqs: ContactList<QQ>

    /**
     * 获取缓存的 QQ 对象. 若没有对应的缓存, 则会线程安全地创建一个.
     */
    abstract fun getQQ(id: Long): QQ

    /**
     * 与这个机器人相关的群列表. 机器人不一定是群成员.
     */
    abstract val groups: ContactList<Group>

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 [id] 无效, 将会抛出 [GroupNotFoundException]
     */
    abstract suspend fun getGroup(id: GroupId): Group

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 [internalId] 无效, 将会抛出 [GroupNotFoundException]
     */
    abstract suspend fun getGroup(internalId: GroupInternalId): Group

    /**
     * 获取缓存的群对象. 若没有对应的缓存, 则会线程安全地创建一个.
     * 若 [id] 无效, 将会抛出 [GroupNotFoundException]
     */
    abstract suspend fun getGroup(id: Long): Group

    // endregion

    // region network

    /**
     * 网络模块
     */
    abstract val network: BotNetworkHandler

    /**
     * 登录
     *
     * @throws LoginFailedException
     */
    abstract suspend fun login()
    // endregion


    // region actions

    abstract suspend fun Image.getLink(): ImageLink

    suspend fun Image.downloadAsByteArray(): ByteArray = getLink().downloadAsByteArray()

    suspend fun Image.download(): ByteReadPacket = getLink().download()

    /**
     * 添加一个好友
     *
     * @param message 若需要验证请求时的验证消息.
     * @param remark 好友备注
     */
    abstract suspend fun addFriend(id: Long, message: String? = null, remark: String? = null): AddFriendResult

    /**
     * 同意来自陌生人的加好友请求
     */
    abstract suspend fun approveFriendAddRequest(id: Long, remark: String?)

    // endregion

    abstract fun dispose(throwable: Throwable?)

    // region extensions

    fun Int.qq(): QQ = getQQ(this.toLong())
    fun Long.qq(): QQ = getQQ(this)

    suspend inline fun Int.group(): Group = getGroup(this.toLong())
    suspend inline fun Long.group(): Group = getGroup(this)
    suspend inline fun GroupInternalId.group(): Group = getGroup(this)
    suspend inline fun GroupId.group(): Group = getGroup(this)


    /**
     * 需要调用者自行 close [output]
     */
    @UseExperimental(KtorExperimentalAPI::class)
    suspend inline fun Image.downloadTo(output: OutputStream) =
        download().use { input -> input.transferTo(output) }

    // endregion
}