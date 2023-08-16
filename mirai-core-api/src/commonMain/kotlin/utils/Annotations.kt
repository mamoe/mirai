/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
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
 * 标记为一个仅供 Mirai 内部使用的 API.
 *
 * 这些 API 可能会在任意时刻更改, 且不会发布任何预警.
 * 非常不建议在发行版本中使用这些 API.
 */
@Retention(AnnotationRetention.BINARY)
@Target(FILE)
public annotation class MiraiInternalFile

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
 * 标记一个定义在使用上是稳定的 (如果没有特殊说明), 但只应该由 mirai 内部实现.
 *
 * 用户自行实现将可能造成对未来版本的不兼容, 因为新的抽象函数或属性会在未经警告的前提下添加. 自行实现还可能因 mirai 内部实现有部分硬编码成分而不兼容.
 *
 * @since 2.7
 */
@Retention(AnnotationRetention.BINARY)
@Target(CLASS, PROPERTY, FUNCTION)
@MustBeDocumented
public annotation class NotStableForInheritance(
    public val message: String = "This declaration is not stable for inheritance."
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


/**
 * 表明这个 API 是为了让 Java 使用者调用更方便.
 *
 * 一般有一定的性能损失, 且不能在 JVM/Android 以外平台使用. 不要在 Kotlin 调用它.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(PROPERTY, FUNCTION, CLASS)
public annotation class JavaFriendlyAPI // made public since 2.8.0-RC