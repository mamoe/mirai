/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import platform.posix.*

private fun readlink(path: String): String = memScoped {
    val len = realpath(path, null)
    if (len != null) {
        try {
            return len.toKString()
        } finally {
            free(len)
        }
    } else {
        when (val errno = errno) {
            ENOTDIR -> return@memScoped path
            EACCES -> return@memScoped path // permission denied
            ENOENT -> return@memScoped path // no such file
            else -> throw IllegalArgumentException(
                "Invalid path($errno): $path",
                cause = PosixException.forErrno(posixFunctionName = "realpath()")
            )
        }
    }
}

internal actual class MiraiFileImpl actual constructor(
    override val path: String,
) : MiraiFile {
    actual companion object {
        private const val SEPARATOR = '/'
        private val ROOT by lazy { MiraiFileImpl("/") }

        @Suppress("UnnecessaryOptInAnnotation")
        @OptIn(UnsafeNumber::class)
        actual fun getWorkingDir(): MiraiFile {
            val path = memScoped {
                ByteArray(PATH_MAX).usePinned {
                    getcwd(it.addressOf(0), it.get().size.convert())
                    it.get().toKString()
                }
            }
            return MiraiFile.create(path)
        }
    }

    override val absolutePath: String by lazy { kotlin.run { readlink(path) } }

    override val parent: MiraiFile? by lazy {
        val absolutePath = absolutePath
        val p = absolutePath.substringBeforeLast(SEPARATOR, "")
        if (p.isEmpty()) {
            if (absolutePath.singleOrNull() == SEPARATOR) return@lazy null // root
            else return@lazy ROOT
        }
        MiraiFileImpl(p)
    }

    override val name: String
        get() = absolutePath.substringAfterLast('/', "").ifEmpty { absolutePath }

    init {
        absolutePath.split('/').forEach { checkName(it) }
    }

    private fun checkName(name: String) {
        name.forEach { c ->
            if (c in """\/:?*"><|""") {
                throw IllegalArgumentException("'${name}' contains illegal character '$c'.")
            }
        }
    }

    override val length: Long
        get() = useStat { it.st_size.convert() } ?: 0

    @OptIn(UnsafeNumber::class)
    override val isFile: Boolean
        get() = useStat { it.st_mode.convert<UInt>() flag S_IFREG } ?: false

    @OptIn(UnsafeNumber::class)
    override val isDirectory: Boolean
        get() = useStat { it.st_mode.convert<UInt>() flag S_IFDIR } ?: false

    override fun exists(): Boolean = useStat { true } ?: false

    override fun resolve(path: String): MiraiFile {
        when (path) {
            "." -> return this
            ".." -> return parent ?: this // root
        }

        if (path.startsWith(SEPARATOR)) {
            return MiraiFileImpl(path)
        }

        return MiraiFileImpl("$absolutePath/$path")
    }

    override fun resolve(file: MiraiFile): MiraiFile {
        val parent = file.parent ?: return resolve(file.name)
        return resolve(parent).resolve(file.name)
    }

    override fun createNewFile(): Boolean {
        memScoped {
            val fp = fopen(absolutePath, "w")
            fwrite(fp, 0, 0, fp)
            fclose(fp)
            return true
        }
    }

    override fun delete(): Boolean {
        return if (isFile) {
            remove(absolutePath) == 0
        } else {
            rmdir(absolutePath) == 0
        }
    }

    override fun mkdir(): Boolean {
        @Suppress("UnnecessaryOptInAnnotation") // bug
        @OptIn(UnsafeNumber::class)
        return (mkdir("$absolutePath/", "755".toUShort(8).convert()).convert<Int>() == 0)
    }

    @OptIn(UnsafeNumber::class)
    override fun mkdirs(): Boolean {
        val flags = useStat { it.st_mode.convert<UInt>() }
        return when {
            flags == null -> {
                this.parent?.mkdirs()
                mkdir()
            }
            flags flag S_IFDIR -> {
                false // already exists
            }
            else -> {
                mkdir()
            }
        }
    }

    override fun input(): Input {
        val handle = fopen(absolutePath, "rb")
            ?: throw IOException(
                "Failed to open file '$absolutePath'",
                PosixException.forErrno(posixFunctionName = "fopen()")
            )
        return PosixInputForFile(handle)
    }

    override fun output(): Output {
        val handle = fopen(absolutePath, "wb")
            ?: throw IOException(
                "Failed to open file '$absolutePath'",
                PosixException.forErrno(posixFunctionName = "fopen()")
            )
        return PosixFileInstanceOutput(handle)
    }

    override fun hashCode(): Int {
        return this.path.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (!isSameType(this, other)) return false
        return this.path == other.path
    }

    override fun toString(): String {
        return "MiraiFileImpl($path)"
    }
}