/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.qqandroid.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.WeakRefProperty

/**
 * Mirai 的网络处理器, 它承担所有数据包([Packet])的处理任务.
 * [BotNetworkHandler] 是线程安全的.
 *
 * NetworkHandler 实现接口 [CoroutineScope]
 * 即 [BotNetworkHandler] 自己就是作用域.
 * 所有 [BotNetworkHandler] 的协程均启动在此作用域下.
 *
 * [BotNetworkHandler] 的协程包含:
 * - UDP 包接收: [PlatformDatagramChannel.read]
 * - 心跳 Job
 * - Key 刷新
 * - 所有数据包处理和发送
 *
 * [BotNetworkHandler.close] 时将会 [取消][Job.cancel] 所有此作用域下的协程
 *
 * @suppress 此为**内部 API**, 可能在任意时刻被改动, 且不会给出任何警告.
 */
@Suppress("PropertyName")
internal abstract class BotNetworkHandler : CoroutineScope {
    /**
     * 所属 [Bot]. 为弱引用
     */
    @WeakRefProperty
    abstract val bot: Bot

    /**
     * 监管 child [Job]s
     */
    abstract val supervisor: CompletableJob

    /**
     * logger
     */
    abstract val logger: MiraiLogger

    /**
     * 依次尝试登录到可用的服务器. 在任一服务器登录完成后返回.
     *
     * - 会断开连接并重新登录.
     * - 不会停止网络层的 [Job].
     * - 重新登录时不会再次拉取联系人列表.
     * - 挂起直到登录成功.
     *
     * 不要使用这个 API. 请使用 [Bot.login]
     *
     * @throws LoginFailedException 登录失败时
     * @throws WrongPasswordException 密码错误时
     */
    @Suppress("SpellCheckingInspection")
    @MiraiInternalAPI
    abstract suspend fun closeEverythingAndRelogin(host: String, port: Int, cause: Throwable? = null)

    /**
     * 初始化获取好友列表等值.
     *
     * 不要使用这个 API. 它会在登录完成后被自动调用.
     */
    @MiraiInternalAPI
    open suspend fun init() {
    }

    /**
     * 当 [Bot] 正常运作时, 这个函数将一直挂起协程到 [Bot] 被 [Bot.close]
     */
    abstract suspend fun join()

    // cool name
    abstract fun areYouOk(): Boolean


    private val connectionLock: Mutex = Mutex()
    internal suspend inline fun withConnectionLock(block: BotNetworkHandler.() -> Unit) {
        connectionLock.withLock { if (areYouOk()) return else block() }
    }

    /**
     * 关闭网络接口, 停止所有有关协程和任务
     *
     * @param cause 关闭的原因. null 时视为正常关闭, 非 null 时视为异常关闭.
     */
    open fun close(cause: Throwable? = null) {
        if (supervisor.isActive) {
            if (cause != null) {
                supervisor.cancel(CancellationException("NetworkHandler closed", cause))
            } else {
                supervisor.cancel(CancellationException("NetworkHandler closed"))
            }
        }
    }
}

internal suspend fun BotNetworkHandler.closeAndJoin(cause: Throwable? = null) {
    this.close(cause)
    this.supervisor.join()
}