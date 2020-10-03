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

public expect open class ByteArrayInputStream : InputStream {
    public constructor(buf: ByteArray)
    public constructor(buf: ByteArray, offset: Int, length: Int)

    public open fun read(): Int
    public open fun read(b: ByteArray, off: Int, len: Int): Int
    public open fun readAllBytes(): ByteArray
    public open fun readNBytes(b: ByteArray, off: Int, len: Int): Int
    public open fun transferTo(out: OutputStream): Long
    public open fun skip(n: Long): Long
    public open fun available(): Int
    public open fun markSupported(): Boolean
    public open fun mark(readAheadLimit: Int)
    public open fun reset()
    public open fun close()
}  