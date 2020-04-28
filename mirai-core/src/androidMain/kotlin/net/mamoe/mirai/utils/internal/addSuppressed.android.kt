@file:Suppress("DuplicatedCode")

package net.mamoe.mirai.utils.internal

import android.os.Build

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.addSuppressed(e)
        } else {
            isAddSuppressedSupported = false
        }
    } catch (e: Exception) {
        isAddSuppressedSupported = false
    }
}