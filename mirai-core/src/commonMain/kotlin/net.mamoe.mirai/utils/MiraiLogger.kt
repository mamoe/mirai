package net.mamoe.mirai.utils

import kotlin.jvm.JvmOverloads

interface MiraiLogger {
    companion object : MiraiLogger by PlatformLogger("[TOP Level]")

    var identity: String?

    fun logInfo(any: Any?) = log(any)

    fun log(e: Throwable)

    fun log(any: Any?)

    fun logError(any: Any?)

    fun logDebug(any: Any?)

    fun logCyan(any: Any?)

    fun logPurple(any: Any?)

    fun logGreen(any: Any?)

    fun logBlue(any: Any?)
}

expect class PlatformLogger @JvmOverloads constructor(identity: String? = null) : MiraiLogger

fun Throwable.log() = MiraiLogger.log(this)