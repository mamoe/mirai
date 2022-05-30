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
import kotlinx.cinterop.*
import platform.posix.fopen
import platform.windows.*


internal actual class MiraiFileImpl actual constructor(
    // canonical
    path: String
) : MiraiFile {
    override val path = path.replace("/", "\\")

    companion object {
        private val ROOT_REGEX = Regex("""^([a-zA-z]+:[/\\])""")
        private const val SEPARATOR = '\\'
    }

    override val absolutePath: String = kotlin.run {
        val result = ROOT_REGEX.matchEntire(path) ?: return@run path.dropLastWhile { it.isSeparator() }
        return@run result.groups.first()!!.value
    }

    private fun Char.isSeparator() = this == '/' || this == '\\'

    override val parent: MiraiFile? by lazy {
        val absolute = absolutePath
        val p = absolute.substringBeforeLast(SEPARATOR, "")
        if (p.isEmpty()) {
            return@lazy null
        }
        if (p.lastOrNull() == ':') {
            if (absolute.lastIndexOf(SEPARATOR) == p.lastIndex) {
                // file is C:/
                return@lazy null
            } else {
                return@lazy MiraiFileImpl("$p/") // file is C:/xxx
            }
        }
        MiraiFileImpl(p)
    }

    override val name: String
        get() = if (absolutePath.matches(ROOT_REGEX)) absolutePath
        else absolutePath.substringAfterLast('/')

    init {
        checkName(absolutePath.substringAfterLast('/')) // do not check drive letter
    }

    private fun checkName(name: String) {
        name.substringAfterLast('/').forEach { c ->
            if (c in """\/:?*"><|""") {
                throw IllegalArgumentException("'${name}' contains illegal character '$c'.")
            }
        }

        memScoped {
            val b = alloc<WINBOOLVar>()
            CheckNameLegalDOS8Dot3A(absolutePath, nullPtr(), 0, nullPtr(), b.ptr)
            if (b.value != 1) {
                throw IllegalArgumentException("'${name}' contains illegal character.")
            }
        }
    }

    override val length: Long
        get() = useStat { it.st_size.convert() } ?: 0


    override val isFile: Boolean
        get() = getFileAttributes() flag FILE_ATTRIBUTE_NORMAL

    override val isDirectory: Boolean
        get() = getFileAttributes() flag FILE_ATTRIBUTE_DIRECTORY

    override fun exists(): Boolean = getFileAttributes() != INVALID_FILE_ATTRIBUTES

    private fun getFileAttributes(): DWORD = memScoped { GetFileAttributesA(absolutePath) }

    override fun resolve(path: String): MiraiFile {
        when (path) {
            "." -> return this
            ".." -> return parent ?: this // root
        }

        if (ROOT_REGEX.find(path) != null) { // absolute
            return MiraiFileImpl(path)
        }

        return MiraiFileImpl(this.absolutePath + SEPARATOR + path) // assuming path is 'appendable'
    }

    override fun resolve(file: MiraiFile): MiraiFile {
        val parent = file.parent ?: return resolve(file.name)
        return resolve(parent).resolve(file.name)
    }

    override fun createNewFile(): Boolean {
        memScoped {
            // https://docs.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilea
            val handle = CreateFileA(
                absolutePath,
                GENERIC_READ,
                FILE_SHARE_WRITE,
                nullPtr(),
                CREATE_NEW,
                FILE_ATTRIBUTE_NORMAL,
                nullPtr()
            )
            if (handle == NULL) return false
            CloseHandle(handle)
            return true
        }
    }

    override fun delete(): Boolean {
        return if (isFile) {
            DeleteFileA(absolutePath) == 0
        } else {
            RemoveDirectoryA(absolutePath) == 0
        }
    }

    override fun mkdir(): Boolean {
        memScoped {
            val v = alloc<_SECURITY_ATTRIBUTES>()
            return CreateDirectoryA(absolutePath, v.ptr) == 0
        }
    }

    override fun mkdirs(): Boolean {
        if (this.parent?.mkdirs() == false) {
            return false
        }
        return mkdir()
    }

    override fun input(): Input {
        val handle = fopen(absolutePath, "r")
        if (handle == NULL) throw IllegalStateException("Failed to open file '$absolutePath'")
        return PosixInputForFile(handle!!)
    }

    override fun output(): Output {
        val handle = fopen(absolutePath, "w")
        if (handle == NULL) throw IllegalStateException("Failed to open file '$absolutePath'")
        return PosixFileInstanceOutput(handle!!)
    }
}