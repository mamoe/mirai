/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*

private fun getFullPathName(path: String): String = memScoped {
    ShortArray(MAX_PATH).usePinned { pin ->
        val len = GetFullPathNameW(path, MAX_PATH, pin.addressOf(0).reinterpret(), null).toInt()
        if (len != 0) {
            return pin.get().toKStringFromUtf16(len)
        } else {
            when (val errno = errno) {
                ENOTDIR -> return@memScoped path
                EACCES -> return@memScoped path // permission denied
                ENOENT -> return@memScoped path // no such file
                else -> throw IllegalArgumentException(
                    "Invalid path($errno): $path",
                    cause = PosixException.forErrno(posixFunctionName = "GetFullPathNameW()")
                )
            }
        }
    }
}

private fun ShortArray.toKStringFromUtf16(len: Int): String {
    val chars = CharArray(len)
    var index = 0
    while (index < len) {
        chars[index] = this[index].toInt().toChar()
        ++index
    }
    return chars.concatToString()
}

internal actual class MiraiFileImpl actual constructor(
    // canonical
    path: String
) : MiraiFile {
    override val path = path.replace("/", "\\")

    actual companion object {
        private val ROOT_REGEX = Regex("""^([a-zA-z]+:[/\\])""")
        private const val SEPARATOR = '\\'

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

    override val absolutePath: String by lazy {
        val result = ROOT_REGEX.matchEntire(this.path)
            ?: return@lazy getFullPathName(this.path).removeSuffix(SEPARATOR.toString())
        return@lazy result.groups.first()!!.value
    }

    private fun Char.isSeparator() = this == '/' || this == '\\'

    override val parent: MiraiFile? by lazy {
        if (ROOT_REGEX.matchEntire(this.path) != null) return@lazy null
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
        else absolutePath.substringAfterLast(SEPARATOR)

    init {
        checkName(absolutePath.substringAfterLast(SEPARATOR)) // do not check drive letter
    }

    private fun checkName(name: String) {
        name.substringAfterLast(SEPARATOR).forEach { c ->
            if (c in """\/:?*"><|""") {
                throw IllegalArgumentException("'${name}' contains illegal character '$c'.")
            }
        }

//        memScoped {
//            val b = alloc<WINBOOLVar>()
//            CheckNameLegalDOS8Dot3A(absolutePath, nullPtr(), 0, nullPtr(), b.ptr)
//            if (b.value != 1) {
//                throw IllegalArgumentException("'${name}' contains illegal character.")
//            }
//        }
    }

    override val length: Long
        get() = useStat { it.st_size.convert() } ?: 0
//            memScoped {
//                val handle = CreateFileW(
//                    absolutePath,
//                    GENERIC_READ,
//                    FILE_SHARE_READ,
//                    null,
//                    OPEN_EXISTING,
//                    FILE_ATTRIBUTE_NORMAL,
//                    null
//                ) ?: return@memScoped 0
//                val length = alloc<DWORDVar>()
//                if (GetFileSize(handle, length.ptr) == INVALID_FILE_SIZE) {
//                    if (GetLastError() == NO_ERROR.toUInt()) {
//                        return INVALID_FILE_SIZE.convert()
//                    }
//                    throw PosixException.forErrno(posixFunctionName = "GetFileSize()").wrapIO()
//                }
//                if (CloseHandle(handle) == FALSE) {
//                    throw PosixException.forErrno(posixFunctionName = "CloseHandle()").wrapIO()
//                }
//                length.value.convert()
//            }


    override val isFile: Boolean
        get() = useStat { it.st_mode.convert<UInt>() flag S_IFREG } ?: false

    override val isDirectory: Boolean
        get() = useStat { it.st_mode.convert<UInt>() flag S_IFDIR } ?: false

    override fun exists(): Boolean = getFileAttributes() != INVALID_FILE_ATTRIBUTES

    private fun getFileAttributes(): DWORD = memScoped { GetFileAttributesW(absolutePath) }

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
        // https://docs.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilea
        val handle = CreateFileW(
            absolutePath,
            GENERIC_READ,
            FILE_SHARE_DELETE,
            null,
            CREATE_NEW,
            FILE_ATTRIBUTE_NORMAL,
            null
        )
        if (handle == null || handle == INVALID_HANDLE_VALUE) {
            return false
        }
        if (CloseHandle(handle) == FALSE) {
            throw PosixException.forErrno(posixFunctionName = "CloseHandle()").wrapIO()
        }
        return true
    }

    override fun delete(): Boolean {
        return if (isFile) {
            DeleteFileW(absolutePath) != 0
        } else {
            RemoveDirectoryW(absolutePath) != 0
        }
    }

    override fun mkdir(): Boolean {
        memScoped {
            val v = alloc<_SECURITY_ATTRIBUTES>()
            return CreateDirectoryW(absolutePath, v.ptr) != 0
        }
    }

    override fun mkdirs(): Boolean {
        this.parent?.mkdirs()
        return mkdir()
    }

    override fun input(): Input {
//        println(absolutePath)
//        val handle2 = fopen(absolutePath, "rb") ?:throw IOException(
//            "Failed to open file '$absolutePath'",
//            PosixException.forErrno(posixFunctionName = "fopen()")
//        )
//        return PosixInputForFile(handle2)
        // Will get I/O operation failed due to posix error code 2

        val handle = CreateFileW(
            absolutePath,
            GENERIC_READ,
            FILE_SHARE_DELETE,
            null,
            OPEN_EXISTING,
            FILE_ATTRIBUTE_NORMAL,
            null
        )
        if (handle == null || handle == INVALID_HANDLE_VALUE) throw IOException(
            "Failed to open file '$absolutePath'",
            PosixException.forErrno(posixFunctionName = "CreateFileW()")
        )
        return WindowsFileInput(handle)
    }

    override fun output(): Output {
//        val handle2 = fopen(absolutePath, "wb")
//            ?: throw IOException(
//                "Failed to open file '$absolutePath'",
//                PosixException.forErrno(posixFunctionName = "fopen()")
//            )
//        return PosixFileInstanceOutput(handle)
//
        val handle = CreateFileW(
            absolutePath,
            GENERIC_WRITE,
            FILE_SHARE_DELETE,
            null,
            (if (exists()) TRUNCATE_EXISTING else CREATE_NEW).toUInt(),
            FILE_ATTRIBUTE_NORMAL,
            null
        )
        if (handle == null || handle == INVALID_HANDLE_VALUE) throw IOException(
            "Failed to open file '$absolutePath'",
            PosixException.forErrno(posixFunctionName = "CreateFileW()")
        )
        return WindowsFileOutput(handle)
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


internal class WindowsFileInput(private val file: HANDLE) : Input() {
    private var closed = false

    override fun fill(destination: Memory, offset: Int, length: Int): Int {
        if (file == INVALID_HANDLE_VALUE) return 0

        memScoped {
            val n = alloc<DWORDVar>()
            if (ReadFile(file, destination.pointer + offset, length.convert(), n.ptr, null) == FALSE) {
                throw PosixException.forErrno(posixFunctionName = "ReadFile()").wrapIO()
            }

            return n.value.convert<UInt>().toInt()
        }
    }

    override fun closeSource() {
        if (closed) return
        closed = true

        if (file != INVALID_HANDLE_VALUE) {
            if (CloseHandle(file) == FALSE) {
                throw PosixException.forErrno(posixFunctionName = "CloseHandle()").wrapIO()
            }
        }
    }
}

@Suppress("DEPRECATION")
internal class WindowsFileOutput(private val file: HANDLE) : Output() {
    private var closed = false

    override fun flush(source: Memory, offset: Int, length: Int) {
        val end = offset + length
        var currentOffset = offset

        memScoped {
            val written = alloc<UIntVar>()
            while (currentOffset < end) {
                val result = WriteFile(
                    file,
                    source.pointer + currentOffset.convert(),
                    (end - currentOffset).convert(),
                    written.ptr,
                    null
                ).convert<Int>()
                if (result == FALSE) {
                    throw PosixException.forErrno(posixFunctionName = "WriteFile()").wrapIO()
                }
                currentOffset += written.value.toInt()
            }

        }
    }

    override fun closeDestination() {
        if (closed) return
        closed = true

        if (CloseHandle(file) == FALSE) {
            throw PosixException.forErrno(posixFunctionName = "CloseHandle()").wrapIO()
        }
    }
}
