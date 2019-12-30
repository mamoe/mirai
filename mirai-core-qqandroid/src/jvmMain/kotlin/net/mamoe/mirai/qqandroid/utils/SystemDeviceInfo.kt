package net.mamoe.mirai.qqandroid.utils

import net.mamoe.mirai.utils.md5

actual class SystemDeviceInfo actual constructor(context: Context) : DeviceInfo(context) {
    override val display: ByteArray get() = TODO("not implemented")
    override val product: ByteArray get() = TODO("not implemented")
    override val device: ByteArray get() = TODO("not implemented")
    override val board: ByteArray get() = TODO("not implemented")
    override val brand: ByteArray get() = TODO("not implemented")
    override val model: ByteArray get() = TODO("not implemented")
    override val bootloader: ByteArray get() = TODO("not implemented")
    override val fingerprint: ByteArray get() = TODO("not implemented")
    override val bootId: ByteArray get() = TODO("not implemented")
    override val procVersion: ByteArray get() = "Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com)".toByteArray()
    override val baseBand: ByteArray get() = TODO()
    override val version: DeviceInfo.Version get() = TODO("not implemented")
    override val simInfo: ByteArray get() = TODO("not implemented")
    override val osType: ByteArray get() = TODO("not implemented")
    override val macAddress: ByteArray get() = TODO("not implemented")
    override val wifiBSSID: ByteArray?
        get() = null
    override val wifiSSID: ByteArray?
        get() = null
    override val imsiMd5: ByteArray get() = md5(byteArrayOf())
    override val ipAddress: ByteArray get() = TODO("not implemented")
    override val androidId: ByteArray get() = TODO("not implemented")
    override val apn: ByteArray get() = TODO("not implemented")

    object Version : DeviceInfo.Version {
        override val incremental: ByteArray get() = TODO("not implemented")
        override val release: ByteArray get() = TODO("not implemented")
        override val codename: ByteArray get() = TODO("not implemented")
    }
}

actual abstract class Context

open class ContextImpl : Context()