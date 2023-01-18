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
import io.ktor.utils.io.streams.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.*

public actual val DEFAULT_BUFFER_SIZE: Int get() = kotlin.io.DEFAULT_BUFFER_SIZE

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

public fun InputStream.sha256(): ByteArray {
    return digest("SHA-256")
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
public actual fun ByteArray.sha256(offset: Int, length: Int): ByteArray {
    checkOffsetAndLength(offset, length)
    return MessageDigest.getInstance("SHA-256").apply { update(this@sha256, offset, length) }.digest()
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
public actual fun ByteArray.ungzip(offset: Int, length: Int): ByteArray {
    return GZIPInputStream(inputStream(offset, length)).use { it.readBytes() }
}

public actual fun ByteArray.inflate(offset: Int, length: Int): ByteArray {
    checkOffsetAndLength(offset, length)
    if (length == 0) return ByteArray(0)

    val inflater = Inflater()
    inflater.reset()
    return InflaterInputStream(ByteArrayInputStream(this, offset, length), inflater).readBytes()
//    ByteArrayOutputStream().use { output ->
//        inflater.setInput(this, offset, length)
//        ByteArray(DEFAULT_BUFFER_SIZE).let {
//            while (!inflater.finished()) {
//                output.write(it, 0, inflater.inflate(it))
//            }
//        }
//
//        inflater.end()
//        return output.toByteArray()
//    }
}

@JvmOverloads
public actual fun ByteArray.deflate(offset: Int, length: Int): ByteArray {
    checkOffsetAndLength(offset, length)
    if (length == 0) return ByteArray(0)

    val deflater = Deflater()
    deflater.setInput(this, offset, length)
    deflater.finish()

    ByteArray(DEFAULT_BUFFER_SIZE).let {
        return it.take(deflater.deflate(it)).toByteArray().also { deflater.end() }
    }
}


/**
 * Input will be closed.
 */
public actual fun Input.gzipAllAvailable(): ByteArray {
    return this.readBytes().gzip()

    // The following doesn't work, input's release won't becalled. Possibly Ktor bug.
//    return this.use {
//        ByteArrayOutputStream().use { buf ->
//            GZIPOutputStream(buf).use { gzip ->
//                copyTo(gzip.asOutput())
//            }
//            buf.flush()
//            buf.toByteArray()
//        }
//    }
}

/**
 * Input will be closed.
 */
public actual fun Input.ungzipAllAvailable(): ByteArray {
    return GZIPInputStream(this.asStream()).use { it.readBytes() }
}

/**
 * Input will be closed.
 */
public actual fun Input.inflateAllAvailable(): ByteArray {
    return this.inflateInput().use { it.readBytes() }
}

/**
 * Input will be closed.
 */
public actual fun Input.deflateAllAvailable(): ByteArray {
    return this.deflateInput().use { it.readBytes() }
}


private fun Input.asStream(): InputStream = object : InputStream() {

    override fun read(): Int {
        if (endOfInput) return -1
        return readByte().toIntUnsigned()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (this@asStream.endOfInput) return -1
        return readAvailable(buffer, offset, length)
    }

    override fun skip(count: Long): Long = discard(count)

    override fun close() {
        this@asStream.close()
    }
}

/**
 * [source] will be closed on returned [Input.close]
 */
@Suppress("FunctionName")
public actual fun GzipDecompressionInput(source: Input): Input {
    return GZIPInputStream(source.asStream()).asInput()
}


/**
 * [source] will be closed on returned [Input.close]
 */
@Suppress("FunctionName")
public actual fun InflateInput(source: Input): Input {
    val inflater = Inflater()
    inflater.reset()
    return InflaterInputStream(source.asStream(), inflater).asInput()
}

/**
 * [source] will be closed on returned [Input.close]
 */
@Suppress("FunctionName")
public actual fun DeflateInput(source: Input): Input {
    val deflater = Deflater()
    deflater.reset()
    return DeflaterInputStream(source.asStream(), deflater).asInput()
}
