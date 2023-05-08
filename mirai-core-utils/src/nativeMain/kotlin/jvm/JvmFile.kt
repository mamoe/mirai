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
@Deprecated("JvmFile is not implemented on native", level = DeprecationLevel.HIDDEN)
@CompatibilityOnlyJvmFile
public actual class JvmFile {
    public actual fun getName(): String? = throw NotImplementedError()

    public actual fun getParent(): String? = throw NotImplementedError()

    public actual fun getParentFile(): JvmFile? = throw NotImplementedError()

    public actual fun getPath(): String = throw NotImplementedError()

    public actual fun isAbsolute(): Boolean = throw NotImplementedError()
    public actual fun getAbsolutePath(): String = throw NotImplementedError()
    public actual fun getAbsoluteFile(): JvmFile = throw NotImplementedError()

    @Throws(IOException::class)
    public actual fun getCanonicalPath(): String = throw NotImplementedError()

    @Throws(IOException::class)
    public actual fun getCanonicalFile(): JvmFile? = throw NotImplementedError()

    @Throws(IOException::class)
    public actual fun createNewFile(): Boolean = throw NotImplementedError()

    public actual fun length(): Long = throw NotImplementedError()

    public actual fun delete(): Boolean = throw NotImplementedError()
    public actual fun exists(): Boolean = throw NotImplementedError()

    public actual fun listFiles(): Array<JvmFile>? = throw NotImplementedError()

    public actual fun mkdir(): Boolean = throw NotImplementedError()

    public actual fun mkdirs(): Boolean = throw NotImplementedError()

    public actual fun renameTo(file: JvmFile): Boolean = throw NotImplementedError()

    public actual constructor(pathname: String) {
        throw NotImplementedError()
    }

    public actual constructor(parent: String, child: String) {
        throw NotImplementedError()
    }
}

@Suppress("DEPRECATION_ERROR")
@Deprecated("JvmFile is not implemented on native", level = DeprecationLevel.HIDDEN)
@CompatibilityOnlyJvmFile
public actual fun JvmFile.writeText(text: String) {
    throw NotImplementedError()
}

@Suppress("DEPRECATION_ERROR")
@Deprecated("JvmFile is not implemented on native", level = DeprecationLevel.HIDDEN)
@CompatibilityOnlyJvmFile
public actual fun JvmFile.readText(): String {
    throw NotImplementedError()
}
