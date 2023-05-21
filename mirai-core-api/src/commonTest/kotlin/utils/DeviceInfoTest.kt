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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonDeviceInfoTest {
    @Test
    fun `DeviceInfo_random with custom Random is stable`() {
        val time = currentTimeMillis()
        assertEquals(DeviceInfo.random(Random(time)), DeviceInfo.random(Random(time)))
    }


    class HexStringTest {
        @Test
        fun `can serialize as String`() {
            val hexString = DeviceInfoManager.HexString(byteArrayOf(1, 2))
            val string = Json.encodeToString(DeviceInfoManager.HexString.serializer(), hexString)
            assertEquals("\"0102\"", string)
        }

        @Test
        fun `can deserialize from String`() {
            val hex = Json.decodeFromString(DeviceInfoManager.HexString.serializer(), "\"0102\"")
            assertContentEquals(byteArrayOf(1, 2), hex.data)
        }
    }

    @Test
    fun `can serialize and deserialize v3`() {
        val device = DeviceInfo.random()
        assertEquals(device, DeviceInfoManager.deserialize(DeviceInfoManager.serialize(device)))
    }

    @Test
    fun `current version pretty print preview`() {
        val device = DeviceInfo.random()

        val text = DeviceInfoManager.serialize(device, Json {
            prettyPrint = true
        })
        println(text)
        /*
{
    "deviceInfoVersion": 2,
    "data": {
        "display": "MIRAI.868912.001",
        "product": "mirai",
        "device": "mirai",
        "board": "mirai",
        "brand": "mamoe",
        "model": "mirai",
        "bootloader": "unknown",
        "fingerprint": "mamoe/mirai/mirai:10/MIRAI.200122.001/6174518:user/release-keys",
        "bootId": "500E9D6F-1A76-4ED0-20F3-66A5B20C7049",
        "procVersion": "Linux version 3.0.31-r35YRB94 (android-build@xxx.xxx.xxx.xxx.com)",
        "baseBand": "",
        "version": {
            "incremental": "5891938",
            "release": "10",
            "codename": "REL"
        },
        "simInfo": "T-Mobile",
        "osType": "android",
        "macAddress": "02:00:00:00:00:00",
        "wifiBSSID": "02:00:00:00:00:00",
        "wifiSSID": "<unknown ssid>",
        "imsiMd5": "d1ead821747a3ad3f8f3784fafa3b954",
        "imei": "155970036849035",
        "apn": "wifi"
    }
}
         */

        val element = DeviceInfoManager.toJsonElement(device)
        assertEquals(3, element.jsonObject["deviceInfoVersion"]!!.jsonPrimitive.content.toInt())

        val imsiMd5 = element.jsonObject["data"]!!.jsonObject["imsiMd5"]!!.jsonPrimitive.content
        assertEquals(
            device.imsiMd5.toUHexString("").lowercase(),
            imsiMd5
        )
        assertTrue { imsiMd5 matches Regex("""[a-z0-9]+""") }
    }
}