package net.mamoe.mirai.event.events


// 不要删除多平台结构.
// 否则在 Java 中这个 class 不会被认为是 java.lang.RuntimeException (Kotlin bug)
@Suppress("unused")
actual class EventCancelledException : RuntimeException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}