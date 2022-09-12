/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package net.mamoe.mirai.mock.test

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.mock.resserver.TmpResourceServer
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.mkParentDirs
import org.junit.jupiter.api.Test
import kotlin.io.path.writeText
import kotlin.test.assertEquals

@Suppress("RemoveExplicitTypeArguments")
internal class FsServerTest {
    @Test
    fun testFsServer() = runBlocking<Unit> {
        val fsServer = TmpResourceServer.newInMemoryTmpResourceServer()
        fsServer.startupServer()
        val testFile = "Test".toByteArray().toExternalResource()
        val resourceId = fsServer.uploadResource(testFile)
        val response = fsServer.resolveHttpUrl(resourceId).toURL().readText()
        assertEquals("Test", response)

        val pt0 = fsServer.storageRoot.resolve("/rand/etc/randrand/somedata")
        pt0.mkParentDirs()
        pt0.writeText("Test")

        assertEquals("Test", fsServer.resolveHttpUrlByPath(pt0).toURL().readText())

        fsServer.close()
    }
}
