package net.mamoe.mirai.qqandroid.utils

@PublishedApi
internal actual fun Throwable.addSuppressedMirai(e: Throwable) {
    if (e === this) {
        return
    }
    kotlin.runCatching {
        this.addSuppressed(e)
    }
}