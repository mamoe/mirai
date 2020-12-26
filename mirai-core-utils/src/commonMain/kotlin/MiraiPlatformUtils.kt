/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiUtils")

package net.mamoe.mirai.utils

import io.ktor.client.*
import kotlinx.io.core.Input
import kotlinx.io.core.readAvailable
import java.io.*
import java.net.Inet4Address
import java.security.MessageDigest
import java.util.zip.Deflater
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.util.zip.Inflater
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public object MiraiPlatformUtils {
    /**
     * Ktor HttpClient. 不同平台使用不同引擎.
     */
    public val Http: HttpClient = HttpClient()
}

@JvmOverloads
public fun ByteArray.unzip(offset: Int = 0, length: Int = size - offset): ByteArray {
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

public fun InputStream.md5(): ByteArray {
    val digest = MessageDigest.getInstance("md5")
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

/**
 * Localhost 解析
 */
public fun localIpAddress(): String = runCatching {
    Inet4Address.getLocalHost().hostAddress
}.getOrElse { "192.168.1.123" }

public fun String.md5(): ByteArray = toByteArray().md5()

@JvmOverloads
public fun ByteArray.md5(offset: Int = 0, length: Int = size - offset): ByteArray {
    checkOffsetAndLength(offset, length)
    return MessageDigest.getInstance("MD5").apply { update(this@md5, offset, length) }.digest()
}

@JvmOverloads
public fun ByteArray.ungzip(offset: Int = 0, length: Int = size - offset): ByteArray {
    return GZIPInputStream(inputStream(offset, length)).use { it.readBytes() }
}

@JvmOverloads
public fun ByteArray.gzip(offset: Int = 0, length: Int = size - offset): ByteArray {
    ByteArrayOutputStream().use { buf ->
        GZIPOutputStream(buf).use { gzip ->
            inputStream(offset, length).use { t -> t.copyTo(gzip) }
        }
        buf.flush()
        return buf.toByteArray()
    }
}

@JvmOverloads
public fun ByteArray.zip(offset: Int = 0, length: Int = size - offset): ByteArray {
    checkOffsetAndLength(offset, length)
    if (length == 0) return ByteArray(0)

    val deflater = Deflater()
    deflater.setInput(this, offset, length)
    deflater.finish()

    ByteArray(DEFAULT_BUFFER_SIZE).let {
        return it.take(deflater.deflate(it)).toByteArray().also { deflater.end() }
    }
}

public inline fun <C : Closeable, R> C.withUse(block: C.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use(block)
}

@Throws(IOException::class)
@JvmOverloads
public fun Input.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = readAvailable(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = readAvailable(buffer)
    }
    return bytesCopied
}

public inline fun <I : AutoCloseable, O : AutoCloseable, R> I.withOut(output: O, block: I.(output: O) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return use { output.use { block(this, output) } }
}
