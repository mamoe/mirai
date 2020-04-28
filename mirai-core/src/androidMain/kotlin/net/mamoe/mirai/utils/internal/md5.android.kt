@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils.internal

import java.io.InputStream
import java.security.MessageDigest

internal actual fun ByteArray.md5(offset: Int, length: Int): ByteArray {
    this.checkOffsetAndLength(offset, length)
    return MessageDigest.getInstance("MD5").apply { update(this@md5, offset, length) }.digest()
}

internal actual fun InputStream.md5(): ByteArray {
    val digest = MessageDigest.getInstance("md5")
    digest.reset()
    this.readInSequence {
        digest.update(it.toByte())
    }
    return digest.digest()
}

internal actual typealias InputStream = InputStream