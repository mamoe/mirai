/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NO_ACTUAL_FOR_EXPECT", "PackageDirectoryMismatch")

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.io

import kotlinx.io.core.Closeable

public expect abstract class OutputStream : Closeable {
    public override fun close()
    public open fun flush()
    public open fun write(buffer: ByteArray, offset: Int, count: Int)
    public open fun write(buffer: ByteArray)
    public abstract fun write(oneByte: Int)
}
