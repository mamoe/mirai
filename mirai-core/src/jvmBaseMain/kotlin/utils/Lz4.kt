/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException


internal actual class Lz4 {
    actual companion object {
        actual fun decode(data: ByteArray): ByteArray? {
            var decompressed: ByteArray? = null
            val byteOutputStream = ByteArrayOutputStream()

            try {
                val byteInputStream = ByteArrayInputStream(data, 0, data.size)
                val `in` = BufferedInputStream(byteInputStream)
                val lz4InputStream: BlockLZ4CompressorInputStream =
                    BlockLZ4CompressorInputStream(
                        `in`
                    )
                var count = 0
                var totalCount = 0
                val buffer = ByteArray(data.size * 2)
                while (lz4InputStream.read(buffer).also { count = it } != -1) {
                    totalCount += count
                    byteOutputStream.write(buffer, 0, count)
                }
                logger.verbose("total size decompressed : $totalCount")
                logger.verbose("number of compressed bytes decompressed into stream : " + lz4InputStream.getCompressedCount())
                byteOutputStream.flush()
                decompressed = byteOutputStream.toByteArray()
                byteOutputStream.close()
                lz4InputStream.close()
                logger.verbose(
                    "input compressed length : " + data.size + ", output decompressed length : "
                            + decompressed.size
                )
            } catch (e: IOException) {
                logger.error("Error ! IOException thrown by LZ4 buffer streams : " + e.message)
            } catch (e: Exception) {
                logger.error("Error ! General exception thrown : " + e.message)
            }

            return decompressed
        }
    }
}