package net.mamoe.mirai.qqandroid.utils

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
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
        get() = kotlin.runCatching { File("/proc/version").useLines { it.firstOrNull() ?: "" }.toByteArray() }.getOrElse { byteArrayOf() }
    override val bootId: ByteArray
        get() = File("/proc/sys/kernel/random/boot_id").useLines { it.firstOrNull() ?: "" }.toByteArray()
    override val version: DeviceInfo.Version get() = Version

    override val simInfo: ByteArray
        get() {
            return kotlin.runCatching {
                val telephonyManager = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (telephonyManager.simState == 5) {
                    telephonyManager.simOperatorName.toByteArray()
                } else byteArrayOf()
            }.getOrElse { byteArrayOf() }
        }

    override val osType: ByteArray = "android".toByteArray()
    override val macAddress: ByteArray get() = "02:00:00:00:00:00".toByteArray()
    override val wifiBSSID: ByteArray?
        get() = kotlin.runCatching {
            (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.bssid.toByteArray()
        }.getOrElse { byteArrayOf() }

    override val wifiSSID: ByteArray?
        get() = kotlin.runCatching {
            (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.ssid.toByteArray()
        }.getOrElse { byteArrayOf() }

    override val imsiMd5: ByteArray
        @SuppressLint("HardwareIds")
        get() = md5(kotlin.runCatching {
            (context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).subscriberId.toByteArray()
        }.getOrElse { byteArrayOf() })
    override val imei: String
        @SuppressLint("HardwareIds")
        get() = kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).imei
            } else {
                @Suppress("DEPRECATION")
                (context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
            }
        }.getOrElse { "" }
    override val ksid: String get() = "|454001228437590|A8.2.0.27f6ea96" // get from T108
    override val ipAddress: String get() = localIpAddress()
    override val androidId: ByteArray get() = Build.ID.toByteArray()
    override val apn: ByteArray get() = "wifi".toByteArray()

    object Version : DeviceInfo.Version {
        override val incremental: ByteArray get() = Build.VERSION.INCREMENTAL.toByteArray()
        override val release: ByteArray get() = Build.VERSION.RELEASE.toByteArray()
        override val codename: ByteArray get() = Build.VERSION.CODENAME.toByteArray()
    }
}