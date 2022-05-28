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
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import platform.posix.*

internal actual class MiraiFileImpl actual constructor(
    absolutePath: String,
) : MiraiFile {
    companion object {
        private const val SEPARATOR = "/"
    }


    // TODO: 2022/5/28 normalize paths
    override val absolutePath: String = kotlin.run {
        absolutePath
    }

    // TODO: 2022/5/28 normalize paths
    override val parent: MiraiFile? by lazy {
        val p = absolutePath.substringBeforeLast('/')
        if (p.isEmpty()) {
            return@lazy null
        }
        if (p.lastOrNull() == ':') {
            if (p.lastIndexOf('/') == p.lastIndex) {
                // C:/
                return@lazy null
            } else {
                return@lazy MiraiFileImpl("$p/") // C:/
            }
        }
        MiraiFileImpl(p)
    }

    override val name: String
        get() = absolutePath.substringAfterLast('/').ifEmpty { absolutePath }

    init {
        checkName(absolutePath.substringAfterLast('/')) // do not check drive letter
    }

    private fun checkName(name: String) {
        name.substringAfterLast('/').forEach { c ->
            if (c in """\/:?*"><|""") {
                throw IllegalArgumentException("'${name}' contains illegal character '$c'.")
            }
        }

        // TODO: 2022/5/28 check name
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

    // TODO: 2022/5/28 normalize paths
    override fun resolve(path: String): MiraiFile {
        when (path) {
            "." -> return this
            ".." -> return parent ?: this // root
        }

        if (path == "/") {
            return MiraiFileImpl(path)
        }

        val new = MiraiFileImpl(path)
        return MiraiFileImpl("$absolutePath${SEPARATOR}${new.parent}/${new.name}")
    }

    override fun resolve(file: MiraiFile): MiraiFile {
        val parent = file.parent ?: return resolve(file.name)
        return resolve(parent).resolve(file.name)
    }

    @OptIn(UnsafeNumber::class)
    override fun createNewFile(): Boolean {
        memScoped {
            // https://docs.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-createfilea
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
        memScoped {
            @Suppress("UnnecessaryOptInAnnotation") // bug
            @OptIn(UnsafeNumber::class)
            return mkdir(absolutePath, 0).convert<Int>() == 0
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