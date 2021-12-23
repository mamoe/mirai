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
import net.mamoe.mirai.mock.resserver.MockServerFileDisk
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TxFsDiskTest {
    val tmpfs = Jimfs.newFileSystem(Configuration.unix())
    val disk = MockServerFileDisk.newFileDisk(tmpfs.getPath("/disk"))
    private fun splitLine() = println("==================================================================")

    @AfterEach
    fun release() {
        println("===================[ FILE SYSTEM STRUCT DUMP ]========================")
        Files.walk(tmpfs.getPath("/")).use { s ->
            s.forEach { pt ->
                println(pt)
            }
        }
        println("===================[                         ]========================")
        tmpfs.close()
    }

    @Test
    fun testDisk() {
        val system = disk.newFsSystem()
        val root = system.root
        println(root)
        println(root.fileInfo)

        splitLine()

        kotlin.run {
            val subdir = root.mksubdir("a-dir", 0)
            println(subdir)
            println(subdir.fileInfo)
            assertEquals("/a-dir", subdir.path)

            assertFails { root.moveTo(subdir) }

            val children = root.listFiles()!!.onEach { println(it) }.toList()
            assertEquals(1, children.size)
            assertEquals(subdir, children[0])
            assertEquals(root, subdir.parent)

            subdir.delete()
            println(subdir)
            assertFalse { subdir.exists }
            assertFalse { subdir.isFile }
            assertFalse { subdir.isDirectory }
            assertTrue { subdir.toString().startsWith("<not exists>") }
            assertFails { subdir.fileInfo }
        }

        splitLine()

        kotlin.run {
            val newFile = root.uploadFile(
                "test.txt",
                """A""".toByteArray().toExternalResource().toAutoCloseable(),
                5
            )
            val newFileInfo = newFile.fileInfo
            assertEquals(5, newFileInfo.creator)
            assertEquals(root, newFile.parent)
            assertEquals("test.txt", newFile.name)
            assertEquals("/test.txt", newFile.path)

            newFile.rename("hello world.bin")
            assertEquals("hello world.bin", newFile.name)


            val children = root.listFiles()!!.onEach { println(it) }.toList()
            assertEquals(1, children.size)
            assertEquals(children[0], newFile)

            val subdir = root.mksubdir("1", 3)
            newFile.moveTo(subdir)
            assertEquals("/1/hello world.bin", newFile.path)

            assertEquals(subdir, newFile.parent)

            val children1 = subdir.listFiles()!!.toList()
            assertEquals(1, children1.size)
            assertEquals(newFile, children1[0])

            val children2 = root.listFiles()!!.toList()
            assertEquals(1, children2.size)
            assertEquals(subdir, children2[0])


            assertEquals(newFile, system.findByPath("/1/hello world.bin").firstOrNull())

            println("TEST SUB DIR: $subdir")

            // TODO: Download content
        }

    }

}