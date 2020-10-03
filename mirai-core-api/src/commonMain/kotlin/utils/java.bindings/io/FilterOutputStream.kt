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

public expect open class FilterOutputStream : OutputStream {
    public open fun write(b: Int)
    public open fun write(b: ByteArray)
    public open fun write(b: ByteArray, off: Int, len: Int)
    public open fun flush()
    public open fun close()
}  