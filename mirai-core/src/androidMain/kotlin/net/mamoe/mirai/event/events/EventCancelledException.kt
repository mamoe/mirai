package net.mamoe.mirai.event.events

@Suppress("unused")
actual class EventCancelledException : RuntimeException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}