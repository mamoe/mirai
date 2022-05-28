/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("RedundantVisibilityModifier")

package net.mamoe.mirai.utils

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import io.ktor.utils.io.streams.*
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.feof
import platform.posix.stat

/**
 * Multiplatform implementation of file operations.
 */
public actual interface MiraiFile {
    public actual val name: String
    public actual val parent: MiraiFile?
    public actual val absolutePath: String
    public actual val length: Long
    public actual val isFile: Boolean
    public actual val isDirectory: Boolean
    public actual fun exists(): Boolean
    public actual fun resolve(path: String): MiraiFile
    public actual fun resolve(file: MiraiFile): MiraiFile
    public actual fun createNewFile(): Boolean
    public actual fun delete(): Boolean
    public actual fun mkdir(): Boolean
    public actual fun mkdirs(): Boolean
    public actual fun input(): Input
    public actual fun output(): Output

    public actual companion object {
        public actual fun create(absolutePath: String): MiraiFile = MiraiFileImpl(absolutePath)
    }
}


internal expect class MiraiFileImpl(
    absolutePath: String,
) : MiraiFile


/*
    Data from https://man7.org/linux/man-pages/man2/lstat.2.html


   st_dev This field describes the device on which this file
          resides.  (The major(3) and minor(3) macros may be useful
          to decompose the device ID in this field.)

   st_ino This field contains the file's inode number.

   st_mode
          This field contains the file type and mode.  See inode(7)
          for further information.

       S_IFMT     0170000   bit mask for the file type bit field

       S_IFSOCK   0140000   socket
       S_IFLNK    0120000   symbolic link
       S_IFREG    0100000   regular file
       S_IFBLK    0060000   block device
       S_IFDIR    0040000   directory
       S_IFCHR    0020000   character device
       S_IFIFO    0010000   FIFO


   st_nlink
          This field contains the number of hard links to the file.

   st_uid This field contains the user ID of the owner of the file.

   st_gid This field contains the ID of the group owner of the file.

   st_rdev
          This field describes the device that this file (inode)
          represents.

   st_size
          This field gives the size of the file (if it is a regular
          file or a symbolic link) in bytes.  The size of a symbolic
          link is the length of the pathname it contains, without a
          terminating null byte.

   st_blksize
          This field gives the "preferred" block size for efficient
          filesystem I/O.

   st_blocks
          This field indicates the number of blocks allocated to the
          file, in 512-byte units.  (This may be smaller than
          st_size/512 when the file has holes.)

   st_atime
          This is the time of the last access of file data.

   st_mtime
          This is the time of last modification of file data.

   st_ctime
          This is the file's last status change timestamp (time of
          last change to the inode).

 */
internal inline fun <R> MiraiFileImpl.useStat(block: (stat) -> R): R? {
    memScoped {
        val stat = alloc<stat>()
        val ret = stat(absolutePath, stat.ptr)
        if (ret != 0) return null
        return block(stat)
    }
}

internal class FileNotFoundException(message: String, cause: Throwable? = null) :
    IOException(message, cause)


@OptIn(ExperimentalIoApi::class)
internal class PosixFileInstanceOutput(val file: CPointer<FILE>) : AbstractOutput() {
    private var closed = false

    override fun flush(source: Memory, offset: Int, length: Int) {
        val end = offset + length
        var currentOffset = offset

        while (currentOffset < end) {
            val result = fwrite(source, currentOffset, end - currentOffset, file.cast())
            if (result == 0) {
                throw PosixException.forErrno(posixFunctionName = "fwrite()").wrapIO()
            }
            currentOffset += result
        }
    }

    override fun closeDestination() {
        if (closed) return
        closed = true

        if (fclose(file) != 0) {
            throw PosixException.forErrno(posixFunctionName = "fclose").wrapIO()
        }
    }
}

@OptIn(ExperimentalIoApi::class)
internal class PosixInputForFile(val file: CPointer<FILE>) : AbstractInput() {
    private var closed = false

    override fun fill(destination: Memory, offset: Int, length: Int): Int {
        val size = fread(destination, offset, length, file.cast())
        if (size == 0) {
            if (feof(file) != 0) return 0
            throw PosixException.forErrno(posixFunctionName = "read()").wrapIO()
        }

        return size
    }

    override fun closeSource() {
        if (closed) return
        closed = true

        if (fclose(file) != 0) {
            throw PosixException.forErrno(posixFunctionName = "fclose()").wrapIO()
        }
    }
}

@OptIn(ExperimentalIoApi::class)
internal fun PosixException.wrapIO(): IOException =
    IOException("I/O operation failed due to posix error code $errno", this)
