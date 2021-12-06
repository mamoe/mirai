/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.ExtensionException
import net.mamoe.mirai.console.extension.FunctionExtension
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge

/**
 * 在 Console 启动完成后立即在主线程调用的扩展. 用于进行一些必要的延迟初始化.
 *
 * 这些扩展只会, 且一定会被调用正好一次.
 */
public fun interface PostStartupExtension : FunctionExtension {
    /**
     * 将在 Console 主线程执行.
     *
     * #### 内部实现细节
     * 在 [MiraiConsoleImplementationBridge.doStart] 所有 [MiraiConsoleImplementationBridge.phase] 执行完成后顺序调用.
     *
     * @throws Exception 所有抛出的 [Exception] 都会被捕获并包装为 [ExtensionException] 抛出, 并停止 [MiraiConsole]
     */
    @Throws(Exception::class)
    public operator fun invoke()

    public companion object ExtensionPoint : AbstractExtensionPoint<PostStartupExtension>(PostStartupExtension::class)
}