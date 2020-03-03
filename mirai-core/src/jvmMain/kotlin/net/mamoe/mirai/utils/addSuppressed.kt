package net.mamoe.mirai.utils

private var isAddSuppressedSupported: Boolean = true

@MiraiInternalAPI
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
actual fun Throwable.addSuppressed(e: Throwable) {
    if (this === e) {
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