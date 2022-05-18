/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.coroutines.channels.SendChannel
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.utils.ProgressionCallback.Companion.asProgressionCallback
import kotlin.jvm.JvmStatic


/**
 * 操作进度回调, 可供前端使用, 以提供进度显示.
 *
 * @param S subject, 操作对象, 如 [AbsoluteFile]
 * @param P progression, 用于提示进度. 如当下载文件时为已下载文件大小字节数 [Long].
 *
 * @see asProgressionCallback
 *
 * @since 2.8
 */
public interface ProgressionCallback<in S, in P> {
    /**
     * 当操作开始时调用
     */
    public fun onBegin(subject: S, resource: ExternalResource) {}

    /**
     * 每当有进度更新时调用. 此方法可能会同时被多个线程调用.
     */
    public fun onProgression(subject: S, resource: ExternalResource, progression: P) {}

    /**
     * 当操作成功时调用.
     *
     * 在默认实现下只会由 [onFinished] 调用
     */
    public fun onSuccess(subject: S, resource: ExternalResource, progression: P) {}

    /**
     * 当操作以异常失败时调用.
     *
     * 在默认实现下只会由 [onFinished] 调用
     */
    public fun onFailure(subject: S, resource: ExternalResource, exception: Throwable) {}

    /**
     * 当操作完成时调用.
     */
    public fun onFinished(subject: S, resource: ExternalResource, result: Result<P>) {
        result.fold(
            onSuccess = { onSuccess(subject, resource, it) },
            onFailure = { onFailure(subject, resource, it) },
        )
    }

    public companion object {
        /**
         * 将一个 [SendChannel] 作为 [ProgressionCallback] 使用.
         *
         * ## 下载文件的使用示例
         *
         * 每当有进度更新, 已下载的字节数都会被[发送][SendChannel.offer]到 [SendChannel] 中.
         * 进度的发送会通过 [offer][SendChannel.offer], 而不是通过 [send][SendChannel.send]. 意味着 [SendChannel] 通常要实现缓存.
         *
         * 若 [closeOnFinish] 为 `true`, 当下载完成 (无论是失败还是成功) 时会 [关闭][SendChannel.close] [SendChannel].
         *
         * 使用示例:
         * ```
         * val progress = Channel<Long>(Channel.BUFFERED)
         *
         * launch {
         *   // 每 3 秒发送一次操作进度百分比
         *   progress.receiveAsFlow().sample(Duration.seconds(3)).collect { bytes ->
         *     group.sendMessage("File upload: ${(bytes.toDouble() / resource.size * 100).toInt() / 100}%.") // 保留 2 位小数
         *   }
         * }
         *
         * group.files.uploadNewFile("/foo.txt", resource, callback = progress.asProgressionCallback(true))
         * group.sendMessage("File uploaded successfully.")
         * ```
         *
         * 直接使用 [ProgressionCallback] 也可以实现示例这样的功能, [asProgressionCallback] 是为了简化操作.
         */
        @JvmStatic
        public fun <S, P> SendChannel<P>.asProgressionCallback(closeOnFinish: Boolean = true): ProgressionCallback<S, P> {
            return object : ProgressionCallback<S, P> {
                override fun onProgression(subject: S, resource: ExternalResource, progression: P) {
                    trySend(progression)
                }

                override fun onFinished(subject: S, resource: ExternalResource, result: Result<P>) {
                    if (closeOnFinish) this@asProgressionCallback.close(result.exceptionOrNull())
                }
            }
        }
    }
}
