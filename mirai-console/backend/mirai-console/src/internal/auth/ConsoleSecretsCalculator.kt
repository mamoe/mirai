/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.auth

import net.mamoe.mirai.utils.SecretsProtection
import net.mamoe.mirai.utils.lateinitMutableProperty
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

internal class ConsoleSecretsCalculator(
    private val file: Path,
) {
    internal val consoleKey: SecretsProtection.EscapedByteBuffer get() = _consoleKey

    private var _consoleKey: SecretsProtection.EscapedByteBuffer by lateinitMutableProperty {
        loadOrCreate()
    }

    fun loadOrCreate(): SecretsProtection.EscapedByteBuffer {
        if (file.isRegularFile()) {
            return SecretsProtection.EscapedByteBuffer(file.readBytes())
        }

        file.parent?.createDirectories()
        val dataStream = ByteArrayOutputStream()
        val dataWriter = DataOutputStream(dataStream)

        repeat(3) {
            dataWriter.writeUTF(UUID.randomUUID().toString())
        }

        val data = dataStream.toByteArray()
        file.writeBytes(data)
        return SecretsProtection.EscapedByteBuffer(data)
    }

    fun reloadOrCreate() {
        _consoleKey = loadOrCreate()
    }
}