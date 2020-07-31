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

package java.nio.file

public expect open interface Path : Comparable<Path>, Iterable<Path>, Watchable {
    public open fun getFileSystem(): FileSystem
    public open fun isAbsolute(): Boolean
    public open fun getRoot(): Path
    public open fun getFileName(): Path
    public open fun getParent(): Path
    public open fun getNameCount(): Int
    public open fun getName(index: Int): Path
    public open fun subpath(beginIndex: Int, endIndex: Int): Path
    public open fun startsWith(other: Path): Boolean
    public open fun startsWith(other: String): Boolean
    public open fun endsWith(other: Path): Boolean
    public open fun endsWith(other: String): Boolean
    public open fun normalize(): Path
    public open fun resolve(other: Path): Path
    public open fun resolve(other: String): Path
    public open fun resolveSibling(other: Path): Path
    public open fun resolveSibling(other: String): Path
    public open fun relativize(other: Path): Path
    public open fun toUri(): java.net.URI
    public open fun toAbsolutePath(): Path

    //public open fun toRealPath(vararg options: LinkOption): Path
    public open fun toFile(): java.io.File

    //public open fun register(watcher: watcher, events: events, WatchEvent.Modifier[0]: new): return
    public open fun iterator(): Iterator<Path>
    public open fun hasNext(): Boolean
    public open fun next(): Path
    public open fun compareTo(other: Path): Int
    public open fun equals(other: Any?): Boolean
    public open fun hashCode(): Int
    public open fun toString(): String

    public companion object {
        //@JvmStatic
        public open fun of(first: String, vararg more: String): Path

        //@JvmStatic
        public open fun of(uri: java.net.URI): Path
    }
}  