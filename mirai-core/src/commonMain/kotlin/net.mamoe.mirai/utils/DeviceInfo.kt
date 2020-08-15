/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoId

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

    val ipAddress: ByteArray get() = byteArrayOf(192.toByte(), 168.toByte(), 1, 123)

    abstract val androidId: ByteArray

    abstract val apn: ByteArray

    fun generateDeviceInfoData(): ByteArray {
        @Serializable
        class DevInfo(
            @ProtoId(1) val bootloader: ByteArray,
            @ProtoId(2) val procVersion: ByteArray,
            @ProtoId(3) val codename: ByteArray,
            @ProtoId(4) val incremental: ByteArray,
            @ProtoId(5) val fingerprint: ByteArray,
            @ProtoId(6) val bootId: ByteArray,
            @ProtoId(7) val androidId: ByteArray,
            @ProtoId(8) val baseBand: ByteArray,
            @ProtoId(9) val innerVersion: ByteArray
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

@Serializable
class DeviceInfoData(
    override val display: ByteArray,
    override val product: ByteArray,
    override val device: ByteArray,
    override val board: ByteArray,
    override val brand: ByteArray,
    override val model: ByteArray,
    override val bootloader: ByteArray,
    override val fingerprint: ByteArray,
    override val bootId: ByteArray,
    override val procVersion: ByteArray,
    override val baseBand: ByteArray,
    override val version: VersionData,
    override val simInfo: ByteArray,
    override val osType: ByteArray,
    override val macAddress: ByteArray,
    override val wifiBSSID: ByteArray?,
    override val wifiSSID: ByteArray?,
    override val imsiMd5: ByteArray,
    override val imei: String,
    override val apn: ByteArray
) : DeviceInfo() {
    @Transient
    override lateinit var context: Context

    override val androidId: ByteArray get() = display

    @Serializable
    class VersionData(
        override val incremental: ByteArray = SystemDeviceInfo.Version.incremental,
        override val release: ByteArray = SystemDeviceInfo.Version.release,
        override val codename: ByteArray = SystemDeviceInfo.Version.codename,
        override val sdk: Int = SystemDeviceInfo.Version.sdk
    ) : Version
}

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