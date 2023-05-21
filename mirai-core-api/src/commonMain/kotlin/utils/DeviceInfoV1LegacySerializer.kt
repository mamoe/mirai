/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable


@Serializable
internal class DeviceInfoV1Legacy(
    val product: ByteArray,
    val display: ByteArray,
    val device: ByteArray,
    val board: ByteArray,
    val brand: ByteArray,
    val model: ByteArray,
    val bootloader: ByteArray,
    val fingerprint: ByteArray,
    val bootId: ByteArray,
    val procVersion: ByteArray,
    val baseBand: ByteArray,
    val version: DeviceInfoV1LegacyVersion,
    val simInfo: ByteArray,
    val osType: ByteArray,
    val macAddress: ByteArray,
    val wifiBSSID: ByteArray,
    val wifiSSID: ByteArray,
    val imsiMd5: ByteArray,
    val imei: String,
    val apn: ByteArray,
    val androidId: ByteArray? = null
)

@Serializable
internal class DeviceInfoV1LegacyVersion(
    val incremental: ByteArray = "5891938".toByteArray(),
    val release: ByteArray = "10".toByteArray(),
    val codename: ByteArray = "REL".toByteArray(),
    val sdk: Int = 29
)

internal object DeviceInfoV1LegacySerializer : KSerializer<DeviceInfo> by DeviceInfoV1Legacy.serializer().map(
    DeviceInfoV1Legacy.serializer().descriptor.copy("DeviceInfo"),
    deserialize = {
        @Suppress("DEPRECATION")
        DeviceInfo(
            display,
            product,
            device,
            board,
            brand,
            model,
            bootloader,
            fingerprint,
            bootId,
            procVersion,
            baseBand,
            DeviceInfo.Version(version.incremental, version.release, version.codename, version.sdk),
            simInfo,
            osType,
            macAddress,
            wifiBSSID,
            wifiSSID,
            imsiMd5,
            imei,
            apn,
            androidId = display
        )
    },
    serialize = {
        DeviceInfoV1Legacy(
            display,
            product,
            device,
            board,
            brand,
            model,
            bootloader,
            fingerprint,
            bootId,
            procVersion,
            baseBand,
            DeviceInfoV1LegacyVersion(version.incremental, version.release, version.codename, version.sdk),
            simInfo,
            osType,
            macAddress,
            wifiBSSID,
            wifiSSID,
            imsiMd5,
            imei,
            apn
        )
    }
)