/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.errors.*
import kotlin.test.*

internal abstract class AbstractNativeMiraiFileImplTest {
    protected abstract val baseTempDir: MiraiFile // MiraiFile.create("/Users/Shared/mirai_test")
    protected abstract val tempPath: String
    private val tempDir by lazy {
        MiraiFile.create(tempPath).apply {
            assertTrue("Failed to make temp directory: ${this.absolutePath}") { mkdirs() }
        }
    }

    @AfterTest
    fun afterTest() {
        println("Cleaning up...")
        baseTempDir.deleteRecursively()
    }

    @BeforeTest
    fun init() {
        println("Test start")
        assertTrue { tempDir.exists() }
    }

    @Test
    fun `canonical paths for canonical input`() {
        assertEquals(tempPath, tempDir.path)
        assertEquals(tempPath, tempDir.absolutePath)
    }

    @Test
    protected open fun parent() {
        assertEquals(tempDir, tempDir.resolve("s").parent)
        assertEquals(tempDir.parent, tempDir.resolve(".."))
    }

    @Test
    fun `canonical paths for non-canonical input`() {
        // extra /
        MiraiFile.create("$tempPath/").resolve("test").let {
            assertEquals("${tempPath}/test", it.path)
            assertEquals("${tempPath}/test", it.absolutePath)
        }
        // extra //
        MiraiFile.create("$tempPath//").resolve("test").let {
            assertEquals("${tempPath}/test", it.path)
            assertEquals("${tempPath}/test", it.absolutePath)
        }
        // extra /.
        MiraiFile.create("$tempPath/.").resolve("test").let {
            assertEquals("${tempPath}/test", it.path)
            assertEquals("${tempPath}/test", it.absolutePath)
        }
        // extra /./.
        MiraiFile.create("$tempPath/./.").resolve("test").let {
            assertEquals("${tempPath}/test", it.path)
            assertEquals("${tempPath}/test", it.absolutePath)
        }
        // extra /sss/..
        MiraiFile.create("$tempPath/sss/..").resolve("test").let {
            assertEquals("${tempPath}/sss/../test", it.path) // because file is not found
            assertEquals("${tempPath}/sss/../test", it.absolutePath)
        }
    }

    @Test
    abstract fun `resolve absolute`()

    @Test
    fun `exits createNewFile mkdir length`() {
        assertTrue { tempDir.exists() }

        assertFalse { tempDir.resolve("not_existing_file.txt").exists() }
        assertEquals(0L, tempDir.resolve("not_existing_file.txt").length)
        assertTrue { tempDir.resolve("not_existing_file.txt").createNewFile() }
        assertEquals(0L, tempDir.resolve("not_existing_file.txt").length)
        assertTrue { tempDir.resolve("not_existing_file.txt").exists() }

        assertFalse { tempDir.resolve("not_existing_dir").exists() }
        assertEquals(0L, tempDir.resolve("not_existing_dir").length)
        assertTrue { tempDir.resolve("not_existing_dir").mkdir() }
        assertNotEquals(0L, tempDir.resolve("not_existing_dir").length)
        assertTrue { tempDir.resolve("not_existing_dir").exists() }
    }

    @Test
    fun `isFile isDirectory`() {
        assertTrue { tempDir.exists() }

        assertFalse { tempDir.resolve("not_existing_file.txt").exists() }
        assertEquals(false, tempDir.resolve("not_existing_file.txt").isFile)
        assertEquals(false, tempDir.resolve("not_existing_file.txt").isDirectory)
        assertTrue { tempDir.resolve("not_existing_file.txt").createNewFile() }
        assertEquals(true, tempDir.resolve("not_existing_file.txt").isFile)
        assertEquals(false, tempDir.resolve("not_existing_file.txt").isDirectory)
        assertTrue { tempDir.resolve("not_existing_file.txt").exists() }

        assertFalse { tempDir.resolve("not_existing_dir").exists() }
        assertEquals(false, tempDir.resolve("not_existing_dir").isFile)
        assertEquals(false, tempDir.resolve("not_existing_dir").isDirectory)
        assertTrue { tempDir.resolve("not_existing_dir").mkdir() }
        assertEquals(false, tempDir.resolve("not_existing_dir").isFile)
        assertEquals(true, tempDir.resolve("not_existing_dir").isDirectory)
        assertTrue { tempDir.resolve("not_existing_dir").exists() }
    }

    @Test
    fun writeText() {
        // new file
        tempDir.resolve("writeText1.txt").let { file ->
            val text = "some text"
            file.writeText(text)
            assertEquals(text.length, file.length.toInt())
        }

        // override
        tempDir.resolve("writeText1.txt").let { file ->
            val text = "some other text"
            file.writeText(text)
            assertEquals(text.length, file.length.toInt())
        }
    }

    @Test
    fun readText() {
        tempDir.resolve("readText1.txt").let { file ->
            assertTrue { !file.exists() }
            assertFailsWith<IOException> { file.readText() }

            val text = "some text"
            file.writeText(text)
            assertEquals(text, file.readText())
        }
    }
}