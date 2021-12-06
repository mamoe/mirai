/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.permission

import kotlin.annotation.AnnotationTarget.*

/**
 * 表示一个应该由专有的权限插件 (提供 [PermissionService] 的插件) 实现的类.
 *
 *
 * 这样的类不能被用户手动实现或者继承, 也不能使用属性委托或者类委托, 或者其他任意直接或间接实现他们的手段 (否则会导致 [PermissionService] 处理异常).
 *
 * 普通插件仅应该使用从 [PermissionService] 或其他途径获取这些对象.
 */
@Retention(AnnotationRetention.BINARY)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
internal annotation class PermissionImplementation