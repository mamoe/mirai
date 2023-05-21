/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.cinterop.*
import openssl.*

@OptIn(UnsafeNumber::class)
internal fun getOpenSSLError(): String {
    memScoped {
        val bio = BIO_new(BIO_s_mem())
        val errBuffer = allocPointerTo<ByteVar>()

        ERR_print_errors(bio)
        BIO_ctrl(bio, BIO_CTRL_FLUSH, 0, null)
        BIO_ctrl(bio, BIO_CTRL_INFO, 0, errBuffer.ptr)

        return errBuffer.value?.toKString()?.also { BIO_free(bio) } ?: "openssl error: no message"
    }
}