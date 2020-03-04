/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.utils.io.getRandomByteArray
import net.mamoe.mirai.utils.io.getRandomString
import java.io.File

/**
 * 加载一个设备信息. 若文件不存在或为空则随机并创建一个设备信息保存.
 */
@OptIn(UnstableDefault::class)
fun File.loadAsDeviceInfo(context: Context = ContextImpl()): DeviceInfo {
    if (!this.exists() || this.length() == 0L) {
        return SystemDeviceInfo(context).also {
            this.writeText(Json.plain.stringify(SystemDeviceInfo.serializer(), it))
        }
    }
    return Json.nonstrict.parse(DeviceInfoData.serializer(), this.readText()).also {
        it.context = context
    }
}


@Serializable
@OptIn(ExperimentalUnsignedTypes::class)
actual open class SystemDeviceInfo actual constructor() : DeviceInfo() {
    actual constructor(context: Context) : this() {
        this.context = context
    }

    @Transient
    final override lateinit var context: Context

    override val display: ByteArray = "MIRAI.200122.001".toByteArray()
    override val product: ByteArray = "mirai".toByteArray()
    override val device: ByteArray = "mirai".toByteArray()
    override val board: ByteArray = "mirai".toByteArray()
    override val brand: ByteArray = "mamoe".toByteArray()
    override val model: ByteArray = "mirai".toByteArray()
    override val bootloader: ByteArray = "unknown".toByteArray()
    override val fingerprint: ByteArray = "mamoe/mirai/mirai:10/MIRAI.200122.001/${getRandomString(7, '0'..'9')}:user/release-keys".toByteArray()
    override val bootId: ByteArray = ExternalImage.generateUUID(md5(getRandomByteArray(16))).toByteArray()
    override val procVersion: ByteArray = "Linux version 3.0.31-${getRandomString(8)} (android-build@xxx.xxx.xxx.xxx.com)".toByteArray()
    override val baseBand: ByteArray = byteArrayOf()
    override val version: Version = Version
    override val simInfo: ByteArray = "T-Mobile".toByteArray()
    override val osType: ByteArray = "android".toByteArray()
    override val macAddress: ByteArray = "02:00:00:00:00:00".toByteArray()
    override val wifiBSSID: ByteArray? = "02:00:00:00:00:00".toByteArray()
    override val wifiSSID: ByteArray? = "<unknown ssid>".toByteArray()
    override val imsiMd5: ByteArray = md5(getRandomByteArray(16))
    override val imei: String = getRandomString(15, '0'..'9')
    override val ipAddress: ByteArray get() = localIpAddress().split(".").map { it.toUByte().toByte() }.takeIf { it.size == 4 }?.toByteArray() ?: byteArrayOf()
    override val androidId: ByteArray get() = display
    override val apn: ByteArray = "wifi".toByteArray()

    @Serializable
    actual object Version : DeviceInfo.Version {
        override val incremental: ByteArray = "5891938".toByteArray()
        override val release: ByteArray = "10".toByteArray()
        override val codename: ByteArray = "REL".toByteArray()
        override val sdk: Int = 29
    }
}