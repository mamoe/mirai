/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmName("Utils")
@file:JvmMultifileClass

package net.mamoe.mirai.utils


import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.coroutines.io.ByteWriteChannel
import kotlinx.coroutines.io.readAvailable
import kotlinx.io.core.Output
import kotlinx.serialization.InternalSerializationApi
import java.io.OutputStream
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

// copyTo

/**
 * 从接收者管道读取所有数据并写入 [dst]. 不会关闭 [dst]
 */
@OptIn(InternalSerializationApi::class)
@MiraiExperimentalAPI
public suspend fun ByteReadChannel.copyTo(dst: OutputStream) {
    val buffer = ByteArray(2048)
    var size: Int
    while (this.readAvailable(buffer).also { size = it } > 0) {
        dst.write(buffer, 0, size)
    }
}

/**
 * 从接收者管道读取所有数据并写入 [dst]. 不会关闭 [dst]
 */
@MiraiExperimentalAPI
public suspend fun ByteReadChannel.copyTo(dst: Output) {
    val buffer = ByteArray(2048)
    var size: Int
    while (this.readAvailable(buffer).also { size = it } > 0) {
        dst.writeFully(buffer, 0, size)
    }
}

/**
 * 从接收者管道读取所有数据并写入 [dst]. 不会关闭 [dst]
 */
@MiraiExperimentalAPI
public suspend fun ByteReadChannel.copyTo(dst: ByteWriteChannel) {
    val buffer = ByteArray(2048)
    var size: Int
    while (this.readAvailable(buffer).also { size = it } > 0) {
        dst.writeFully(buffer, 0, size)
    }
}

// copyAndClose


/**
 * 从接收者管道读取所有数据并写入 [dst], 最终关闭 [dst]
 */
@MiraiExperimentalAPI
@OptIn(InternalSerializationApi::class)
public suspend fun ByteReadChannel.copyAndClose(dst: OutputStream) { // 在 JVM 这个 API 不是 internal 的
    try {
        val buffer = ByteArray(2048)
        var size: Int
        while (this.readAvailable(buffer).also { size = it } > 0) {
            dst.write(buffer, 0, size)
        }
    } finally {
        dst.close()
    }
}

/**
 * 从接收者管道读取所有数据并写入 [dst], 最终关闭 [dst]
 */
@MiraiExperimentalAPI
public suspend fun ByteReadChannel.copyAndClose(dst: Output) {
    try {
        val buffer = ByteArray(2048)
        var size: Int
        while (this.readAvailable(buffer).also { size = it } > 0) {
            dst.writeFully(buffer, 0, size)
        }
    } finally {
        dst.close()
    }
}

/**
 * 从接收者管道读取所有数据并写入 [dst], 最终关闭 [dst]
 */
@MiraiExperimentalAPI
public suspend fun ByteReadChannel.copyAndClose(dst: ByteWriteChannel) {
    @Suppress("DuplicatedCode")
    try {
        val buffer = ByteArray(2048)
        var size: Int
        while (this.readAvailable(buffer).also { size = it } > 0) {
            dst.writeFully(buffer, 0, size)
        }
    } finally {
        @Suppress("DuplicatedCode")
        dst.close(null)
    }
}

/**
 * 从接收者管道读取所有数据并写入 [dst], 最终关闭 [dst]
 */
@MiraiExperimentalAPI
public suspend fun ByteReadChannel.copyAndClose(dst: io.ktor.utils.io.ByteWriteChannel) {
    @Suppress("DuplicatedCode")
    try {
        val buffer = ByteArray(2048)
        var size: Int
        while (this.readAvailable(buffer).also { size = it } > 0) {
            dst.writeFully(buffer, 0, size)
        }
    } finally {
        @Suppress("DuplicatedCode")
        dst.close(null)
    }
}