/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.utils

import io.ktor.utils.io.errors.*
import kotlinx.coroutines.Dispatchers
import net.mamoe.mirai.Bot
import net.mamoe.mirai.IMirai
import net.mamoe.mirai.utils.ExternalResource.Companion.sendAsImageTo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsImage
import net.mamoe.mirai.utils.FileCacheStrategy.MemoryCache
import net.mamoe.mirai.utils.FileCacheStrategy.TempCache
import java.io.File
import java.io.InputStream

/**
 * 资源缓存策略.
 *
 * 由于上传资源时服务器要求提前给出 MD5 和文件大小等数据, 一些资源如 [InputStream] 需要首先缓存才能使用.
 *
 * 资源的缓存都是将 [InputStream] 缓存未 [ExternalResource]. 根据 [FileCacheStrategy] 实现不同, 可以以临时文件存储, 也可以在数据库或是内存按需存储.
 * Mirai 内置的实现有 [内存存储][MemoryCache] 和 [临时文件存储][TempCache].
 * 操作 [ExternalResource.toExternalResource] 时将会使用 [IMirai.FileCacheStrategy]. 可以覆盖, 示例:
 * ```
 * // Kotlin
 * Mirai.FileCacheStrategy = FileCacheStrategy.TempCache() // 使用系统默认缓存路径, 也是默认的行为
 * Mirai.FileCacheStrategy = FileCacheStrategy.TempCache(File("C:/cache")) // 使用自定义缓存路径
 *
 * // Java
 * Mirai.getInstance().setFileCacheStrategy(new FileCacheStrategy.TempCache()); // 使用系统默认缓存路径, 也是默认的行为
 * Mirai.getInstance().setFileCacheStrategy(new FileCacheStrategy.TempCache(new File("C:/cache"))); // 使用自定义的缓存路径
 * ```
 *
 * 此接口的实现和使用都是稳定的. 自行实现的 [FileCacheStrategy] 也可以被 Mirai 使用.
 *
 * 注意, 此接口目前仅缓存 [InputStream] 等一次性数据. 好友列表等数据由每个 [Bot] 的 [BotConfiguration.cacheDir] 缓存.
 *
 * ### 使用 [FileCacheStrategy] 的操作
 * - [ExternalResource.toExternalResource]
 * - [ExternalResource.uploadAsImage]
 * - [ExternalResource.sendAsImageTo]
 *
 * @see ExternalResource
 */
public interface FileCacheStrategy {
    /**
     * 立即读取 [input] 所有内容并缓存为 [ExternalResource].
     *
     * 注意:
     * - 此函数不会关闭输入
     * - 此函数可能会阻塞线程读取 [input] 内容, 若在 Kotlin 协程使用请确保在允许阻塞的环境 ([Dispatchers.IO]).
     *
     * @param formatName 文件类型. 此参数通常只会影响官方客户端接收到的文件的文件后缀. 若为 `null` 则会自动根据文件头识别. 识别失败时将使用 "mirai"
     */
    @Throws(IOException::class)
    public fun newCache(input: InputStream, formatName: String? = null): ExternalResource

    /**
     * 立即读取 [input] 所有内容并缓存为 [ExternalResource]. 自动根据文件头识别文件类型. 识别失败时将使用 "mirai".
     *
     * 注意:
     * - 此函数不会关闭输入
     * - 此函数可能会阻塞线程读取 [input] 内容, 若在 Kotlin 协程使用请确保在允许阻塞的环境 ([Dispatchers.IO]).
     */
    @Throws(IOException::class)
    public fun newCache(input: InputStream): ExternalResource = newCache(input, null)

    /**
     * 使用内存直接存储所有图片文件. 由 JVM 执行 GC.
     */
    public object MemoryCache : FileCacheStrategy {
        @Throws(IOException::class)
        override fun newCache(input: InputStream, formatName: String?): ExternalResource {
            return input.readBytes().toExternalResource(formatName)
        }
    }

    /**
     * 使用系统临时文件夹缓存图片文件. 在图片使用完毕后或 JVM 正常结束时删除临时文件.
     */
    public class TempCache @JvmOverloads public constructor(
        /**
         * 缓存图片存放位置. 为 `null` 时使用主机系统的临时文件夹: `File.createTempFile("tmp", null, directory)`
         */
        public val directory: File? = null,
    ) : FileCacheStrategy {
        private fun createTempFile(): File {
            return File.createTempFile("tmp", null, directory)
        }

        @Throws(IOException::class)
        override fun newCache(input: InputStream, formatName: String?): ExternalResource {
            val file = createTempFile()
            return file.apply {
                deleteOnExit()
                outputStream().use { out -> input.copyTo(out) }
            }.toExternalResource(formatName).apply {
                closed.invokeOnCompletion {
                    kotlin.runCatching { file.delete() }
                }
            }
        }
    }

    public companion object {
        /**
         * 当前平台下默认的缓存策略. 注意, 这可能不是 Mirai 全局默认使用的, Mirai 从 [IMirai.FileCacheStrategy] 获取.
         *
         * @see IMirai.FileCacheStrategy
         */
        @MiraiExperimentalApi
        @JvmStatic
        public val PlatformDefault: FileCacheStrategy = TempCache(null)
    }
}
