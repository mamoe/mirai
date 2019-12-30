package net.mamoe.mirai.qqandroid.utils

import android.annotation.SuppressLint
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.io.core.toByteArray
import net.mamoe.mirai.utils.localIpAddress
import net.mamoe.mirai.utils.md5
import java.io.File

/**
 * Delegated by [Build]
 */
actual class SystemDeviceInfo actual constructor(context: Context) : DeviceInfo(context) {
    override val display: ByteArray get() = Build.DISPLAY.toByteArray()
    override val product: ByteArray get() = Build.PRODUCT.toByteArray()
    override val device: ByteArray get() = Build.DEVICE.toByteArray()
    override val board: ByteArray get() = Build.BOARD.toByteArray()
    override val brand: ByteArray get() = Build.BRAND.toByteArray()
    override val model: ByteArray get() = Build.MODEL.toByteArray()
    override val bootloader: ByteArray get() = Build.BOOTLOADER.toByteArray()

    override val baseBand: ByteArray
        @SuppressLint("PrivateApi")
        @Suppress("SpellCheckingInspection")
        get() = kotlin.runCatching {
            Class.forName("android.os.SystemProperties").let { clazz ->
                clazz.getMethod("get", String::class.java, String::class.java)
                    .invoke(clazz.newInstance(), "gsm.version.baseband", "no message")
                    ?.toString() ?: ""
            }
        }.getOrElse { "" }.toByteArray()

    override val fingerprint: ByteArray get() = Build.FINGERPRINT.toByteArray()
    override val procVersion: ByteArray
        get() = File("/proc/version").useLines { it.firstOrNull() ?: "" }.toByteArray()
    override val bootId: ByteArray
        get() = File("/proc/sys/kernel/random/boot_id").useLines { it.firstOrNull() ?: "" }.toByteArray()
    override val version: DeviceInfo.Version get() = Version

    override val simInfo: ByteArray
        @SuppressLint("WrongConstant")
        get() {
            return kotlin.runCatching {
                val telephonyManager = context.getSystemService("phone") as TelephonyManager
                if (telephonyManager.simState == 5) {
                    telephonyManager.simOperatorName.toByteArray()
                } else byteArrayOf()
            }.getOrElse { byteArrayOf() }
        }

    override val osType: ByteArray = "android".toByteArray()
    override val macAddress: ByteArray get() = "02:00:00:00:00:00".toByteArray()
    override val wifiBSSID: ByteArray?
        get() = TODO("not implemented")
    override val wifiSSID: ByteArray?
        get() = TODO("not implemented")
    override val imsiMd5: ByteArray // null due to permission READ_PHONE_STATE required
        get() = md5(byteArrayOf())/*{
            val telephonyManager = context.getSystemService("phone") as TelephonyManager
            if (telephonyManager != null) {
                val subscriberId = telephonyManager.subscriberId
                if (subscriberId != null) {
                    return subscriberId.toByteArray()
                }
            }
            return kotlin.runCatching {
                val telephonyManager = context.getSystemService("phone") as TelephonyManager
                if (telephonyManager != null) {
                    telephonyManager.subscriberId.toByteArray()
                } else byteArrayOf()
            }.getOrElse { byteArrayOf() }
        }*/
    override val ipAddress: ByteArray get() = localIpAddress().toByteArray()
    override val androidId: ByteArray get() = Build.ID.toByteArray()
    override val apn: ByteArray get() = "wifi".toByteArray()

    object Version : DeviceInfo.Version {
        override val incremental: ByteArray get() = Build.VERSION.INCREMENTAL.toByteArray()
        override val release: ByteArray get() = Build.VERSION.RELEASE.toByteArray()
        override val codename: ByteArray get() = Build.VERSION.CODENAME.toByteArray()
    }
}