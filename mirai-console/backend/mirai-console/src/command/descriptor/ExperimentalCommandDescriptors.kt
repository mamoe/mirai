/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.command.descriptor

import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import kotlin.annotation.AnnotationTarget.*

/**
 * 标记一个实验性的指令解释器 API.
 *
 * 这些 API 不具有稳定性, 且可能会在任意时刻更改.
 * 不建议在发行版本中使用这些 API.
 *
 * @since 1.0-RC
 */
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
@ConsoleExperimentalApi
@ExperimentalCommandDescriptors
public annotation class ExperimentalCommandDescriptors(
    val message: String = "Command descriptors are an experimental API.",
)