/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.io.autoHexToBytes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class ImageTest {

    @Test
    fun testHexDigitToByte() {
        assertEquals(0xf, 'f'.hexDigitToByte())
        assertEquals(0x1, '1'.hexDigitToByte())
        assertEquals(0x0, '0'.hexDigitToByte())
        assertFailsWith<IllegalArgumentException> {
            'g'.hexDigitToByte()
        }
    }

    @Test
    fun testCalculateImageMd5ByImageId() {
        assertEquals(
            "01E9451B-70ED-EAE3-B37C-101F1EEBF5B5".filterNot { it == '-' }.autoHexToBytes().contentToString(),
            calculateImageMd5ByImageId("{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png").contentToString()
        )

        assertEquals(
            "f8f1ab55-bf8e-4236-b55e-955848d7069f".filterNot { it == '-' }.autoHexToBytes().contentToString(),
            calculateImageMd5ByImageId("/f8f1ab55-bf8e-4236-b55e-955848d7069f").contentToString()
        )

        assertEquals(
            "BFB7027B9354B8F899A062061D74E206".filterNot { it == '-' }.autoHexToBytes().contentToString(),
            calculateImageMd5ByImageId("/000000000-3814297509-BFB7027B9354B8F899A062061D74E206").contentToString()
        )

    }

    //  `/f8f1ab55-bf8e-4236-b55e-955848d7069f` 或 `/000000000-3814297509-BFB7027B9354B8F899A062061D74E206`
}