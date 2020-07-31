/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NO_ACTUAL_FOR_EXPECT", "PackageDirectoryMismatch")

/**
 * Bindings for JDK.
 *
 * All the sources are copied from OpenJDK. Copyright OpenJDK authors.
 */

package java.nio

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic


/**
 * A byte buffer.
 *
 *
 *  This class defines six categories of operations upon
 * byte buffers:
 *
 *
 *
 *  *
 *
 * Absolute and relative [&lt;i&gt;get&lt;/i&gt;][.get] and
 * [&lt;i&gt;put&lt;/i&gt;][.put] methods that read and write
 * single bytes;
 *
 *  *
 *
 * Absolute and relative [&lt;i&gt;bulk get&lt;/i&gt;][.get]
 * methods that transfer contiguous sequences of bytes from this buffer
 * into an array;
 *
 *  *
 *
 * Absolute and relative [&lt;i&gt;bulk put&lt;/i&gt;][.put]
 * methods that transfer contiguous sequences of bytes from a
 * byte array or some other byte
 * buffer into this buffer;
 *
 *
 *
 *  *
 *
 * Absolute and relative [&lt;i&gt;get&lt;/i&gt;][.getChar]
 * and [&lt;i&gt;put&lt;/i&gt;][.putChar] methods that read and
 * write values of other primitive types, translating them to and from
 * sequences of bytes in a particular byte order;
 *
 *  *
 *
 * Methods for creating *[view buffers](#views)*,
 * which allow a byte buffer to be viewed as a buffer containing values of
 * some other primitive type; and
 *
 *
 *
 *  *
 *
 * A method for [compacting][.compact]
 * a byte buffer.
 *
 *
 *
 *
 *  Byte buffers can be created either by [ &lt;i&gt;allocation&lt;/i&gt;][.allocate], which allocates space for the buffer's
 *
 *
 *
 * content, or by [&lt;i&gt;wrapping&lt;/i&gt;][.wrap] an
 * existing byte array  into a buffer.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * <a id="direct"></a>
 * <h2> Direct *vs.* non-direct buffers </h2>
 *
 *
 *  A byte buffer is either *direct* or *non-direct*.  Given a
 * direct byte buffer, the Java virtual machine will make a best effort to
 * perform native I/O operations directly upon it.  That is, it will attempt to
 * avoid copying the buffer's content to (or from) an intermediate buffer
 * before (or after) each invocation of one of the underlying operating
 * system's native I/O operations.
 *
 *
 *  A direct byte buffer may be created by invoking the [ ][.allocateDirect] factory method of this class.  The
 * buffers returned by this method typically have somewhat higher allocation
 * and deallocation costs than non-direct buffers.  The contents of direct
 * buffers may reside outside of the normal garbage-collected heap, and so
 * their impact upon the memory footprint of an application might not be
 * obvious.  It is therefore recommended that direct buffers be allocated
 * primarily for large, long-lived buffers that are subject to the underlying
 * system's native I/O operations.  In general it is best to allocate direct
 * buffers only when they yield a measureable gain in program performance.
 *
 *
 *  A direct byte buffer may also be created by [ ][java.nio.channels.FileChannel.map] a region of a file
 * directly into memory.  An implementation of the Java platform may optionally
 * support the creation of direct byte buffers from native code via JNI.  If an
 * instance of one of these kinds of buffers refers to an inaccessible region
 * of memory then an attempt to access that region will not change the buffer's
 * content and will cause an unspecified exception to be thrown either at the
 * time of the access or at some later time.
 *
 *
 *  Whether a byte buffer is direct or non-direct may be determined by
 * invoking its [isDirect][.isDirect] method.  This method is provided so
 * that explicit buffer management can be done in performance-critical code.
 *
 *
 * <a id="bin"></a>
 * <h2> Access to binary data </h2>
 *
 *
 *  This class defines methods for reading and writing values of all other
 * primitive types, except `boolean`.  Primitive values are translated
 * to (or from) sequences of bytes according to the buffer's current byte
 * order, which may be retrieved and modified via the [order][.order]
 * methods.  Specific byte orders are represented by instances of the [ ] class.  The initial order of a byte buffer is always [ ][ByteOrder.BIG_ENDIAN].
 *
 *
 *  For access to heterogeneous binary data, that is, sequences of values of
 * different types, this class defines a family of absolute and relative
 * *get* and *put* methods for each type.  For 32-bit floating-point
 * values, for example, this class defines:
 *
 * <blockquote><pre>
 * float  [.getFloat]
 * float  [getFloat(int index)][.getFloat]
 * void  [putFloat(float f)][.putFloat]
 * void  [putFloat(int index, float f)][.putFloat]</pre></blockquote>
 *
 *
 *  Corresponding methods are defined for the types `char,
 * short, int, long`, and `double`.  The index
 * parameters of the absolute *get* and *put* methods are in terms of
 * bytes rather than of the type being read or written.
 *
 * <a id="views"></a>
 *
 *
 *  For access to homogeneous binary data, that is, sequences of values of
 * the same type, this class defines methods that can create *views* of a
 * given byte buffer.  A *view buffer* is simply another buffer whose
 * content is backed by the byte buffer.  Changes to the byte buffer's content
 * will be visible in the view buffer, and vice versa; the two buffers'
 * position, limit, and mark values are independent.  The [ ][.asFloatBuffer] method, for example, creates an instance of
 * the [FloatBuffer] class that is backed by the byte buffer upon which
 * the method is invoked.  Corresponding view-creation methods are defined for
 * the types `char, short, int, long`, and `double`.
 *
 *
 *  View buffers have three important advantages over the families of
 * type-specific *get* and *put* methods described above:
 *
 *
 *
 *  *
 *
 * A view buffer is indexed not in terms of bytes but rather in terms
 * of the type-specific size of its values;
 *
 *  *
 *
 * A view buffer provides relative bulk *get* and *put*
 * methods that can transfer contiguous sequences of values between a buffer
 * and an array or some other buffer of the same type; and
 *
 *  *
 *
 * A view buffer is potentially much more efficient because it will
 * be direct if, and only if, its backing byte buffer is direct.
 *
 *
 *
 *
 *  The byte order of a view buffer is fixed to be that of its byte buffer
 * at the time that the view is created.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * <h2> Invocation chaining </h2>
 *
 *
 *
 *  Methods in this class that do not otherwise have a value to return are
 * specified to return the buffer upon which they are invoked.  This allows
 * method invocations to be chained.
 *
 *
 *
 * The sequence of statements
 *
 * <blockquote><pre>
 * bb.putInt(0xCAFEBABE);
 * bb.putShort(3);
 * bb.putShort(45);</pre></blockquote>
 *
 * can, for example, be replaced by the single statement
 *
 * <blockquote><pre>
 * bb.putInt(0xCAFEBABE).putShort(3).putShort(45);</pre></blockquote>
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
public expect abstract class ByteBuffer @JvmOverloads internal constructor(
    mark: Int, pos: Int, lim: Int, cap: Int,  // package-private
    hb: ByteArray? = null, offset: Int = 0
) : Buffer, Comparable<ByteBuffer?> {
    /**
     * Creates a new, read-only byte buffer that shares this buffer's
     * content.
     *
     *
     *  The content of the new buffer will be that of this buffer.  Changes
     * to this buffer's content will be visible in the new buffer; the new
     * buffer itself, however, will be read-only and will not allow the shared
     * content to be modified.  The two buffers' position, limit, and mark
     * values will be independent.
     *
     *
     *  The new buffer's capacity, limit, position,
     *
     * and mark values will be identical to those of this buffer, and its byte
     * order will be [BIG_ENDIAN][ByteOrder.BIG_ENDIAN].
     *
     *
     *
     *
     *
     *  If this buffer is itself read-only then this method behaves in
     * exactly the same way as the [duplicate][.duplicate] method.
     *
     * @return  The new, read-only byte buffer
     */
    public abstract fun asReadOnlyBuffer(): ByteBuffer?
    // -- Singleton get/put methods --
    /**
     * Relative *get* method.  Reads the byte at this buffer's
     * current position, and then increments the position.
     *
     * @return  The byte at the buffer's current position
     *
     * @throws  BufferUnderflowException
     * If the buffer's current position is not smaller than its limit
     */
    public abstract fun get(): Byte

    /**
     * Relative *put* method&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  Writes the given byte into this buffer at the current
     * position, and then increments the position.
     *
     * @param  b
     * The byte to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If this buffer's current position is not smaller than its limit
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public abstract fun put(b: Byte): ByteBuffer

    /**
     * Modifies this buffer's byte order.
     *
     * @param  bo
     * The new byte order,
     * either [BIG_ENDIAN][ByteOrder.BIG_ENDIAN]
     * or [LITTLE_ENDIAN][ByteOrder.LITTLE_ENDIAN]
     *
     * @return  This buffer
     */
    public fun order(bo: java.nio.ByteOrder): ByteBuffer

    /**
     * Absolute *get* method.  Reads the byte at the given
     * index.
     *
     * @param  index
     * The index from which the byte will be read
     *
     * @return  The byte at the given index
     *
     * @throws  IndexOutOfBoundsException
     * If `index` is negative
     * or not smaller than the buffer's limit
     */
    public abstract operator fun get(index: Int): Byte

    /**
     * Absolute *put* method&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  Writes the given byte into this buffer at the given
     * index.
     *
     * @param  index
     * The index at which the byte will be written
     *
     * @param  b
     * The byte value to be written
     *
     * @return  This buffer
     *
     * @throws  IndexOutOfBoundsException
     * If `index` is negative
     * or not smaller than the buffer's limit
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public abstract fun put(index: Int, b: Byte): ByteBuffer?
    // -- Bulk get operations --
    /**
     * Relative bulk *get* method.
     *
     *
     *  This method transfers bytes from this buffer into the given
     * destination array.  If there are fewer bytes remaining in the
     * buffer than are required to satisfy the request, that is, if
     * `length`&nbsp;`>`&nbsp;`remaining()`, then no
     * bytes are transferred and a [BufferUnderflowException] is
     * thrown.
     *
     *
     *  Otherwise, this method copies `length` bytes from this
     * buffer into the given array, starting at the current position of this
     * buffer and at the given offset in the array.  The position of this
     * buffer is then incremented by `length`.
     *
     *
     *  In other words, an invocation of this method of the form
     * `src.get(dst,&nbsp;off,&nbsp;len)` has exactly the same effect as
     * the loop
     *
     * <pre>`for (int i = off; i < off + len; i++)
     * dst[i] = src.get();
    `</pre> *
     *
     * except that it first checks that there are sufficient bytes in
     * this buffer and it is potentially much more efficient.
     *
     * @param  dst
     * The array into which bytes are to be written
     *
     * @param  offset
     * The offset within the array of the first byte to be
     * written; must be non-negative and no larger than
     * `dst.length`
     *
     * @param  length
     * The maximum number of bytes to be written to the given
     * array; must be non-negative and no larger than
     * `dst.length - offset`
     *
     * @return  This buffer
     *
     * @throws  BufferUnderflowException
     * If there are fewer than `length` bytes
     * remaining in this buffer
     *
     * @throws  IndexOutOfBoundsException
     * If the preconditions on the `offset` and `length`
     * parameters do not hold
     */
    public open operator fun get(dst: ByteArray, offset: Int, length: Int): ByteBuffer

    /**
     * Relative bulk *get* method.
     *
     *
     *  This method transfers bytes from this buffer into the given
     * destination array.  An invocation of this method of the form
     * `src.get(a)` behaves in exactly the same way as the invocation
     *
     * <pre>
     * src.get(a, 0, a.length) </pre>
     *
     * @param   dst
     * The destination array
     *
     * @return  This buffer
     *
     * @throws  BufferUnderflowException
     * If there are fewer than `length` bytes
     * remaining in this buffer
     */
    public operator fun get(dst: ByteArray): ByteBuffer

    // -- Bulk put operations --
    /**
     * Relative bulk *put* method&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  This method transfers the bytes remaining in the given source
     * buffer into this buffer.  If there are more bytes remaining in the
     * source buffer than in this buffer, that is, if
     * `src.remaining()`&nbsp;`>`&nbsp;`remaining()`,
     * then no bytes are transferred and a [ ] is thrown.
     *
     *
     *  Otherwise, this method copies
     * *n*&nbsp;=&nbsp;`src.remaining()` bytes from the given
     * buffer into this buffer, starting at each buffer's current position.
     * The positions of both buffers are then incremented by *n*.
     *
     *
     *  In other words, an invocation of this method of the form
     * `dst.put(src)` has exactly the same effect as the loop
     *
     * <pre>
     * while (src.hasRemaining())
     * dst.put(src.get()); </pre>
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient.
     *
     * @param  src
     * The source buffer from which bytes are to be read;
     * must not be this buffer
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If there is insufficient space in this buffer
     * for the remaining bytes in the source buffer
     *
     * @throws  IllegalArgumentException
     * If the source buffer is this buffer
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public fun put(src: ByteBuffer): ByteBuffer

    /**
     * Relative bulk *put* method&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  This method transfers bytes into this buffer from the given
     * source array.  If there are more bytes to be copied from the array
     * than remain in this buffer, that is, if
     * `length`&nbsp;`>`&nbsp;`remaining()`, then no
     * bytes are transferred and a [BufferOverflowException] is
     * thrown.
     *
     *
     *  Otherwise, this method copies `length` bytes from the
     * given array into this buffer, starting at the given offset in the array
     * and at the current position of this buffer.  The position of this buffer
     * is then incremented by `length`.
     *
     *
     *  In other words, an invocation of this method of the form
     * `dst.put(src,&nbsp;off,&nbsp;len)` has exactly the same effect as
     * the loop
     *
     * <pre>`for (int i = off; i < off + len; i++)
     * dst.put(src[i]);
    `</pre> *
     *
     * except that it first checks that there is sufficient space in this
     * buffer and it is potentially much more efficient.
     *
     * @param  src
     * The array from which bytes are to be read
     *
     * @param  offset
     * The offset within the array of the first byte to be read;
     * must be non-negative and no larger than `src.length`
     *
     * @param  length
     * The number of bytes to be read from the given array;
     * must be non-negative and no larger than
     * `src.length - offset`
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If there is insufficient space in this buffer
     *
     * @throws  IndexOutOfBoundsException
     * If the preconditions on the `offset` and `length`
     * parameters do not hold
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public open fun put(src: ByteArray, offset: Int, length: Int): ByteBuffer

    /**
     * Relative bulk *put* method&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  This method transfers the entire content of the given source
     * byte array into this buffer.  An invocation of this method of the
     * form `dst.put(a)` behaves in exactly the same way as the
     * invocation
     *
     * <pre>
     * dst.put(a, 0, a.length) </pre>
     *
     * @param   src
     * The source array
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If there is insufficient space in this buffer
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public fun put(src: ByteArray): ByteBuffer

    /**
     * Relative *put* method for writing an int
     * value&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  Writes four bytes containing the given int value, in the
     * current byte order, into this buffer at the current position, and then
     * increments the position by four.
     *
     * @param  value
     * The int value to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If there are fewer than four bytes
     * remaining in this buffer
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public abstract fun putInt(value: Int): ByteBuffer

    /**
     * Relative *put* method for writing a long
     * value&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  Writes eight bytes containing the given long value, in the
     * current byte order, into this buffer at the current position, and then
     * increments the position by eight.
     *
     * @param  value
     * The long value to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If there are fewer than eight bytes
     * remaining in this buffer
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public abstract fun putLong(value: Long): ByteBuffer

    /**
     * Absolute *get* method for reading a long value.
     *
     *
     *  Reads eight bytes at the given index, composing them into a
     * long value according to the current byte order.
     *
     * @param  index
     * The index from which the bytes will be read
     *
     * @return  The long value at the given index
     *
     * @throws  IndexOutOfBoundsException
     * If `index` is negative
     * or not smaller than the buffer's limit,
     * minus seven
     */
    public abstract fun getLong(index: Int): Long

    /**
     * Relative *put* method for writing a float
     * value&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  Writes four bytes containing the given float value, in the
     * current byte order, into this buffer at the current position, and then
     * increments the position by four.
     *
     * @param  value
     * The float value to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If there are fewer than four bytes
     * remaining in this buffer
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public abstract fun putFloat(value: Float): ByteBuffer

    /**
     * Relative *put* method for writing a double
     * value&nbsp;&nbsp;*(optional operation)*.
     *
     *
     *  Writes eight bytes containing the given double value, in the
     * current byte order, into this buffer at the current position, and then
     * increments the position by eight.
     *
     * @param  value
     * The double value to be written
     *
     * @return  This buffer
     *
     * @throws  BufferOverflowException
     * If there are fewer than eight bytes
     * remaining in this buffer
     *
     * @throws  ReadOnlyBufferException
     * If this buffer is read-only
     */
    public abstract fun putDouble(value: Double): ByteBuffer

    public companion object {
        /**
         * Allocates a new byte buffer.
         *
         *
         *  The new buffer's position will be zero, its limit will be its
         * capacity, its mark will be undefined, each of its elements will be
         * initialized to zero, and its byte order will be
         *
         * [BIG_ENDIAN][ByteOrder.BIG_ENDIAN].
         *
         *
         *
         *
         * It will have a [backing array][.array], and its
         * [array offset][.arrayOffset] will be zero.
         *
         * @param  capacity
         * The new buffer's capacity, in bytes
         *
         * @return  The new byte buffer
         *
         * @throws  IllegalArgumentException
         * If the `capacity` is a negative integer
         */
        @JvmStatic
        public fun allocate(capacity: Int): ByteBuffer
    }
}
