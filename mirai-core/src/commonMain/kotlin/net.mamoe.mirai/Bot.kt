@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "FunctionName")

package net.mamoe.mirai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.io.OutputStream
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.use
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.LoginFailedException
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.WeakRef
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
    @MiraiInternalAPI
    abstract val account: BotAccount

    /**
     * QQ 号码. 实际类型为 uint
     */
    abstract val uin: Long

    /**
     * 日志记录器
     */
    abstract val logger: MiraiLogger

    // region contacts

    /**
     * 机器人的好友列表.
     */
    abstract val qqs: ContactList<QQ>

    /**
     * 获取一个好友对象. 若没有这个好友, 则会抛出异常[NoSuchElementException]
     */
    abstract fun getQQ(id: Long): QQ

    /**
     * 构造一个 [QQ] 对象. 它持有对 [Bot] 的弱引用([WeakRef]).
     *
     * [Bot] 无法管理这个对象, 但这个对象会以 [Bot] 的 [Job] 作为父 Job.
     * 因此, 当 [Bot] 被关闭后, 这个对象也会被关闭.
     */
    abstract fun QQ(id: Long): QQ

    /**
     * 机器人加入的群列表.
     */
    abstract val groups: ContactList<Group>

    /**
     * 获取一个机器人加入的群. 若没有这个群, 则会抛出异常 [NoSuchElementException]
     */
    abstract fun getGroupByID(id: Long): Group

    /**
     * 获取一个机器人加入的群. 若没有这个群, 则会抛出异常 [NoSuchElementException]
     */
    abstract fun getGroupByGroupCode(groupCode: Long): Group

    // 目前还不能构造群对象. 这将在以后支持

    // endregion

    // region network

    /**
     * 网络模块
     */
    abstract val network: BotNetworkHandler

    /**
     * 登录, 或重新登录.
     * 不建议调用这个函数.
     *
     * 最终调用 [net.mamoe.mirai.network.BotNetworkHandler.login]
     *
     * @throws LoginFailedException
     */
    abstract suspend fun login()
    // endregion


    // region actions

    abstract suspend fun Image.downloadAsByteArray(): ByteArray

    abstract suspend fun Image.download(): ByteReadPacket

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

    /**
     * 关闭这个 [Bot], 停止一切相关活动. 不可重新登录.
     */
    abstract fun dispose(throwable: Throwable?)

    // region extensions

    fun Int.qq(): QQ = getQQ(this.toLong())
    fun Long.qq(): QQ = getQQ(this)


    /**
     * 需要调用者自行 close [output]
     */
    suspend inline fun Image.downloadTo(output: OutputStream) =
        download().use { input -> input.transferTo(output) }

    // endregion
}