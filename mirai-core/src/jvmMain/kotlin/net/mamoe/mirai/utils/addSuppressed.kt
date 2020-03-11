package net.mamoe.mirai.utils

private var isAddSuppressedSupported: Boolean = true

@PublishedApi
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