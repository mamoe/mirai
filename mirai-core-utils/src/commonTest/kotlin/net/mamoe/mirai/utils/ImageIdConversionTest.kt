/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.Test
import kotlin.test.assertEquals

internal class ImageIdConversionTest {
    @Test
    fun testConversions() {
        assertEquals(
            "{f8f1ab55-bf8e-4236-b55e-955848d7069f}.mirai",
            generateImageIdFromResourceId("/f8f1ab55-bf8e-4236-b55e-955848d7069f"),
        )
        assertEquals(
            "{EFF4427C-E3D2-7DB6-B1D9-A8AB72E7A29C}.mirai",
            generateImageIdFromResourceId("/000000000-3666252994-EFF4427CE3D27DB6B1D9A8AB72E7A29C"),
        )
        assertEquals(
            "{EF42A82D-8DB6-5D0F-4F11-68961D8DA5CB}.png",
            generateImageIdFromResourceId("{EF42A82D-8DB6-5D0F-4F11-68961D8DA5CB}.png"),
        )
    }
}