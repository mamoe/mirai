/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils.jvm

import io.ktor.utils.io.errors.*

@Suppress("DEPRECATION_ERROR")
public expect class JvmFile {
    public constructor(pathname: String)
    public constructor(parent: String, child: String)

    public fun getName(): String?
    public fun getParent(): String?
    public fun getParentFile(): JvmFile?

    public fun getPath(): String
    public fun isAbsolute(): Boolean
    public fun getAbsolutePath(): String
    public fun getAbsoluteFile(): JvmFile

    @Throws(IOException::class)
    public fun getCanonicalPath(): String

    @Throws(IOException::class)
    public fun getCanonicalFile(): JvmFile?

    @Throws(IOException::class)
    public fun createNewFile(): Boolean

    public fun length(): Long
    public fun delete(): Boolean
    public fun exists(): Boolean

    public fun listFiles(): Array<JvmFile>?

    public fun mkdir(): Boolean
    public fun mkdirs(): Boolean
    public fun renameTo(file: JvmFile): Boolean
}

@Suppress("DEPRECATION_ERROR")
@CompatibilityOnlyJvmFile
public expect fun JvmFile.writeText(text: String)

@Suppress("DEPRECATION_ERROR")
@CompatibilityOnlyJvmFile
public expect fun JvmFile.readText(): String

@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.FUNCTION)
@RequiresOptIn(
    "JvmFile is only used for compatibility and for simplifying expect/actual structure. " +
            "It allows you to use java.io.File in commonMain and is intended only for maintaining compatibility with legacy code in commonMain." +
            "JvmFile is not implemented in Native and you must deprecate the implementation on nativeMain as HIDDEN." +
            "Do not design new APIs using JvmFile."
)
public annotation class CompatibilityOnlyJvmFile