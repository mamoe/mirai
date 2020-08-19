/*
 *
 *  * Copyright 2020 Mamoe Technologies and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/mamoe/mirai/blob/master/LICENSE
 *
 */

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

import kotlin.jvm.JvmStatic

public expect class File : Serializable, Comparable<File> {
    public constructor(parent: String, child: String)
    public constructor(parent: File, child: String)
    public constructor(uri: java.net.URI)

    public open fun getName(): String
    public open fun getParent(): String
    public open fun getParentFile(): File
    public open fun getPath(): String
    public open fun isAbsolute(): Boolean
    public open fun getAbsolutePath(): String
    public open fun getAbsoluteFile(): File
    public open fun getCanonicalPath(): String
    public open fun getCanonicalFile(): File
    public open fun toURL(): java.net.URL
    public open fun toURI(): java.net.URI
    public open fun canRead(): Boolean
    public open fun canWrite(): Boolean
    public open fun exists(): Boolean
    public open fun isDirectory(): Boolean
    public open fun isFile(): Boolean
    public open fun isHidden(): Boolean
    public open fun lastModified(): Long
    public open fun length(): Long
    public open fun createNewFile(): Boolean
    public open fun delete(): Boolean
    public open fun deleteOnExit()
    public open fun list(): Array<String>?

    //public open fun list(filter: FilenameFilter): Array<String>
    public open fun listFiles(): Array<File>?

    //public open fun listFiles(filter: FilenameFilter): Array<File>
    //public open fun listFiles(filter: FileFilter): Array<File>
    public open fun mkdir(): Boolean
    public open fun mkdirs(): Boolean
    public open fun renameTo(dest: File): Boolean
    public open fun setLastModified(time: Long): Boolean
    public open fun setReadOnly(): Boolean
    public open fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean
    public open fun setWritable(writable: Boolean): Boolean
    public open fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean
    public open fun setReadable(readable: Boolean): Boolean
    public open fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean
    public open fun setExecutable(executable: Boolean): Boolean
    public open fun canExecute(): Boolean
    public open fun getTotalSpace(): Long
    public open fun getFreeSpace(): Long
    public open fun getUsableSpace(): Long
    public open fun compareTo(pathname: File): Int
    public open fun equals(obj: Any?): Boolean
    public open fun hashCode(): Int
    public open fun toString(): String
    public open fun toPath(): java.nio.file.Path

    public companion object {
        @JvmStatic
        public val separatorChar: Char

        @JvmStatic
        public val separator: String

        @JvmStatic
        public val pathSeparatorChar: Char

        @JvmStatic
        public val pathSeparator: String

        @JvmStatic
        public fun createTempFile(prefix: String, suffix: String): File
    }
}