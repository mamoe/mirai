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

public expect open interface DataInput {
    public open fun readFully(b: ByteArray)
    public open fun readFully(b: ByteArray, off: Int, len: Int)
    public open fun skipBytes(n: Int): Int
    public open fun readBoolean(): Boolean
    public open fun readByte(): Byte
    public open fun readUnsignedByte(): Int
    public open fun readShort(): Short
    public open fun readUnsignedShort(): Int
    public open fun readChar(): Char
    public open fun readInt(): Int
    public open fun readLong(): Long
    public open fun readFloat(): Float
    public open fun readDouble(): Double
    public open fun readLine(): String
    public open fun readUTF(): String
}  