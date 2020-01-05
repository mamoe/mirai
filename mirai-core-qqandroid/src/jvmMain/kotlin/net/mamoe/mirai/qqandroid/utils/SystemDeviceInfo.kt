package net.mamoe.mirai.qqandroid.utils

import kotlinx.io.core.toByteArray
import net.mamoe.mirai.utils.localIpAddress

actual class SystemDeviceInfo actual constructor(context: Context) : DeviceInfo(context) {
    override val display: ByteArray get() = "QSR1.190920.001".toByteArray()
    override val product: ByteArray get() = "sdk_gphone_x86".toByteArray()
    override val device: ByteArray get() = "generic_x86".toByteArray()
    override val board: ByteArray get() = "goldfish_x86".toByteArray()
    override val brand: ByteArray get() = "google".toByteArray()
    override val model: ByteArray get() = "Android SDK built for x86".toByteArray()
    override val bootloader: ByteArray get() = "unknown".toByteArray()
    override val fingerprint: ByteArray get() = "google/sdk_gphone_x86/generic_x86:10/QSR1.190920.001/5891938:user/release-keys".toByteArray()
    override val bootId: ByteArray get() = "5974cb66-bb69-4e82-a436-836b98ebd88c".toByteArray()
    override val procVersion: ByteArray get() = "Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com)".toByteArray()
    override val baseBand: ByteArray get() = byteArrayOf()
    override val version: DeviceInfo.Version get() = Version
    override val simInfo: ByteArray get() = "T-Mobile".toByteArray()
    override val osType: ByteArray get() = "android".toByteArray()
    override val macAddress: ByteArray get() = "02:00:00:00:00:00".toByteArray()
    override val wifiBSSID: ByteArray?
        get() = "02:00:00:00:00:00".toByteArray()
    override val wifiSSID: ByteArray?
        get() = "<unknown ssid>".toByteArray()
    @UseExperimental(ExperimentalUnsignedTypes::class)
    override val imsiMd5: ByteArray
        get() = ubyteArrayOf(0xD4u, 0x1Du, 0x8Cu, 0xD9u, 0x8Fu, 0x00u, 0xB2u, 0x04u, 0xE9u, 0x80u, 0x09u, 0x98u, 0xECu, 0xF8u, 0x42u, 0x7Eu).toByteArray()
    override val imei: String get() = "858414369211993"
    override val ksid: String get() = "|454001228437590|A8.2.0.27f6ea96"
    override val ipAddress: String get() = localIpAddress()
    override val androidId: ByteArray get() = "QSR1.190920.001".toByteArray()
    override val apn: ByteArray get() = "wifi".toByteArray()

    object Version : DeviceInfo.Version {
        override val incremental: ByteArray get() = "5891938".toByteArray()
        override val release: ByteArray get() = "10".toByteArray()
        override val codename: ByteArray get() = "REL".toByteArray()
    }
}

actual abstract class Context

open class ContextImpl : Context()