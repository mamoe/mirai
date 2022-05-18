/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Inet4Address
import java.security.MessageDigest
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater

public actual val DEFAULT_BUFFER_SIZE: Int get() = kotlin.io.DEFAULT_BUFFER_SIZE

public actual fun ByteArray.unzip(offset: Int, length: Int): ByteArray {
    checkOffsetAndLength(offset, length)
    if (length == 0) return ByteArray(0)

    val inflater = Inflater()
    inflater.reset()
    ByteArrayOutputStream().use { output ->
        inflater.setInput(this, offset, length)
        ByteArray(DEFAULT_BUFFER_SIZE).let {
            while (!inflater.finished()) {
                output.write(it, 0, inflater.inflate(it))
            }
        }

        inflater.end()
        return output.toByteArray()
    }
}

public actual fun localIpAddress(): String = runCatching {
    Inet4Address.getLocalHost().hostAddress
}.getOrElse { "192.168.1.123" }

public fun InputStream.md5(): ByteArray {
    return digest("md5")
}

public fun InputStream.digest(algorithm: String): ByteArray {
    val digest = MessageDigest.getInstance(algorithm)
    digest.reset()
    use { input ->
        object : OutputStream() {
            override fun write(b: Int) {
                digest.update(b.toByte())
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                digest.update(b, off, len)
            }
        }.use { output ->
            input.copyTo(output)
        }
    }
    return digest.digest()
}

public fun InputStream.sha1(): ByteArray {
    return digest("SHA-1")
}

public actual fun ByteArray.md5(offset: Int, length: Int): ByteArray {
    checkOffsetAndLength(offset, length)
    return MessageDigest.getInstance("MD5").apply { update(this@md5, offset, length) }.digest()
}


@JvmOverloads
public actual fun ByteArray.sha1(offset: Int, length: Int): ByteArray {
    checkOffsetAndLength(offset, length)
    return MessageDigest.getInstance("SHA-1").apply { update(this@sha1, offset, length) }.digest()
}

@JvmOverloads
public actual fun ByteArray.ungzip(offset: Int, length: Int): ByteArray {
    return GZIPInputStream(inputStream(offset, length)).use { it.readBytes() }
}

@JvmOverloads
public actual fun ByteArray.gzip(offset: Int, length: Int): ByteArray {
    ByteArrayOutputStream().use { buf ->
        GZIPOutputStream(buf).use { gzip ->
            inputStream(offset, length).use { t -> t.copyTo(gzip) }
        }
        buf.flush()
        return buf.toByteArray()
    }
}

@JvmOverloads
public actual fun ByteArray.zip(offset: Int, length: Int): ByteArray {
    checkOffsetAndLength(offset, length)
    if (length == 0) return ByteArray(0)

    val deflater = Deflater()
    deflater.setInput(this, offset, length)
    deflater.finish()

    ByteArray(DEFAULT_BUFFER_SIZE).let {
        return it.take(deflater.deflate(it)).toByteArray().also { deflater.end() }
    }
}

public actual fun availableProcessors(): Int = Runtime.getRuntime().availableProcessors()