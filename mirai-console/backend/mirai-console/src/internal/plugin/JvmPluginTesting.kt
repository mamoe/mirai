/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.plugin

import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 仅用于 Console 测试, 标记期望方法执行结果应该是 success 还是 failed
 */
@MiraiInternalApi
public annotation class ConsoleJvmPluginFuncCallbackStatusExcept {
    @MiraiInternalApi
    @Target(AnnotationTarget.CLASS)
    public annotation class OnEnable(
        val excepted: ConsoleJvmPluginFuncCallbackStatus,
    )
}

@MiraiInternalApi
public enum class ConsoleJvmPluginFuncCallbackStatus {
    SUCCESS, FAILED
}

@MiraiInternalApi
public class ConsoleJvmPluginTestFailedError : Error {
    public constructor() : super()
    public constructor(cause: Throwable?) : super(cause)
    public constructor(msg: String?, cause: Throwable?) : super(msg, cause)
    public constructor(msg: String?) : super(msg)
}
