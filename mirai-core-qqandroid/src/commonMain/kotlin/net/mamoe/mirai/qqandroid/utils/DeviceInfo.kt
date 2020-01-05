package net.mamoe.mirai.qqandroid.utils

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf

abstract class DeviceInfo(
    val context: Context
) {
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
    abstract val ksid: String

    abstract val ipAddress: String

    abstract val androidId: ByteArray

    abstract val apn: ByteArray

    val guid: ByteArray get() = generateGuid(androidId, macAddress)

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
    }
}