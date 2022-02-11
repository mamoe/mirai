/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import kotlinx.coroutines.flow.toList
import net.mamoe.mirai.mock.internal.remotefile.v2.MockRemoteFiles
import net.mamoe.mirai.mock.internal.txfs.TxFileDiskImpl
import net.mamoe.mirai.mock.internal.txfs.TxFileSystemImpl
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import kotlin.test.assertEquals

internal class RemoteFileV2Test : MockBotTestBase() {
    private val tmpfs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
    private val disk = TxFileDiskImpl(tmpfs.getPath("/disk"))
    private val g = bot.addGroup(11L, "a")
    private val fsys = TxFileSystemImpl(disk)
    private val files = MockRemoteFiles(g, fsys)

    @AfterEach
    internal fun release() {
        tmpfs.close()
    }

    @Test
    internal fun listFileAndFolder() = runTest {
        val f = files.root.createFolder("test1")
        val ff = f.uploadNewFile("test.txt", "cc".toByteArray().toExternalResource().toAutoCloseable())
        println(files.root.folders().toList())
        println(files.root.resolveFolder("test1")!!.files().toList())
        assertEquals(files.root.folders().toList().size, 1)
        assertEquals(files.root.folders().toList()[0].name, "test1")
        assertEquals(files.root.resolveFolder("test1")!!.files().toList().size, 1)
        assertEquals(files.root.resolveFolder("test1")!!.files().toList()[0].name, "test.txt")
        assertEquals(files.root.resolveFolderById(f.id)!!.name, "test1")
        assertEquals(files.root.resolveFileById(ff.id, true)!!.name, "test.txt")
    }
}