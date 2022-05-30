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
    protected val tempDir by lazy {
        MiraiFile.create(tempPath).apply {
            assertTrue("Failed to make temp directory: ${this.absolutePath}") { mkdirs() }
        }
    }

    @AfterTest
    fun afterTest() {
        println("Cleaning up...")
        println("deleteRecursively:" + baseTempDir.deleteRecursively())
    }

    @BeforeTest
    fun init() {
        println("Test start")
        assertTrue { tempDir.exists() }
    }

    @Test
    fun `canonical paths for canonical input`() {
        assertPathEquals(tempPath, tempDir.path)
        assertPathEquals(tempPath, tempDir.absolutePath)
    }

    @Test
    protected open fun parent() {
        assertEquals(tempDir, tempDir.resolve("s").parent)
        assertEquals(tempDir.parent, tempDir.resolve(".."))
    }

    @Test
    open fun `canonical paths for non-canonical input`() {
        // extra /
        MiraiFile.create("$tempPath/").resolve("test").let {
            assertPathEquals("${tempPath}/test", it.path)
            assertPathEquals("${tempPath}/test", it.absolutePath)
        }
        // extra //
        MiraiFile.create("$tempPath//").resolve("test").let {
            assertPathEquals("${tempPath}/test", it.path)
            assertPathEquals("${tempPath}/test", it.absolutePath)
        }
        // extra /.
        MiraiFile.create("$tempPath/.").resolve("test").let {
            assertPathEquals("${tempPath}/test", it.path)
            assertPathEquals("${tempPath}/test", it.absolutePath)
        }
        // extra /./.
        MiraiFile.create("$tempPath/./.").resolve("test").let {
            assertPathEquals("${tempPath}/test", it.path)
            assertPathEquals("${tempPath}/test", it.absolutePath)
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
//        assertNotEquals(0L, tempDir.resolve("not_existing_dir").length) // length is platform-dependent, on Windows it is 0 but on unix it is not
        assertTrue { tempDir.resolve("not_existing_dir").exists() }
    }

    @Test
    fun `isFile isDirectory`() {
        assertTrue { tempDir.exists() }

        println("1")
        assertFalse { tempDir.resolve("not_existing_file.txt").exists() }
        assertEquals(false, tempDir.resolve("not_existing_file.txt").isFile)
        println("1")
        assertEquals(false, tempDir.resolve("not_existing_file.txt").isDirectory)
        println("1")
        assertTrue { tempDir.resolve("not_existing_file.txt").createNewFile() }
        assertEquals(true, tempDir.resolve("not_existing_file.txt").isFile)
        assertEquals(false, tempDir.resolve("not_existing_file.txt").isDirectory)
        println("1")
        assertTrue { tempDir.resolve("not_existing_file.txt").exists() }

        println("1")
        assertFalse { tempDir.resolve("not_existing_dir").exists() }
        assertEquals(false, tempDir.resolve("not_existing_dir").isFile)
        assertEquals(false, tempDir.resolve("not_existing_dir").isDirectory)
        println("1")
        assertTrue { tempDir.resolve("not_existing_dir").mkdir() }
        assertEquals(false, tempDir.resolve("not_existing_dir").isFile)
        assertEquals(true, tempDir.resolve("not_existing_dir").isDirectory)
        println("1")
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
        tempDir.resolve("readText2.txt").let { file ->
            assertTrue { !file.exists() }
            assertFailsWith<IOException> { file.readText() }

            val text = "some text"
            file.writeText(text)
            assertEquals(text, file.readText())
        }
    }

    private val bigText = "some text".repeat(10000)

    @Test
    fun writeBigText() {
        // new file
        tempDir.resolve("writeText3.txt").let { file ->
            file.writeText(bigText)
            assertEquals(bigText.length, file.length.toInt())
        }

        // override
        tempDir.resolve("writeText4.txt").let { file ->
            file.writeText(bigText)
            assertEquals(bigText.length, file.length.toInt())
        }
    }

    @Test
    fun readBigText() {
        tempDir.resolve("readText4.txt").let { file ->
            assertTrue { !file.exists() }
            assertFailsWith<IOException> { file.readText() }

            file.writeText(bigText)
            println("reading text")
            val read = file.readText()
            assertEquals(bigText.length, read.length)
            assertEquals(bigText, read)
        }
    }

    protected fun assertPathEquals(expected: String, actual: String, message: String? = null) {
        asserter.assertEquals(message, expected.replace("\\", "/"), actual.replace("\\", "/"))
    }
}