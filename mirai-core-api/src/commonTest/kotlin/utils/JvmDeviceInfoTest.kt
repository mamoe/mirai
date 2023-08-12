/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.json.Json
import net.mamoe.mirai.utils.DeviceInfo.Companion.loadAsDeviceInfo
import net.mamoe.mirai.utils.DeviceInfoManager.Version.Companion.trans
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JvmDeviceInfoTest {

    @TempDir
    lateinit var dir: File

    @Test
    fun `can write and read`() {
        val device = DeviceInfo.random()
        val file = dir.resolve("device.json")

        file.writeText(DeviceInfoManager.serialize(device))
        assertEquals(device, file.loadAsDeviceInfo())
    }

    @Test
    fun `can write read legacy v1`() {
        val device = DeviceInfo.random()
        val file = dir.resolve("device.json")

        val encoded = Json.encodeToString(
            DeviceInfoManager.V1.serializer(), DeviceInfoManager.V1(
                display = device.display,
                product = device.product,
                device = device.device,
                board = device.board,
                brand = device.brand,
                model = device.model,
                bootloader = device.bootloader,
                fingerprint = device.fingerprint,
                bootId = device.bootId,
                procVersion = device.procVersion,
                baseBand = device.baseBand,
                version = device.version,
                simInfo = device.simInfo,
                osType = device.osType,
                macAddress = device.macAddress,
                wifiBSSID = device.wifiBSSID,
                wifiSSID = device.wifiSSID,
                imsiMd5 = device.imsiMd5,
                imei = device.imei,
                apn = device.apn,
            )
        )

        file.writeText(encoded)
        val fileDeviceInfo = file.loadAsDeviceInfo()

        assertTrue { isSameType(device, fileDeviceInfo) }

        assertTrue { device.display.contentEquals(fileDeviceInfo.display) }
        assertTrue { device.product.contentEquals(fileDeviceInfo.product) }
        assertTrue { device.device.contentEquals(fileDeviceInfo.device) }
        assertTrue { device.board.contentEquals(fileDeviceInfo.board) }
        assertTrue { device.brand.contentEquals(fileDeviceInfo.brand) }
        assertTrue { device.model.contentEquals(fileDeviceInfo.model) }
        assertTrue { device.bootloader.contentEquals(fileDeviceInfo.bootloader) }
        assertTrue { device.fingerprint.contentEquals(fileDeviceInfo.fingerprint) }
        assertTrue { device.bootId.contentEquals(fileDeviceInfo.bootId) }
        assertTrue { device.procVersion.contentEquals(fileDeviceInfo.procVersion) }
        assertTrue { device.baseBand.contentEquals(fileDeviceInfo.baseBand) }
        assertEquals(device.version, fileDeviceInfo.version)
        assertTrue { device.simInfo.contentEquals(fileDeviceInfo.simInfo) }
        assertTrue { device.osType.contentEquals(fileDeviceInfo.osType) }
        assertTrue { device.macAddress.contentEquals(fileDeviceInfo.macAddress) }
        assertTrue { device.wifiBSSID.contentEquals(fileDeviceInfo.wifiBSSID) }
        assertTrue { device.wifiSSID.contentEquals(fileDeviceInfo.wifiSSID) }
        assertTrue { device.imsiMd5.contentEquals(fileDeviceInfo.imsiMd5) }
        assertEquals(device.imei, fileDeviceInfo.imei)
        assertTrue { device.apn.contentEquals(fileDeviceInfo.apn) }
        assertTrue { device.androidId.size == fileDeviceInfo.androidId.size }
    }

    @Test
    fun `can write and read legacy v2`() {
        val device = DeviceInfo.random()
        val file = dir.resolve("device.json")

        val encoded = Json.encodeToString(
            DeviceInfoManager.Wrapper.serializer(DeviceInfoManager.V2.serializer()),
            DeviceInfoManager.Wrapper(
                2, DeviceInfoManager.V2(
                    display = device.display.decodeToString(),
                    product = device.product.decodeToString(),
                    device = device.device.decodeToString(),
                    board = device.board.decodeToString(),
                    brand = device.brand.decodeToString(),
                    model = device.model.decodeToString(),
                    bootloader = device.bootloader.decodeToString(),
                    fingerprint = device.fingerprint.decodeToString(),
                    bootId = device.bootId.decodeToString(),
                    procVersion = device.procVersion.decodeToString(),
                    baseBand = DeviceInfoManager.HexString(device.baseBand),
                    version = device.version.trans(),
                    simInfo = device.simInfo.decodeToString(),
                    osType = device.osType.decodeToString(),
                    macAddress = device.macAddress.decodeToString(),
                    wifiBSSID = device.wifiBSSID.decodeToString(),
                    wifiSSID = device.wifiSSID.decodeToString(),
                    imsiMd5 = DeviceInfoManager.HexString(device.imsiMd5),
                    imei = device.imei,
                    apn = device.apn.decodeToString(),
                )
            )
        )

        file.writeText(encoded)
        val fileDeviceInfo = file.loadAsDeviceInfo()

        assertTrue { isSameType(device, fileDeviceInfo) }

        assertTrue { device.display.contentEquals(fileDeviceInfo.display) }
        assertTrue { device.product.contentEquals(fileDeviceInfo.product) }
        assertTrue { device.device.contentEquals(fileDeviceInfo.device) }
        assertTrue { device.board.contentEquals(fileDeviceInfo.board) }
        assertTrue { device.brand.contentEquals(fileDeviceInfo.brand) }
        assertTrue { device.model.contentEquals(fileDeviceInfo.model) }
        assertTrue { device.bootloader.contentEquals(fileDeviceInfo.bootloader) }
        assertTrue { device.fingerprint.contentEquals(fileDeviceInfo.fingerprint) }
        assertTrue { device.bootId.contentEquals(fileDeviceInfo.bootId) }
        assertTrue { device.procVersion.contentEquals(fileDeviceInfo.procVersion) }
        assertTrue { device.baseBand.contentEquals(fileDeviceInfo.baseBand) }
        assertEquals(device.version, fileDeviceInfo.version)
        assertTrue { device.simInfo.contentEquals(fileDeviceInfo.simInfo) }
        assertTrue { device.osType.contentEquals(fileDeviceInfo.osType) }
        assertTrue { device.macAddress.contentEquals(fileDeviceInfo.macAddress) }
        assertTrue { device.wifiBSSID.contentEquals(fileDeviceInfo.wifiBSSID) }
        assertTrue { device.wifiSSID.contentEquals(fileDeviceInfo.wifiSSID) }
        assertTrue { device.imsiMd5.contentEquals(fileDeviceInfo.imsiMd5) }
        assertEquals(device.imei, fileDeviceInfo.imei)
        assertTrue { device.apn.contentEquals(fileDeviceInfo.apn) }
        assertTrue { device.androidId.size == fileDeviceInfo.androidId.size }
    }

    @Test
    fun `can write and read v3`() {
        val device = DeviceInfo.random()
        val file = dir.resolve("device.json")

        val encoded = Json.encodeToString(
            DeviceInfoManager.Wrapper.serializer(DeviceInfoManager.V3.serializer()),
            DeviceInfoManager.Wrapper(
                3, DeviceInfoManager.V3(
                    display = device.display.decodeToString(),
                    product = device.product.decodeToString(),
                    device = device.device.decodeToString(),
                    board = device.board.decodeToString(),
                    brand = device.brand.decodeToString(),
                    model = device.model.decodeToString(),
                    bootloader = device.bootloader.decodeToString(),
                    fingerprint = device.fingerprint.decodeToString(),
                    bootId = device.bootId.decodeToString(),
                    procVersion = device.procVersion.decodeToString(),
                    baseBand = DeviceInfoManager.HexString(device.baseBand),
                    version = device.version.trans(),
                    simInfo = device.simInfo.decodeToString(),
                    osType = device.osType.decodeToString(),
                    macAddress = device.macAddress.decodeToString(),
                    wifiBSSID = device.wifiBSSID.decodeToString(),
                    wifiSSID = device.wifiSSID.decodeToString(),
                    imsiMd5 = DeviceInfoManager.HexString(device.imsiMd5),
                    imei = device.imei,
                    apn = device.apn.decodeToString(),
                    androidId = device.androidId.decodeToString()
                )
            )
        )

        file.writeText(encoded)
        val fileDeviceInfo = file.loadAsDeviceInfo()

        assertEquals(device, fileDeviceInfo)
    }
}