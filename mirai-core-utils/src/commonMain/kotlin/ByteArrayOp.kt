/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmName("ByteArrayOpKt_common")

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlin.jvm.JvmName

public expect val DEFAULT_BUFFER_SIZE: Int

public fun String.md5(): ByteArray = toByteArray().md5()

public expect fun ByteArray.md5(offset: Int = 0, length: Int = size - offset): ByteArray

public fun String.sha1(): ByteArray = toByteArray().sha1()

public expect fun ByteArray.sha1(offset: Int = 0, length: Int = size - offset): ByteArray

public fun String.sha256(): ByteArray = toByteArray().sha256()

public expect fun ByteArray.sha256(offset: Int = 0, length: Int = size - offset): ByteArray

///////////////////////////////////////////////////////////////////////////
// How to choose 'inflate', 'inflateAllAvailable', 'InflateInput'?
//
// On JVM, performance 'inflateAllAvailable' > 'InflateInput' > 'inflate'
// On Native, performance 'inflateAllAvailable' = 'InflateInput' > 'inflate'
//
// So you should use `inflateAllAvailable` if you need have an Input and you need a ByteArray.
// If you have a ByteArray and you need an InputStream, use 'InflateInput'.
// Use 'inflate' only if the input and desired output type are both ByteArray.
//
// Specially if you are using `.decodeToString()` after reading a ByteArray, then you'd prefer 'InflateInput' with 'readText()'.
///////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////
// Processing ByteArray
///////////////////////////////////////////////////////////////////////////

public expect fun ByteArray.gzip(offset: Int = 0, length: Int = size - offset): ByteArray
public expect fun ByteArray.ungzip(offset: Int = 0, length: Int = size - offset): ByteArray

public expect fun ByteArray.inflate(offset: Int = 0, length: Int = size - offset): ByteArray
public expect fun ByteArray.deflate(offset: Int = 0, length: Int = size - offset): ByteArray


///////////////////////////////////////////////////////////////////////////
// Consuming input
///////////////////////////////////////////////////////////////////////////

/**
 * Input will be closed.
 */
public expect fun Input.gzipAllAvailable(): ByteArray

/**
 * Input will be closed.
 */
public expect fun Input.ungzipAllAvailable(): ByteArray

/**
 * Input will be closed.
 */
public expect fun Input.inflateAllAvailable(): ByteArray

/**
 * Input will be closed.
 */
public expect fun Input.deflateAllAvailable(): ByteArray

///////////////////////////////////////////////////////////////////////////
// Input adapters.
///////////////////////////////////////////////////////////////////////////

//@Suppress("FunctionName")
//public expect fun GzipCompressionInput(source: Input): Input // No GzipInputStream for decompression on JVM

/**
 * [source] will be closed on returned [Input.close]
 */
@Suppress("FunctionName")
public expect fun GzipDecompressionInput(source: Input): Input

/**
 * @see GzipDecompressionInput
 */
public fun Input.gzipDecompressionInput(): Input = GzipDecompressionInput(this)

/**
 * [source] will be closed on returned [Input.close]
 */
@Suppress("FunctionName")
public expect fun InflateInput(source: Input): Input

/**
 * @see InflateInput
 */
public fun Input.inflateInput(): Input = InflateInput(this)

/**
 * [source] will be closed on returned [Input.close]
 */
@Suppress("FunctionName")
public expect fun DeflateInput(source: Input): Input

/**
 * @see DeflateInput
 */
public fun Input.deflateInput(): Input = DeflateInput(this)