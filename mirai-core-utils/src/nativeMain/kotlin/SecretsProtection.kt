/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer

internal actual object SecretsProtectionPlatform {
    actual fun impl_asString(data: Any): String {
        return (data as ByteArray).decodeToString()
    }

    actual fun impl_asByteArray(data: Any): ByteArray {
        return data as ByteArray
    }

    actual fun impl_getSize(data: Any): Int {
        return data.cast<ByteArray>().size
    }

    actual fun escape(data: ByteArray): Any {
        return data
    }

    actual object EscapedStringSerializer : KSerializer<SecretsProtection.EscapedString> by String.serializer().map(
        String.serializer().descriptor.copy("EscapedString"),
        deserialize = { SecretsProtection.EscapedString(it.encodeToByteArray()) },
        serialize = { it.data.cast<ByteArray>().decodeToString() }
    )

    actual object EscapedByteBufferSerializer :
        KSerializer<SecretsProtection.EscapedByteBuffer> by ByteArraySerializer().map(
            ByteArraySerializer().descriptor.copy("EscapedByteBuffer"),
            deserialize = { SecretsProtection.EscapedByteBuffer(it) },
            serialize = { it.data.cast() }
        )


}