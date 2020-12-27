/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.annotation.AnnotationTarget.*

/**
 * 标记为一个仅供 Mirai 内部使用的 API.
 *
 * 这些 API 可能会在任意时刻更改, 且不会发布任何预警.
 * 非常不建议在发行版本中使用这些 API.
 */
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(
    CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR,
    CLASS, FUNCTION, PROPERTY
)
@MustBeDocumented
public annotation class MiraiInternalApi(
    public val message: String = ""
)

/**
 * 标记这个类, 类型, 函数, 属性, 字段, 或构造器为实验性的 API.
 *
 * 这些 API 不具有稳定性, 且可能会在任意时刻更改.
 * 不建议在发行版本中使用这些 API.
 */
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
public annotation class MiraiExperimentalApi(
    public val message: String = ""
)

/**
 * 标记一个正计划在 [version] 版本时删除 (对外隐藏) 的 API.
 */
@Target(CLASS, PROPERTY, FIELD, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
internal annotation class PlannedRemoval(val version: String)


/**
 * 该注解仅用于测试 EventHandler
 *
 * 标注了此注解的意为像处理 java 方法那样处理 kotlin 方法
 */
@Retention(AnnotationRetention.RUNTIME)
internal annotation class EventListenerLikeJava


