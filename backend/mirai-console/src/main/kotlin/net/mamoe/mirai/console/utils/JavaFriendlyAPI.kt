package net.mamoe.mirai.console.utils

import kotlin.annotation.AnnotationTarget.*

/**
 * 表明这个 API 是为了让 Java 使用者调用更方便. Kotlin 使用者不应该使用这些 API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(PROPERTY, FUNCTION, TYPE, CLASS)
internal annotation class JavaFriendlyAPI

/**
 * 标记为一个仅供 mirai-console 内部使用的 API.
 *
 * 这些 API 可能会在任意时刻更改, 且不会发布任何预警.
 * 非常不建议在发行版本中使用这些 API.
 */
@Retention(AnnotationRetention.SOURCE)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR, CLASS, FUNCTION, PROPERTY)
@MustBeDocumented
public annotation class ConsoleInternalAPI(
    val message: String = ""
)

/**
 * 标记一个实验性的 API.
 *
 * 这些 API 不具有稳定性, 且可能会在任意时刻更改.
 * 不建议在发行版本中使用这些 API.
 */
@Retention(AnnotationRetention.SOURCE)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
@MustBeDocumented
public annotation class ConsoleExperimentalAPI(
    val message: String = ""
)
