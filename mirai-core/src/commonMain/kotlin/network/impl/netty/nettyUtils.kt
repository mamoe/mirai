/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelOutboundInvoker
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Job
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.streams.outputStream
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.SimpleLogger
import net.mamoe.mirai.utils.SimpleLogger.LogPriority.ERROR
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


internal fun MiraiLogger.asCoroutineExceptionHandler(
    priority: SimpleLogger.LogPriority = ERROR
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { context, e ->
        call(
            priority,
            context[CoroutineName]?.let { "Exception in coroutine '${it.name}'." } ?: "Exception in unnamed coroutine.",
            e
        )
    }
}

internal fun ChannelOutboundInvoker.writeAndFlushOrCloseAsync(msg: Any?): ChannelFuture? {
    return writeAndFlush(msg)
        .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
}


internal suspend inline fun joinCompleted(job: Job) {
    if (job.isCompleted) job.join()
}
