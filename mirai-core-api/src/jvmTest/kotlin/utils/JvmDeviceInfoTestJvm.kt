/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlin.test.Test

class JvmDeviceInfoTestJvm {
    @Test
    fun `can deserialize legacy versions before 2_9_0`() {
        // resources not available on android

        DeviceInfoManager.deserialize(
            this::class.java.classLoader.getResourceAsStream("device/legacy-device-info-1.json")!!
                .use { it.readBytes().decodeToString() })
    }
}