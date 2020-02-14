/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * 设备信息. 可通过继承 [SystemDeviceInfo] 来在默认的基础上修改
 */
abstract class DeviceInfo {
    @Transient
    abstract val context: Context

    abstract val display: ByteArray
    abstract val product: ByteArray
    abstract val device: ByteArray
    abstract val board: ByteArray

    abstract val brand: ByteArray
    abstract val model: ByteArray
    abstract val bootloader: ByteArray
    abstract val fingerprint: ByteArray
    abstract val bootId: ByteArray

    abstract val procVersion: ByteArray
    abstract val baseBand: ByteArray

    abstract val version: Version

    abstract val simInfo: ByteArray

    abstract val osType: ByteArray

    abstract val macAddress: ByteArray

    abstract val wifiBSSID: ByteArray?
    abstract val wifiSSID: ByteArray?

    abstract val imsiMd5: ByteArray
    abstract val imei: String

    abstract val ipAddress: ByteArray

    abstract val androidId: ByteArray

    abstract val apn: ByteArray

    val guid: ByteArray by lazy { generateGuid(androidId, macAddress) }

    fun generateDeviceInfoData(): ByteArray {
        @Serializable
        class DevInfo(
            @SerialId(1) val bootloader: ByteArray,
            @SerialId(2) val procVersion: ByteArray,
            @SerialId(3) val codename: ByteArray,
            @SerialId(4) val incremental: ByteArray,
            @SerialId(5) val fingerprint: ByteArray,
            @SerialId(6) val bootId: ByteArray,
            @SerialId(7) val androidId: ByteArray,
            @SerialId(8) val baseBand: ByteArray,
            @SerialId(9) val innerVersion: ByteArray
        )

        return ProtoBuf.dump(
            DevInfo.serializer(), DevInfo(
                bootloader,
                procVersion,
                version.codename,
                version.incremental,
                fingerprint,
                bootId,
                androidId,
                baseBand,
                version.incremental
            )
        )
    }

    interface Version {
        val incremental: ByteArray
        val release: ByteArray
        val codename: ByteArray
        val sdk: Int
    }
}

/**
 * Defaults "%4;7t>;28<fc.5*6".toByteArray()
 */
fun generateGuid(androidId: ByteArray, macAddress: ByteArray): ByteArray = md5(androidId + macAddress)

/*
fun DeviceInfo.toOidb0x769DeviceInfo() : Oidb0x769.DeviceInfo = Oidb0x769.DeviceInfo(
    brand = brand.encodeToString(),
    model = model.encodeToString(),
    os = Oidb0x769.OS(
        version = version.release.encodeToString(),
        sdk = version.sdk.toString(),
        kernel = version.kernel
    )
)
*/