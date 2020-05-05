package net.mamoe.mirai.utils.internal

private var isAddSuppressedSupported: Boolean = true

internal actual fun Throwable.addSuppressedMirai(e: Throwable) {
    if (this == e) {
        return
    }
    if (!isAddSuppressedSupported) {
        return
    }
    try {
        this.addSuppressed(e)
    } catch (e: Exception) {
        isAddSuppressedSupported = false
    }
}