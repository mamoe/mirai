package net.mamoe.mirai.console.utils

/**
 * 表明这个 API 是为了让 Java 使用者调用更方便. Kotlin 使用者不应该使用这些 API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
internal annotation class JavaFriendlyAPI
