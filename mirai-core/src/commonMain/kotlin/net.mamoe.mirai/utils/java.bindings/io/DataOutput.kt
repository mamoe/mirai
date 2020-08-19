@file:Suppress(
    "unused",
    "NO_ACTUAL_FOR_EXPECT",
    "PackageDirectoryMismatch",
    "NON_FINAL_MEMBER_IN_FINAL_CLASS",
    "VIRTUAL_MEMBER_HIDDEN",
    "RedundantModalityModifier",
    "REDUNDANT_MODIFIER_FOR_TARGET",
    "REDUNDANT_OPEN_IN_INTERFACE"
)

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.io

public expect open interface DataOutput {
    public open fun write(b: Int)
    public open fun write(b: ByteArray)
    public open fun write(b: ByteArray, off: Int, len: Int)
    public open fun writeBoolean(v: Boolean)
    public open fun writeByte(v: Int)
    public open fun writeShort(v: Int)
    public open fun writeChar(v: Int)
    public open fun writeInt(v: Int)
    public open fun writeLong(v: Long)
    public open fun writeFloat(v: Float)
    public open fun writeDouble(v: Double)
    public open fun writeBytes(s: String)
    public open fun writeChars(s: String)
    public open fun writeUTF(s: String)
}  