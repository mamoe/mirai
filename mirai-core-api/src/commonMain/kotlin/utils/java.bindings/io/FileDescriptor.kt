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
    "NON_FINAL_MEMBER_IN_OBJECT",
    "ConvertSecondaryConstructorToPrimary"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.io

import kotlin.jvm.JvmStatic

public expect final class FileDescriptor {
    public constructor()

    public open fun set(fdo: FileDescriptor, fd: Int)
    public open fun get(fdo: FileDescriptor): Int
    public open fun setAppend(fdo: FileDescriptor, append: Boolean)
    public open fun getAppend(fdo: FileDescriptor): Boolean
    public open fun close(fdo: FileDescriptor)
    public open fun registerCleanup(fdo: FileDescriptor)
    public open fun unregisterCleanup(fdo: FileDescriptor)
    public open fun setHandle(fdo: FileDescriptor, handle: Long)
    public open fun getHandle(fdo: FileDescriptor): Long
    public open fun valid(): Boolean
    public open fun sync()

    public companion object {
        @JvmStatic
        public val `in`: FileDescriptor

        @JvmStatic
        public val out: FileDescriptor

        @JvmStatic
        public val err: FileDescriptor
    }
}  