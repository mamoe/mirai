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

public expect abstract class InputStream : Closeable {
    public open fun available(): Int
    public open fun read(): Int
    public open fun read(b: ByteArray, off: Int, len: Int): Int
    public open fun readAllBytes(): ByteArray
    public open fun readNBytes(b: ByteArray, off: Int, len: Int): Int
    public open fun readNBytes(len: Int): ByteArray
    public open fun skip(n: Long): Long
    public open fun close()
    public open fun read(b: ByteArray): Int
    public open fun mark(readlimit: Int)
    public open fun reset()
    public open fun markSupported(): Boolean

    //public open fun transferTo(out: OutputStream): Long
    public companion object {
        @JvmStatic
        public open fun nullInputStream(): InputStream
    }
}  