/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package test

import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.readAvailable
import io.ktor.utils.io.core.use
import io.ktor.utils.io.pool.useInstance
import net.mamoe.mirai.utils.DefaultLogger
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLoggerWithSwitch
import net.mamoe.mirai.utils.io.ByteArrayPool
import net.mamoe.mirai.utils.io.toReadPacket
import net.mamoe.mirai.utils.io.toUHexString
import net.mamoe.mirai.utils.withSwitch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


val DebugLogger: MiraiLoggerWithSwitch = DefaultLogger("Packet Debug").withSwitch(true)

inline fun ByteArray.debugPrintThis(name: String): ByteArray {
    DebugLogger.debug(name + "=" + this.toUHexString())
    return this
}

@UseExperimental(ExperimentalContracts::class, MiraiInternalAPI::class)
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