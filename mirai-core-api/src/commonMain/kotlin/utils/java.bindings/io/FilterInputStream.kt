@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.io

public expect open class FilterInputStream : InputStream {
    public open fun read(): Int
    public open fun read(b: ByteArray): Int
    public open fun read(b: ByteArray, off: Int, len: Int): Int
    public open fun skip(n: Long): Long
    public open fun available(): Int
    public open fun close()
    public open fun mark(readlimit: Int)
    public open fun reset()
    public open fun markSupported(): Boolean
}  