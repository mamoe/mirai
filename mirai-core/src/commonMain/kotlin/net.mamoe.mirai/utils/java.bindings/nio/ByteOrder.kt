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

import kotlin.jvm.JvmStatic

/**
 * A typesafe enumeration for byte orders.
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */
public expect class ByteOrder {
    public companion object {
        /**
         * Constant denoting big-endian byte order.  In this order, the bytes of a
         * multibyte value are ordered from most significant to least significant.
         */
        @JvmStatic
        public val BIG_ENDIAN: ByteOrder

        /**
         * Constant denoting little-endian byte order.  In this order, the bytes of
         * a multibyte value are ordered from least significant to most
         * significant.
         */
        @JvmStatic
        public val LITTLE_ENDIAN: ByteOrder

        /**
         * Retrieves the native byte order of the underlying platform.
         *
         *
         *  This method is defined so that performance-sensitive Java code can
         * allocate direct buffers with the same byte order as the hardware.
         * Native code libraries are often more efficient when such buffers are
         * used.
         *
         * @return  The native byte order of the hardware upon which this Java
         * virtual machine is running
         */
        @JvmStatic
        public fun nativeOrder(): ByteOrder
    }
}