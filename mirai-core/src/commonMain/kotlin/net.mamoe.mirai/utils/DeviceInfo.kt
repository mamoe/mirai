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
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * 设备信息. 可通过继承 [SystemDeviceInfo] 来在默认的基础上修改
 */
public abstract class DeviceInfo {
    @Transient
    public abstract val context: Context

    public abstract val display: ByteArray
    public abstract val product: ByteArray
    public abstract val device: ByteArray
    public abstract val board: ByteArray

    public abstract val brand: ByteArray
    public abstract val model: ByteArray
    public abstract val bootloader: ByteArray
    public abstract val fingerprint: ByteArray
    public abstract val bootId: ByteArray

    public abstract val procVersion: ByteArray
    public abstract val baseBand: ByteArray

    public abstract val version: Version

    public abstract val simInfo: ByteArray

    public abstract val osType: ByteArray

    public abstract val macAddress: ByteArray

    public abstract val wifiBSSID: ByteArray?
    public abstract val wifiSSID: ByteArray?

    public abstract val imsiMd5: ByteArray
    public abstract val imei: String

    public val ipAddress: ByteArray get() = byteArrayOf(192.toByte(), 168.toByte(), 1, 123)

    public abstract val androidId: ByteArray

    public abstract val apn: ByteArray

    public fun generateDeviceInfoData(): ByteArray {
        @Serializable
        class DevInfo(
            @ProtoNumber(1) val bootloader: ByteArray,
            @ProtoNumber(2) val procVersion: ByteArray,
            @ProtoNumber(3) val codename: ByteArray,
            @ProtoNumber(4) val incremental: ByteArray,
            @ProtoNumber(5) val fingerprint: ByteArray,
            @ProtoNumber(6) val bootId: ByteArray,
            @ProtoNumber(7) val androidId: ByteArray,
            @ProtoNumber(8) val baseBand: ByteArray,
            @ProtoNumber(9) val innerVersion: ByteArray
        )

        return ProtoBuf.encodeToByteArray(
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

    public interface Version {
        public val incremental: ByteArray
        public val release: ByteArray
        public val codename: ByteArray
        public val sdk: Int
    }
}

@Serializable
public class DeviceInfoData(
    public override val display: ByteArray,
    public override val product: ByteArray,
    public override val device: ByteArray,
    public override val board: ByteArray,
    public override val brand: ByteArray,
    public override val model: ByteArray,
    public override val bootloader: ByteArray,
    public override val fingerprint: ByteArray,
    public override val bootId: ByteArray,
    public override val procVersion: ByteArray,
    public override val baseBand: ByteArray,
    public override val version: VersionData,
    public override val simInfo: ByteArray,
    public override val osType: ByteArray,
    public override val macAddress: ByteArray,
    public override val wifiBSSID: ByteArray?,
    public override val wifiSSID: ByteArray?,
    public override val imsiMd5: ByteArray,
    public override val imei: String,
    public override val apn: ByteArray
) : DeviceInfo() {
    @Transient
    public override lateinit var context: Context

    public override val androidId: ByteArray get() = display

    @Serializable
    public class VersionData(
        public override val incremental: ByteArray = SystemDeviceInfo.Version.incremental,
        public override val release: ByteArray = SystemDeviceInfo.Version.release,
        public override val codename: ByteArray = SystemDeviceInfo.Version.codename,
        public override val sdk: Int = SystemDeviceInfo.Version.sdk
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