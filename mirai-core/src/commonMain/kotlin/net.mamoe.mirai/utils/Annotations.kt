package net.mamoe.mirai.utils

import kotlin.annotation.AnnotationTarget.*

/**
 * 标记为一个仅供 Mirai 内部使用的 API.
 *
 * 这些 API 可能会在任意时刻更改, 且不会发布任何预警.
 * 非常不建议在发行版本中使用这些 API.
 */
@Experimental(level = Experimental.Level.ERROR)
@Target(
    CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR,
    CLASS,
    FUNCTION,
    PROPERTY
)
annotation class MiraiInternalAPI(
    val message: String = ""
)

/**
 * 标记这个类, 类型, 函数, 属性, 字段, 或构造器为实验性的.
 *
 * 这些 API 不具有稳定性, 且可能会在任意时刻更改.
 * 不建议在发行版本中使用这些 API.
 */
@Experimental(level = Experimental.Level.ERROR)
@Target(CLASS, TYPEALIAS, FUNCTION, PROPERTY, FIELD, CONSTRUCTOR)
annotation class MiraiExperimentalAPI(
    val message: String = ""
)

/**
 * 标记这个 API 是自 Mirai 某个版本起才受支持.
 */
@Target(CLASS, PROPERTY, FIELD, CONSTRUCTOR, FUNCTION, PROPERTY_GETTER, PROPERTY_SETTER, TYPEALIAS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented
annotation class SinceMirai(val version: String)

/**
 * 包的最后一次修改时间, 和分析时使用的 TIM 版本
 */
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class PacketVersion(val date: String, val timVersion: String)

/**
 * 带有这个注解的 [Packet] 将不会被记录在 log 中.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoLog