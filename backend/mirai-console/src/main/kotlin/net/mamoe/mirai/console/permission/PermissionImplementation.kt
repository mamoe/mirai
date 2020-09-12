/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.permission

import kotlin.annotation.AnnotationTarget.*

/**
 * 表示一个应该由权限插件实现的类.
 *
 * 这样的类不能被用户手动实现或者继承, 也不能使用属性委托或者类委托, 或者其他任意改变实现类的手段.
 * 用户仅应该使用从 [PermissionService] 或其他途径获取这些对象, 而不能自行实现它们.
 */
@Retention(AnnotationRetention.BINARY)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
internal annotation class PermissionImplementation