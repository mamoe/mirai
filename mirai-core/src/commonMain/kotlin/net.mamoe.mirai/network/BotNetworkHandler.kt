/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.io.PlatformDatagramChannel

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
 */
@Suppress("PropertyName")
abstract class BotNetworkHandler : CoroutineScope {
    /**
     * 所属 [Bot]. 为弱引用
     */
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
     * 本函数将挂起直到登录成功.
     *
     * 不要使用这个 API. 请使用 [Bot.login]
     */
    @MiraiInternalAPI
    abstract suspend fun login()

    /**
     * 初始化获取好友列表等值.
     *
     * 不要使用这个 API. 它会在登录完成后被自动调用.
     */
    @MiraiInternalAPI
    open suspend fun init() {
    }

    /**
     * 等待直到与服务器断开连接. 若未连接则立即返回
     */
    abstract suspend fun awaitDisconnection()

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

suspend fun BotNetworkHandler.closeAndJoin(cause: Throwable? = null) {
    this.close(cause)
    this.supervisor.join()
}