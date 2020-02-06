@file:Suppress("EXPERIMENTAL_API_USAGE", "unused", "FunctionName")

package net.mamoe.mirai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.io.OutputStream
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import kotlinx.io.core.use
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.transferTo

/**
 * 机器人对象. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 *
 * 注: Bot 为全协程实现, 没有其他任务时若不使用 [awaitDisconnection], 主线程将会退出.
 *
 * @see Contact
 */
abstract class Bot : CoroutineScope {
    @UseExperimental(MiraiInternalAPI::class)
    companion object {
        /**
         * 复制一份此时的 [Bot] 实例列表.
         */
        val instances: List<WeakRef<Bot>> get() = BotImpl.instances.toList()

        /**
         * 遍历每一个 [Bot] 实例
         */
        inline fun forEachInstance(block: (Bot) -> Unit) = BotImpl.forEachInstance(block)

        /**
         * 获取一个 [Bot] 实例, 找不到则 [NoSuchElementException]
         */
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
     * 机器人的好友列表. 它将与服务器同步更新
     */
    abstract val qqs: ContactList<QQ>

    /**
     * 获取一个好友对象. 若没有这个好友, 则会抛出异常 [NoSuchElementException]
     */
    @Deprecated(message = "这个函数有歧义. 它获取的是好友, 却名为 getQQ", replaceWith = ReplaceWith("getFriend(id)"))
    fun getQQ(id: Long): QQ = getFriend(id)

    /**
     * 获取一个好友或一个群.
     * 在一些情况下这可能会造成歧义. 请考虑后使用.
     */
    operator fun get(id: Long): Contact {
        return this.qqs.getOrNull(id) ?: this.groups.getOrNull(id) ?: throw NoSuchElementException("contact id $id")
    }

    /**
     * 判断是否有这个 id 的好友或群.
     * 在一些情况下这可能会造成歧义. 请考虑后使用.
     */
    operator fun contains(id: Long): Boolean {
        return this.qqs.contains(id) || this.groups.contains(id)
    }

    /**
     * 获取一个好友对象. 若没有这个好友, 则会抛出异常 [NoSuchElementException]
     */
    abstract fun getFriend(id: Long): QQ

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
    abstract fun getGroup(id: Long): Group

    // TODO 目前还不能构造群对象. 这将在以后支持

    // endregion

    // region network

    /**
     * 网络模块
     */
    abstract val network: BotNetworkHandler

    /**
     * 挂起直到 [Bot] 下线.
     */
    suspend inline fun awaitDisconnection() = network.awaitDisconnection()

    /**
     * 登录, 或重新登录.
     * 不建议调用这个函数.
     *
     * 最终调用 [net.mamoe.mirai.network.BotNetworkHandler.login]
     */
    abstract suspend fun login()
    // endregion


    // region actions

    @Deprecated("内存使用效率十分低下", ReplaceWith("this.download()"), DeprecationLevel.WARNING)
    abstract suspend fun Image.downloadAsByteArray(): ByteArray

    /**
     * 将图片下载到内存中 (使用 [IoBuffer.Pool])
     */
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
     * 关闭这个 [Bot], 停止一切相关活动. 所有引用都会被释放.
     *
     * 注: 不可重新登录. 必须重新实例化一个 [Bot].
     *
     * @param cause 原因. 为 null 时视为正常关闭, 非 null 时视为异常关闭
     */
    abstract fun close(cause: Throwable? = null)

    // region extensions

    @Deprecated(message = "这个函数有歧义, 将在不久后删除", replaceWith = ReplaceWith("getFriend(this.toLong())"))
    fun Int.qq(): QQ = getFriend(this.toLong())
    @Deprecated(message = "这个函数有歧义, 将在不久后删除", replaceWith = ReplaceWith("getFriend(this)"))
    fun Long.qq(): QQ = getFriend(this)

    final override fun toString(): String {
        return "Bot(${uin})"
    }

    /**
     * 需要调用者自行 close [output]
     */
    suspend inline fun Image.downloadTo(output: OutputStream) =
        download().use { input -> input.transferTo(output) }

    // endregion
}