/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("RedundantVisibilityModifier")

package net.mamoe.mirai.utils

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import platform.zlib.*


public actual val DEFAULT_BUFFER_SIZE: Int get() = 8192

public actual fun ByteArray.md5(offset: Int, length: Int): ByteArray {
    MD5.create().run {
        update(this@md5, offset, length)
        return digest().bytes
    }
}

public actual fun ByteArray.sha1(offset: Int, length: Int): ByteArray = SHA1.create().run {
    update(this@sha1, offset, length)
    return digest().bytes
}

public actual fun ByteArray.sha256(offset: Int, length: Int): ByteArray = SHA256.create().run {
    update(this@sha256, offset, length)
    return digest().bytes
}

/**
 * WARNING: DO NOT SET THIS BUFFER TOO SMALL, OR YOU WILL SEE COMPRESSION ERROR.
 */
@set:TestOnly
public var ZLIB_BUFFER_SIZE: Long = 8192

public actual fun ByteArray.gzip(offset: Int, length: Int): ByteArray {
    return GzipCompressionInput(this.toReadPacket(offset, length)).use { it.readBytes() }
}

public actual fun ByteArray.ungzip(offset: Int, length: Int): ByteArray {
    return GzipDecompressionInput(this.toReadPacket(offset, length)).use { it.readBytes() }
}

public actual fun ByteArray.deflate(offset: Int, length: Int): ByteArray {
    return DeflateInput(this.toReadPacket(offset, length)).use { it.readBytes() }
}

public actual fun ByteArray.inflate(offset: Int, length: Int): ByteArray {
    return InflateInput(this.toReadPacket(offset, length)).use { it.readBytes() }
}


@Suppress("FunctionName")
public fun GzipCompressionInput(source: Input): Input {
    return ZlibInput(
        source = source,
        zlibInit = { deflateInit2(it, Z_DEFAULT_COMPRESSION, Z_DEFLATED, 15 or 16, 8, Z_DEFAULT_STRATEGY) },
        zlibProcess = { z, flush -> deflate(z, flush) },
        zlibHasPending = null,
        zlibFlushMode = { if (it) Z_FINISH else Z_NO_FLUSH },
        zlibEnd = { deflateEnd(it) },
    )
}

@Suppress("FunctionName")
public actual fun GzipDecompressionInput(source: Input): Input {
    return ZlibInput(
        source = source,
        zlibInit = { inflateInit2(it, 15 or 16) },
        zlibProcess = { z, flush -> inflate(z, flush) },
        zlibHasPending = null,
        zlibFlushMode = { if (it) Z_SYNC_FLUSH else Z_NO_FLUSH },
        zlibEnd = { inflateEnd(it) },
    )
}

@Suppress("FunctionName")
public actual fun InflateInput(source: Input): Input {
    return ZlibInput(
        source = source,
        zlibInit = { inflateInit2(it, 15) },
        zlibProcess = { z, flush -> inflate(z, flush) },
        zlibHasPending = null,
        zlibFlushMode = { if (it) Z_SYNC_FLUSH else Z_NO_FLUSH },
        zlibEnd = { inflateEnd(it) },
    )
}

@Suppress("FunctionName")
public actual fun DeflateInput(source: Input): Input {
    return ZlibInput(
        source = source,
        zlibInit = { deflateInit(it, Z_DEFAULT_COMPRESSION) },
        zlibProcess = { z, flush -> deflate(z, flush) },
        zlibHasPending = { z ->
            memScoped {
                val pendingBytes = cValue<UIntVar>().ptr
                val pendingBits = cValue<IntVar>().ptr

                if (deflatePending(z, pendingBytes, pendingBits) != Z_OK) {
                    false
                } else {
                    pendingBytes.pointed.value > 0u || pendingBits.pointed.value > 0
                }
            }
        },
        zlibFlushMode = { if (it) Z_FINISH else Z_NO_FLUSH },
        zlibEnd = { deflateEnd(it) },
    )
}


/**
 * Input will be closed.
 */
public actual fun Input.gzipAllAvailable(): ByteArray {
    return GzipCompressionInput(this).use { it.readBytes() }
}

/**
 * Input will be closed.
 */
public actual fun Input.ungzipAllAvailable(): ByteArray {
    return GzipDecompressionInput(this).use { it.readBytes() }
}

/**
 * Input will be closed.
 */
public actual fun Input.inflateAllAvailable(): ByteArray {
    return InflateInput(this).use { it.readBytes() }
}

/**
 * Input will be closed.
 */
public actual fun Input.deflateAllAvailable(): ByteArray {
    return DeflateInput(this).use { it.readBytes() }
}


/**
 * [source] will be closed on [ZlibInput.close]
 */
internal class ZlibInput(
    private val source: Input,
    zlibInit: (z_streamp) -> Int,
    private val zlibProcess: (z_streamp, flush: Int) -> Int,
    private val zlibHasPending: ((z_streamp) -> Boolean)?, // null lambda means operation not defined
    private val zlibFlushMode: (shouldFlushAll: Boolean) -> Int,
    private val zlibEnd: (z_streamp) -> Int,
) : Input() {
    private val z: z_stream = nativeHeap.alloc()
    // Zlib manual: https://refspecs.linuxbase.org/LSB_3.0.0/LSB-Core-generic/LSB-Core-generic/zlib-inflate-1.html

    init {
        val r = zlibInit(z.ptr)
        if (r != 0) {
            nativeHeap.free(z)
            error("Failed to init zlib: $r (${getZlibError(r)})")
        }
    }

    private var bufferReadableSize = 0L
    private val inputBuffer = nativeHeap.allocArray<ByteVar>(ZLIB_BUFFER_SIZE)

    private var closed = false

    override fun close() {
        if (closed) return
        closed = true
        debug { "close" }
        super.close()
        debug { "freeing inputBuffer" }
        nativeHeap.free(inputBuffer)
        debug { "freed" }
    }

    override fun closeSource() {
        debug { "closeSource" }
        source.close()
        debug { "zlibEnd" }
        zlibEnd(z.ptr)
        debug { "zlibEnd done" }
    }

    override fun fill(destination: Memory, offset: Int, length: Int): Int {
        require(offset in 0..destination.size32) { "invalid offset: $offset" }
        require(length in 0..destination.size32) { "invalid length: $length" }
        require(offset + length in 0..destination.size32) { "invalid offset and length: $offset, $length" }

        debug { "prepare:  bufferReadableSize = $bufferReadableSize" }
        debug { "prepare: previous value: z.avail_in=${z.avail_in}, z.avail_out=${z.avail_out}" }

        val filled = try {
            if (z.avail_in == 0u) {

                // These two cases are similar.
//            if (z.avail_out == 0u) {
//                // Last time we used all the output, there is either something cached in Zlib, or no further source.
//            } else {
//                // We did not use all the inputs, meaning least time we used all avail_in.
//            }

                // bot input and output are used
                val flush = updateAvailIn() ?: return 0
                copyOutputsFromZlib(destination, offset, length, flush)
            } else {
                // Inputs not used up.
                copyOutputsFromZlib(destination, offset, length, Z_NO_FLUSH)
            }

        } catch (e: Throwable) {
            // If you throw this error up, ktor will somehow kill the process. (Ktor 2.0.2)
            debug { e.printStackTrace(); "" }
            return 0
        }
        check(filled in 0..length) { "Filled more than $length bytes: $filled" }
        check(filled in 0..destination.size) { "Filled more than ${destination.size} bytes: $filled" }
        return filled
    }

    private fun copyOutputsFromZlib(memory: Memory, offset: Int, length: Int, flush: Int): Int {
        debug { "copyOutputsFromZlib, memory.offset = $offset, memory.length=$length, memory.size=${memory.size}" }

        z.avail_out = length.convert()
        z.next_out = (memory.pointer + offset)!!.reinterpret()

        // We still have input, no need to update.
        debug { "Set z.avail_out=${z.avail_out}, z.next_out=(memory.pointer + offset)!!.reinterpret()" }
        debug { "Calling zlib, flush = $flush" }

        val p = zlibProcess(z.ptr, flush)
        when (p) {
            Z_BUF_ERROR -> error("Zlib failed to process data. (Z_BUF_ERROR)")
            Z_MEM_ERROR -> throw OutOfMemoryError("Insufficient native heap memory for Zlib. (Z_MEM_ERROR)")
            Z_STREAM_ERROR -> error("Zlib failed to process data. (Z_STREAM_ERROR)")
            Z_DATA_ERROR -> error("Zlib failed to process data. (Z_DATA_ERROR)")
            Z_NEED_DICT -> error("Zlib failed to process data. (Z_NEED_DICT)")
            else -> debug { "zlib: $p" }
        }
        val readSize = (length.toUInt() - z.avail_out).toInt()

        debug { "Zlib produced readSize=$readSize  bytes" }
//        debug { "Partial output: ${memory.readBytes(bufferReadableSize).toUHexString()}" }
        debug { "Now z.avail_in=${z.avail_in}, z.avail_out=${z.avail_out}" }

        if (p == Z_FINISH) {
            debug { "Zlib returned Z_FINISH. Ignoring result check." }
            return readSize
        }

        if (p == Z_STREAM_END) {
            debug { "Zlib returned Z_STREAM_END. Ignoring result check." }
            return readSize
        }

        if (bufferReadableSize == 0L && (z.avail_in == 0u && source.endOfInput)) {
            if (zlibHasPending?.invoke(z.ptr) == true) {
                // has pending. So the data must be incomplete.
                error("Failed to process data, possibly bad data inputted.")
//                    if (z.avail_in == 0u && source.endOfInput) {
//                        // no any input.
//                    } else {
//                        // there's some input, so we can still read.
//                    }
            } else {
                // no pending, but we should expect Z_FINISH in this case.
                error("Zlib read 0 byte, but it should not happen.")
            }
            // can't read
        }
        return readSize
    }

    private fun updateAvailIn(): Int? {
        val read = source.readAvailable(inputBuffer, 0, ZLIB_BUFFER_SIZE)
        if (read == 0L) {
            debug { "updateAvailIn: endOfInput, closing" }
            close() // automatically close
            return null // no more source available
        }
        bufferReadableSize = read
        z.avail_in = read.toUInt()
        val flush = zlibFlushMode(read < ZLIB_BUFFER_SIZE || source.endOfInput)
        debug { "inputBuffer content: " + inputBuffer.readBytes(read.toInt()).toUHexString() }
        z.next_in = inputBuffer.reinterpret()
        debug { "Updated availIn: z.avail_in=${z.avail_in}, z.next_in = inputBuffer.reinterpret()" }
        return flush
    }

    private companion object {
        private fun getZlibError(it: Int): String {
            return when (it) {
                Z_DATA_ERROR -> "Z_DATA_ERROR"
                Z_STREAM_ERROR -> "Z_STREAM_ERROR"
                else -> "Unknown error $it"
            }
        }


        private const val debugging = false
        private inline fun debug(string: () -> String) {
            if (debugging) println(string())
        }

        private inline fun debug() {
            if (debugging) println()
        }
    }
}


//private fun ByteArray.callImpl(
//    fn: (CValuesRef<uint8_tVar>, UInt, CValuesRef<SizedByteArray>) -> Boolean,
//    offset: Int,
//    length: Int
//): ByteArray {
//    checkOffsetAndLength(offset, length)
//
//    memScoped {
//        val r = alloc<SizedByteArray>()
//        if (!fn(toCValues().ptr.reinterpret<uint8_tVar>().plus(offset)!!, length.toUInt(), r.ptr)) {
//            throw IllegalStateException("Failed platform implementation call")
//        }
//        try {
//            return r.arr?.readBytes(r.size.toInt())!!
//        } finally {
//            free(r.arr)
//        }
//    }
//}