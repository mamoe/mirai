/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.compiler.common

import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * 标记一个函数, 在其函数体内限制特定一些函数的使用.
 *
 * @suppress 这是实验性 API, 可能会在未来有不兼容变更
 */
@Target(FUNCTION)
@Retention(AnnotationRetention.BINARY)
public annotation class RestrictedScope(
    vararg val kinds: Kind,
) {
    public enum class Kind {
        PERMISSION_REGISTER, // ILLEGAL_PERMISSION_REGISTER_USE
        COMMAND_REGISTER, // ILLEGAL_COMMAND_REGISTER_USE
    }
}