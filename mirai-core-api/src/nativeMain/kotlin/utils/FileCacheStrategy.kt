/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

/**
 * 资源缓存策略.
 *
 * 注意: 本接口只用于 JVM 平台. 在 native 平台没有作用.
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
public actual interface FileCacheStrategy {
    public actual companion object {
        /**
         * 当前平台下默认的缓存策略. 注意, 这可能不是 Mirai 全局默认使用的, Mirai 从 [IMirai.FileCacheStrategy] 获取.
         *
         * @see IMirai.FileCacheStrategy
         */
        @MiraiExperimentalApi
        public actual val PlatformDefault: FileCacheStrategy = object : FileCacheStrategy {}
    }
}