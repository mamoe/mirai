package demo.gentleman

import kotlin.reflect.KClass


@Throws(TryFailedException::class)
inline fun <T> tryNTimes(
    tryTimes: Int,
    vararg expectingExceptions: KClass<out Exception> = Array<KClass<out Exception>>(1) { Exception::class },
    expectingHandler: (Exception) -> Unit = { },
    unexpectingHandler: (Exception) -> Unit = { throw it },
    block: () -> T
): T {
    require(tryTimes > 0) { "tryTimes must be greater than 0" }

    var lastE: java.lang.Exception? = null
    repeat(tryTimes) {
        try {
            return block()
        } catch (e: Exception) {
            if (lastE != null && lastE !== e) {
                e.addSuppressed(lastE)
            }
            lastE = e
            if (expectingExceptions.any { it.isInstance(e) }) {
                expectingHandler(e)
            } else {
                unexpectingHandler(e)
            }
        }
    }

    if (lastE != null) {
        throw TryFailedException(lastE!!)
    }
    throw TryFailedException()
}

class TryFailedException : RuntimeException {
    constructor() : super()
    constructor(e: Exception) : super(e)
}