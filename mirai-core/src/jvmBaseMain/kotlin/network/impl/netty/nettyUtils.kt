/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import net.mamoe.mirai.utils.withUse


internal suspend fun ChannelFuture.awaitKt(): ChannelFuture {
    suspendCancellableCoroutine<Unit> { cont ->
        cont.invokeOnCancellation {
            channel().close()
        }
        addListener { f ->
            if (f.isSuccess) {
                cont.resumeWith(Result.success(Unit))
            } else {
                cont.resumeWith(Result.failure(f.cause()))
            }
        }
    }
    return this
}

internal fun ByteBuf.toReadPacket(): ByteReadPacket {
    val buf = this
    return buildPacket {
        ByteBufInputStream(buf).withUse { copyTo(outputStream()) }
    }
}
