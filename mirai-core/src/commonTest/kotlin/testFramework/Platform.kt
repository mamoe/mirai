/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework


expect fun currentPlatform(): Platform

enum class PlatformRuntime {
    JVM,
    DALVIK,
}

// see `@DisabledOnPlatform`
sealed class Platform(
    val runtime: PlatformRuntime,
) {
    sealed class JvmLike(runtime: PlatformRuntime) : Platform(runtime)

    sealed class Android(runtime: PlatformRuntime) : JvmLike(runtime)

    sealed class AndroidUnitTest : Android(PlatformRuntime.JVM)
    object AndroidUnitTestWithJdk : AndroidUnitTest()
    object AndroidUnitTestWithAdk : AndroidUnitTest()

    object AndroidInstrumentedTest : Android(PlatformRuntime.DALVIK)

    object Jvm : JvmLike(PlatformRuntime.JVM)
}

