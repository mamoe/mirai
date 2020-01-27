package net.mamoe.mirai.qqandroid.io.serialization

import kotlinx.serialization.SerialDescriptor

/*
 * Helper for kotlinx.serialization
 */

internal inline fun <reified A: Annotation> SerialDescriptor.findAnnotation(elementIndex: Int): A? {
    val candidates = getElementAnnotations(elementIndex).filterIsInstance<A>()
    return when (candidates.size) {
        0 -> null
        1 -> candidates[0]
        else -> throw IllegalStateException("There are duplicate annotations of type ${A::class} in the descriptor $this")
    }
}
