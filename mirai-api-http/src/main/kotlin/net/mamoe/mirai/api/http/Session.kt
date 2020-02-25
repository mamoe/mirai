/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.api.http.queue.MessageQueue
import net.mamoe.mirai.event.Listener
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

tailrec fun generateSessionKey(): String {
    fun generateRandomSessionKey(): String {
        val all = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890qwertyuiopasdfghjklzxcvbnm"
        return buildString(capacity = 8) {
            repeat(8) {
                append(all.random())
            }
        }
    }

    val key = generateRandomSessionKey()
    if (!SessionManager.allSession.containsKey(key)) {
        return key
    }

    return generateSessionKey()
}

internal object SessionManager {

    val allSession: MutableMap<String, Session> = mutableMapOf()

    lateinit var authKey: String


    fun createTempSession(): TempSession = TempSession(EmptyCoroutineContext).also { newTempSession ->
        allSession[newTempSession.key] = newTempSession
        //设置180000ms后检测并回收
        newTempSession.launch {
            delay(180000)
            allSession[newTempSession.key]?.run {
                if (this is TempSession)
                    closeSession(newTempSession.key)
            }
        }
    }

    fun createAuthedSession(bot: Bot, originKey: String): AuthedSession = AuthedSession(bot, originKey, EmptyCoroutineContext).also { session ->
        closeSession(originKey)
        allSession[originKey] = session
    }

    operator fun get(sessionKey: String) = allSession[sessionKey]?.also {
        if (it is AuthedSession) it.latestUsed = currentTimeSeconds }

    fun containSession(sessionKey: String): Boolean = allSession.containsKey(sessionKey)

    fun closeSession(sessionKey: String) = allSession.remove(sessionKey)?.also { it.close() }

    fun closeSession(session: Session) = closeSession(session.key)

}


/**
 * @author NaturalHG
 * 这个用于管理不同Client与Mirai HTTP的会话
 *
 * [Session]均为内部操作用类
 * 需使用[SessionManager]
 */
abstract class Session internal constructor(
    coroutineContext: CoroutineContext,
    val key: String = generateSessionKey()
) : CoroutineScope {
    val supervisorJob = SupervisorJob(coroutineContext[Job])
    final override val coroutineContext: CoroutineContext = supervisorJob + coroutineContext

    internal open fun close() {
        supervisorJob.complete()
    }
}


/**
 * 任何新链接建立后分配一个[TempSession]
 *
 * TempSession在建立180s内没有转变为[AuthedSession]应被清除
 */
class TempSession internal constructor(coroutineContext: CoroutineContext) : Session(coroutineContext)

/**
 * 任何[TempSession]认证后转化为一个[AuthedSession]
 * 在这一步[AuthedSession]应该已经有assigned的bot
 */
class AuthedSession internal constructor(val bot: Bot, originKey: String, coroutineContext: CoroutineContext) : Session(coroutineContext, originKey) {

    companion object {
        const val CHECK_TIME = 1800L // 1800s aka 30min
    }

    val messageQueue = MessageQueue()
    private val _listener: Listener<BotEvent>
    private val releaseJob: Job //手动释放将会在下一次检查时回收Session

    internal var latestUsed = currentTimeSeconds

    init {
        _listener = bot.subscribeAlways{ this.run(messageQueue::add) }
        releaseJob = launch {
            while (true) {
                delay(CHECK_TIME * 1000)
                if (currentTimeSeconds - latestUsed >= CHECK_TIME) {
                    SessionManager.closeSession(this@AuthedSession)
                    break
                }
            }
        }
    }

    override fun close() {
        messageQueue.clear()
        _listener.complete()
        super.close()
    }
}

