/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.json.Json
import net.mamoe.mirai.utils.DeviceInfo.Companion.loadAsDeviceInfo
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class JvmDeviceInfoTest {

    @TempDir
    lateinit var dir: File

    @Test
    fun `can write and read v2`() {
        val device = DeviceInfo.random()
        val file = dir.resolve("device.json")

        file.writeText(DeviceInfoManager.serialize(device))
        assertEquals(device, file.loadAsDeviceInfo())
    }

    @Test
    fun `can read legacy v1`() {
        val device = DeviceInfo.random()
        val file = dir.resolve("device.json")

        file.writeText(Json.encodeToString(DeviceInfo.serializer(), device))
        assertEquals(device, file.loadAsDeviceInfo())
    }


    // TODO: 2022/10/19 move this to common test when Kotlin supports loading resources in commonMain
    @Test
    fun `can deserialize legacy versions before 2_9_0`() {
        DeviceInfoManager.deserialize(
            this::class.java.classLoader.getResourceAsStream("device/legacy-device-info-1.json")!!
                .use { it.readBytes().decodeToString() })
    }

}