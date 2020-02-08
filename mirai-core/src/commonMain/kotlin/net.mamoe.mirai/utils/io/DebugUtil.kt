/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("Utils")

package net.mamoe.mirai.utils.io

import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiDebugAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.withSwitch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.js.JsName
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName


@MiraiDebugAPI("Unsatble")
object DebugLogger : MiraiLogger by DefaultLogger("Packet Debug").withSwitch()

@MiraiDebugAPI("Unstable")
inline fun Throwable.logStacktrace(message: String? = null) = DebugLogger.error(message, this)

@MiraiDebugAPI("Low efficiency.")
inline fun debugPrintln(any: Any?) = DebugLogger.debug(any)

@MiraiDebugAPI("Low efficiency.")
inline fun String.debugPrintThis(name: String): String {
    DebugLogger.debug("$name=$this")
    return this
}

@MiraiDebugAPI("Low efficiency.")
inline fun ByteArray.debugPrintThis(name: String): ByteArray {
    DebugLogger.debug(name + "=" + this.toUHexString())
    return this
}

@MiraiDebugAPI("Low efficiency.")
inline fun IoBuffer.debugPrintThis(name: String): IoBuffer {
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        DebugLogger.debug(name + "=" + it.toUHexString(offset = 0, length = count))
        return it.toIoBuffer(0, count)
    }
}

@MiraiDebugAPI("Low efficiency.")
inline fun IoBuffer.debugCopyUse(block: IoBuffer.() -> Unit): IoBuffer {
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        block(it.toIoBuffer(0, count))
        return it.toIoBuffer(0, count)
    }
}

@MiraiDebugAPI("Low efficiency.")
inline fun Input.debugDiscardExact(n: Number, name: String = "") {
    DebugLogger.debug("Discarded($n) $name=" + this.readBytes(n.toInt()).toUHexString())
}

@MiraiDebugAPI("Low efficiency.")
inline fun ByteReadPacket.debugPrintThis(name: String = ""): ByteReadPacket {
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        DebugLogger.debug("ByteReadPacket $name=" + it.toUHexString(offset = 0, length = count))
        return it.toReadPacket(0, count)
    }
}

/**
 * 备份数据, 并在 [block] 失败后执行 [onFail].
 *
 * 此方法非常低效. 请仅在测试环境使用.
 */
@MiraiDebugAPI("Low efficiency")
@UseExperimental(ExperimentalContracts::class)
inline fun <R> Input.debugIfFail(name: String = "", onFail: (ByteArray) -> ByteReadPacket = { it.toReadPacket() }, block: ByteReadPacket.() -> R): R {

    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        callsInPlace(onFail, InvocationKind.UNKNOWN)
    }
    ByteArrayPool.useInstance {
        val count = this.readAvailable(it)
        try {
            return it.toReadPacket(0, count).use(block)
        } catch (e: Throwable) {
            onFail(it.take(count).toByteArray()).readAvailable(it)
            DebugLogger.debug("Error in ByteReadPacket $name=" + it.toUHexString(offset = 0, length = count))
            throw e
        }
    }
}