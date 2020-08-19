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