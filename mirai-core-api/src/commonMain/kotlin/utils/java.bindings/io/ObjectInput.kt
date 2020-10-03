@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN",
    "REDUNDANT_MODIFIER_FOR_TARGET",
    "REDUNDANT_OPEN_IN_INTERFACE"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.io

public expect open interface ObjectInput : DataInput /*, AutoCloseable*/ {
    public open fun readObject(): Any?
    public open fun read(): Int
    public open fun read(b: ByteArray): Int
    public open fun read(b: ByteArray, off: Int, len: Int): Int
    public open fun skip(n: Long): Long
    public open fun available(): Int
    public open fun close()
}  