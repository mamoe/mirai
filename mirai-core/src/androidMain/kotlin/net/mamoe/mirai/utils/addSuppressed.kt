package net.mamoe.mirai.utils

import android.os.Build

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.addSuppressed(e)
        } else {
            isAddSuppressedSupported = false
        }
    } catch (e: Exception) {
        isAddSuppressedSupported = false
    }
}