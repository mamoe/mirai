/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.rules

import net.mamoe.mirai.internal.testFramework.Platform
import kotlin.reflect.KClass

/**
 * 在 commonTest 使用, 以在目标平台忽略这个 test.
 *
 * 此注解只对 JVM 和 Android 平台生效.
 * 要在 native 平台忽略 test, 请手动写 `if (currentPlatform() is Native) return`.
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class DisabledOnJvmLikePlatform(
    vararg val values: KClass<out Platform.JvmLike> // don't read this property
)