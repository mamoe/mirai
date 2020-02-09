/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.utils.io.getRandomByteArray
import net.mamoe.mirai.utils.io.getRandomString

@UseExperimental(ExperimentalUnsignedTypes::class)
actual open class SystemDeviceInfo actual constructor(context: Context) : DeviceInfo(context) {
    override val display: ByteArray get() = "MIRAI.200122.001".toByteArray()
    override val product: ByteArray get() = "mirai".toByteArray()
    override val device: ByteArray get() = "mirai".toByteArray()
    override val board: ByteArray get() = "mirai".toByteArray()
    override val brand: ByteArray get() = "mamoe".toByteArray()
    override val model: ByteArray get() = "mirai".toByteArray()
    override val bootloader: ByteArray get() = "unknown".toByteArray()
    override val fingerprint: ByteArray get() = "mamoe/mirai/mirai:10/MIRAI.200122.001/5891938:user/release-keys".toByteArray()
    override val bootId: ByteArray = ExternalImage.generateUUID(md5(getRandomByteArray(16))).toByteArray()
    override val procVersion: ByteArray get() = "Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com)".toByteArray()
    override val baseBand: ByteArray get() = byteArrayOf()
    override val version: DeviceInfo.Version get() = Version
    override val simInfo: ByteArray get() = "T-Mobile".toByteArray()
    override val osType: ByteArray get() = "android".toByteArray()
    override val macAddress: ByteArray get() = "02:00:00:00:00:00".toByteArray()
    override val wifiBSSID: ByteArray? get() = "02:00:00:00:00:00".toByteArray()
    override val wifiSSID: ByteArray? get() = "<unknown ssid>".toByteArray()
    override val imsiMd5: ByteArray get() = md5(getRandomByteArray(16))
    override val imei: String get() = getRandomString(15, '0'..'9')
    override val ipAddress: ByteArray get() = localIpAddress().split(".").map { it.toUByte().toByte() }.takeIf { it.size == 4 }?.toByteArray() ?: byteArrayOf()
    override val androidId: ByteArray get() = display
    override val apn: ByteArray get() = "wifi".toByteArray()

    object Version : DeviceInfo.Version {
        override val incremental: ByteArray get() = "5891938".toByteArray()
        override val release: ByteArray get() = "10".toByteArray()
        override val codename: ByteArray get() = "REL".toByteArray()
        override val sdk: Int get() = 29
    }
}