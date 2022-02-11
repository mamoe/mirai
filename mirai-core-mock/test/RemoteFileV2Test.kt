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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.mock.internal.remotefile.v2.MockRemoteFiles
import net.mamoe.mirai.mock.internal.txfs.TxFileDiskImpl
import net.mamoe.mirai.mock.internal.txfs.TxFileSystemImpl
import net.mamoe.mirai.mock.utils.simpleMemberInfo
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
        files.root.createFolder("test2")
        val ff = f.uploadNewFile("test.txt", "cc".toByteArray().toExternalResource().toAutoCloseable())
        f.uploadNewFile("test.txt", "cac".toByteArray().toExternalResource().toAutoCloseable())
        println(files.root.folders().toList())
        println(files.root.resolveFolder("test1")!!.files().toList())
        assertEquals(files.root.folders().toList().size, 2)
        assertEquals(files.root.resolveFolder("test1")!!.files().toList().size, 2)
        assertEquals(files.root.resolveFolder("test1")!!.files().toList()[0].name, "test.txt")
        assertEquals(files.root.resolveFolderById(f.id)!!.name, "test1")
        assertEquals(files.root.resolveFileById(ff.id, true)!!.name, "test.txt")
    }

    @Test
    internal fun testDeleteAndMoveTo() = runTest {
        val f = files.root.createFolder("test")
        val ff = f.uploadNewFile("test.txt", "ccc".toByteArray().toExternalResource())
        val fff = files.root.resolveFileById(ff.id, true)!!
        assertEquals(fff, ff)
        f.renameTo("test2")
        assertEquals(files.root.folders().first().name, "test2")
        fff.refresh()
        assertEquals(fff.absolutePath, f.absolutePath + "/" + fff.name)
        fff.moveTo(files.root)
        assertEquals(fff.absolutePath, "/${fff.name}")
        assertEquals(fff.parent, files.root)
        fff.delete()
        assertEquals(fff.exists(), false)
        assertEquals(files.root.resolveFileById(fff.id), null)
    }

    @Test
    fun testSendAndDownload() = runTest {
        val f = files.root.uploadNewFile("test.txt", "c".toByteArray().toExternalResource())
        println(files.fileSystem.findByPath("/test.txt").first().path)
        bot.addGroup(111, "a")
            .addMember0(simpleMemberInfo(222, "bb", permission = MemberPermission.MEMBER)) says f.toMessage()
    }
}