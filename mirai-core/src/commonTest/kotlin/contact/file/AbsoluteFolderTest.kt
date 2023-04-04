/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.file

import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.network.notice.BotAware
import net.mamoe.mirai.internal.network.protocol.data.proto.GroupFileCommon
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.internal.test.runBlockingUnit
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AbsoluteFolderTest : AbstractTest(), BotAware, GroupExtensions {
    override val bot = MockBot { }
    val group = bot.addGroup(1L, 2L)
    private val root = group.files.root

    @Test
    fun `resolveFolderById always returns null if it is not root`() = runBlockingUnit {
        val child = root.impl().createChildFolder(
            GroupFileCommon.FolderInfo(
                folderId = "/f-1",
                folderName = "name"
            )
        )
        assertEquals(null, child.resolveFolderById("/anything"))
    }

    @Test
    fun `resolveFolderById always returns root for slash`() = runBlockingUnit {
        val child = root.impl().createChildFolder(
            GroupFileCommon.FolderInfo(
                folderId = "/f-1",
                folderName = "name"
            )
        )
        assertEquals(root, root.resolveFolderById("/"))
        assertEquals(root, child.resolveFolderById("/"))
    }
}