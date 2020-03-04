/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 加载一个设备信息. 若文件不存在或为空则随机并创建一个设备信息保存.
 */
@OptIn(UnstableDefault::class)
fun File.loadAsDeviceInfo(context: Context): DeviceInfo {
    if (!this.exists() || this.length() == 0L) {
        return SystemDeviceInfo(context).also {
            this.writeText(Json.plain.stringify(SystemDeviceInfo.serializer(), it))
        }
    }
    return Json.nonstrict.parse(DeviceInfoData.serializer(), this.readText()).also {
        it.context = context
    }
}

/**
 * 部分引用指向 [Build].
 * 部分需要权限, 若无权限则会使用默认值.
 */
@Serializable
actual open class SystemDeviceInfo actual constructor() : DeviceInfo() {
    actual constructor(context: Context) : this() {
        this.context = context
    }

    @Transient
    final override lateinit var context: Context

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
    override val ipAddress: ByteArray get() = localIpAddress().split(".").map { it.toByte() }.takeIf { it.size == 4 }?.toByteArray() ?: byteArrayOf()
    override val androidId: ByteArray get() = Build.ID.toByteArray()
    override val apn: ByteArray get() = "wifi".toByteArray()

    @Serializable
    actual object Version : DeviceInfo.Version {
        override val incremental: ByteArray get() = Build.VERSION.INCREMENTAL.toByteArray()
        override val release: ByteArray get() = Build.VERSION.RELEASE.toByteArray()
        override val codename: ByteArray get() = Build.VERSION.CODENAME.toByteArray()
        override val sdk: Int get() = Build.VERSION.SDK_INT
    }
}