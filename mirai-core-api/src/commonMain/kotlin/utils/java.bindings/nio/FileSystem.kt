/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

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

package java.nio.file

import java.io.Closeable

public expect abstract class FileSystem : Closeable {
    //public abstract fun provider(): java.nio.file.spi.FileSystemProvider
    public abstract fun close()
    public abstract fun isOpen(): Boolean
    public abstract fun isReadOnly(): Boolean
    public abstract fun getSeparator(): String
    public abstract fun getRootDirectories(): Iterable<Path>

    //public abstract fun getFileStores(): Iterable<FileStore>
    public abstract fun supportedFileAttributeViews(): Set<String>
    public abstract fun getPath(first: String, vararg more: String): Path
    //public abstract fun getPathMatcher(syntaxAndPattern: String): PathMatcher
    //public abstract fun getUserPrincipalLookupService(): UserPrincipalLookupService
    // public abstract fun newWatchService(): WatchService
}  