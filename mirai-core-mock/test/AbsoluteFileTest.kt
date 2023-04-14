/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.mock.internal.contact.mockUploadAudio
import net.mamoe.mirai.mock.internal.remotefile.absolutefile.MockRemoteFiles
import net.mamoe.mirai.mock.internal.serverfs.MockServerFileSystemImpl
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.cast
import net.mamoe.mirai.utils.md5
import net.mamoe.mirai.utils.runBIO
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.FileSystem
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class AbsoluteFileTest : MockBotTestBase() {
    private val tmpfs: FileSystem = Jimfs.newFileSystem(Configuration.unix())
    private val disk = bot.tmpResourceServer.mockServerFileDisk
    private val group = bot.addGroup(11L, "a").also { println(it.owner) }
    private val fsys = MockServerFileSystemImpl(disk.cast())
    private val files = MockRemoteFiles(group, fsys)

    @AfterEach
    internal fun release() {
        tmpfs.close()
    }

    @Test
    internal fun listFileAndFolder() = runTest {
        val folder = files.root.createFolder("test1")
        files.root.createFolder("test2")
        val file = folder.uploadNewFile("test.txt", "cc".toByteArray().toExternalResource().toAutoCloseable())
        folder.uploadNewFile("test.txt", "cac".toByteArray().toExternalResource().toAutoCloseable())
        println(files.root.folders().toList())
        println(files.root.resolveFolder("test1")!!.files().toList())
        assertEquals(2, files.root.folders().toList().size)
        assertEquals(2, files.root.resolveFolder("test1")!!.files().toList().size)
        assertEquals("test.txt", files.root.resolveFolder("test1")!!.files().toList()[0].name)
        assertEquals("test1", files.root.resolveFolderById(folder.id)!!.name)
        assertEquals("test.txt", files.root.resolveFileById(file.id, true)!!.name)
    }

    @Test
    internal fun testDeleteAndMoveTo() = runTest {
        val f = files.root.createFolder("test")
        val ff = f.uploadNewFile("test.txt", "ccc".toByteArray().toExternalResource())
        val fff = files.root.resolveFileById(ff.id, true)!!
        assertEquals(fff, ff)
        f.renameTo("test2")
        assertEquals("test2", files.root.folders().first().name)
        fff.refresh()
        assertEquals(f.absolutePath + "/" + fff.name, fff.absolutePath)
        fff.moveTo(files.root)
        assertEquals("/${fff.name}", fff.absolutePath)
        assertEquals(files.root, fff.parent)
        fff.delete()
        assertEquals(false, fff.exists())
        assertEquals(null, files.root.resolveFileById(fff.id))
    }

    @Test
    internal fun testSendAndDownload() = runTest {
        val f = files.root.uploadNewFile("test.txt", "c".toByteArray().toExternalResource())
        println(files.fileSystem.findByPath("/test.txt").first().path)
        runAndReceiveEventBroadcast {
            group.addMember(simpleMemberInfo(222, "bb", permission = MemberPermission.MEMBER))
                .saysMessage { f.toMessage() }
        }.let { events ->
            assertEquals(1, events.size)
            assertEquals(true, events[0].cast<GroupMessageEvent>().message.contains(FileMessage))
        }
        assertEquals("c", f.getUrl()!!.toUrl().readText())
    }

    @Test
    fun testRename() = runTest {
        val folder = files.root.createFolder("test1")
        val file = folder.uploadNewFile("test.txt", "content".toByteArray().toExternalResource().toAutoCloseable())
        assertEquals(file.id, folder.resolveFiles("test.txt").first().id)
        folder.renameTo("test2")
        file.refresh()
        assertEquals(true, file.exists())
        assertNotEquals(null, folder.resolveFiles("test.txt").firstOrNull())
    }

    @Test
    fun testMD5() = runTest {
        val bytes = "test".toByteArray()
        val file = bytes.toExternalResource().use { res ->
            files.root.uploadNewFile("/test.txt", res)
        }
        assertContentEquals(bytes.md5(), file.md5)
    }

    @Test
    fun testMD5WithResolve() = runTest {
        val bytes = "test".toByteArray()
        bytes.toExternalResource().use { res ->
            files.root.uploadNewFile("/test.txt", res)
        }
        val file = files.root.resolveFiles("/test.txt").toList()
        assertEquals(1, file.size)
        assertContentEquals(bytes.md5(), file[0].md5)
    }

    @Test
    fun testMD5WithIDResolve() = runTest {
        val bytes = "test".toByteArray()
        val absFile = bytes.toExternalResource().use { res ->
            files.root.uploadNewFile("/test.txt", res)
        }
        val file = files.root.resolveFileById(absFile.id, true)!!
        assertContentEquals(bytes.md5(), file.md5)
    }

    @Test
    fun testResolveFiles() = runTest {
        val file = runBIO {
            kotlin.io.path.createTempFile("test", ".txt").toFile().apply {
                writeText("test")
                deleteOnExit()
            }
        }
        file.toExternalResource().use {
            group.files.root.uploadNewFile("/a/test.txt", it)
        }
        assertEquals(0, group.files.root.resolveFiles("/a").count())
    }

    @Test
    @Suppress("INVISIBLE_REFERENCE")
    fun testMockUploadAudio() = runTest {
        val file = runBIO {
            kotlin.io.path.createTempFile("test", ".txt").toFile().apply {
                writeText("test")
                deleteOnExit()
            }
        }

        file.toExternalResource().use {
            assertIsInstance<net.mamoe.mirai.internal.utils.ExternalResourceImplByFile>(it)
            it.mockUploadAudio(bot)
        }
    }
}