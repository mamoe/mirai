/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN",
    "RedundantModalityModifier",
    "REDUNDANT_MODIFIER_FOR_TARGET",
    "REDUNDANT_OPEN_IN_INTERFACE",
    "NON_FINAL_MEMBER_IN_OBJECT"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.io

public expect open class FileOutputStream : OutputStream {
    public constructor(name: String)
    public constructor(name: String, append: Boolean)
    public constructor(file: File)
    public constructor(file: File, append: Boolean)
    public constructor(fdObj: FileDescriptor)

    public open fun write(b: Int)
    public open fun write(b: ByteArray)
    public open fun write(b: ByteArray, off: Int, len: Int)
    public open fun close()
    public final fun getFD(): FileDescriptor

    //public open fun getChannel(): java.nio.channels.FileChannel
    protected final fun finalize()
}  