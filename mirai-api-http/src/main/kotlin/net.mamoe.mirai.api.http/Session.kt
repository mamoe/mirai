package net.mamoe.mirai.api.http

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
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

object SessionManager {

    val allSession: MutableMap<String, Session> = mutableMapOf()

    fun createTempSession(): TempSession = TempSession(EmptyCoroutineContext).also { allSession[it.key] = it }

    fun closeSession(sessionKey: String) = allSession.remove(sessionKey)?.also { it.close() }

    fun closeSession(session: Session) = closeSession(session.key)
}


/**
 * @author NaturalHG
 * 这个用于管理不同Client与Mirai HTTP的会话
 */
abstract class Session internal constructor(
    coroutineContext: CoroutineContext
) : CoroutineScope {
    private val supervisorJob = SupervisorJob()
    final override val coroutineContext: CoroutineContext = supervisorJob + coroutineContext

    val key: String = generateSessionKey()


    internal fun close() {
        supervisorJob.cancel()
    }

}


/**
 * 任何新链接建立后分配一个[TempSession]
 *
 * TempSession在建立180s内没有转变为[AuthedSession]应被清除
 */
class TempSession internal constructor(coroutineContext: CoroutineContext) : Session(coroutineContext) {

}

/**
 * 任何[TempSession]认证后转化为一个[AuthedSession]
 * 在这一步[AuthedSession]应该已经有assigned的bot
 */
class AuthedSession internal constructor(botNumber: Int, coroutineContext: CoroutineContext) : Session(coroutineContext) {


}




