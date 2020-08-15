/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.utils

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import kotlinx.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import net.mamoe.mirai.utils.internal.md5
import java.io.File

/**
 * 加载一个设备信息. 若文件不存在或为空则随机并创建一个设备信息保存.
 */
@OptIn(UnstableDefault::class)
fun File.loadAsDeviceInfo(context: Context): DeviceInfo {
    if (!this.exists() || this.length() == 0L) {
        return SystemDeviceInfo(context).also {
            this.writeText(JSON.stringify(SystemDeviceInfo.serializer(), it))
        }
    }
    return JSON.parse(DeviceInfoData.serializer(), this.readText()).also {
        it.context = context
    }
}

@OptIn(UnstableDefault::class)
private val JSON = Json {
    isLenient = true
    ignoreUnknownKeys = true
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

    override val display: ByteArray get() = Build.DISPLAY.orEmpty().toByteArray()
    override val product: ByteArray get() = Build.PRODUCT.orEmpty().toByteArray()
    override val device: ByteArray get() = Build.DEVICE.orEmpty().toByteArray()
    override val board: ByteArray get() = Build.BOARD.orEmpty().toByteArray()
    override val brand: ByteArray get() = Build.BRAND.orEmpty().toByteArray()
    override val model: ByteArray get() = Build.MODEL.orEmpty().toByteArray()
    override val bootloader: ByteArray get() = Build.BOOTLOADER.orEmpty().toByteArray()

    override val baseBand: ByteArray
        @SuppressLint("PrivateApi")
        @Suppress("SpellCheckingInspection")
        get() = kotlin.runCatching {
            Class.forName("android.os.SystemProperties").let { clazz ->
                clazz.getMethod("get", String::class.java, String::class.java)
                    .invoke(clazz.newInstance(), "gsm.version.baseband", "no message")
                    ?.toString().orEmpty()
            }
        }.getOrElse { "" }.toByteArray()

    override val fingerprint: ByteArray get() = Build.FINGERPRINT.toByteArray()
    override val procVersion: ByteArray
        get() = kotlin.runCatching { File("/proc/version").useLines { it.firstOrNull().orEmpty() }.toByteArray() }
            .getOrElse { byteArrayOf() }
    override val bootId: ByteArray
        get() = kotlin.runCatching {
            File("/proc/sys/kernel/random/boot_id").useLines { it.firstOrNull().orEmpty() }.toByteArray()
        }.getOrEmpty()
    override val version: DeviceInfo.Version get() = Version

    override val simInfo: ByteArray
        get() = kotlin.runCatching {
            val telephonyManager =
                context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager.simState == 5) {
                telephonyManager.simOperatorName.orEmpty().toByteArray()
            } else byteArrayOf()
        }.getOrEmpty()

    override val osType: ByteArray = "android".toByteArray()
    override val macAddress: ByteArray get() = "02:00:00:00:00:00".toByteArray()
    override val wifiBSSID: ByteArray?
        get() = kotlin.runCatching {
            (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.bssid.orEmpty()
                .toByteArray()
        }.getOrEmpty()

    override val wifiSSID: ByteArray?
        get() = kotlin.runCatching {
            (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).connectionInfo.ssid.orEmpty()
                .toByteArray()
        }.getOrEmpty()
    override val imsiMd5: ByteArray
        @SuppressLint("HardwareIds")
        get() = kotlin.runCatching {
            (context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).subscriberId.orEmpty()
                .toByteArray()
        }.getOrEmpty().md5()

    override val imei: String
        @SuppressLint("HardwareIds")
        get() = kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).imei
            } else {
                @Suppress("DEPRECATION")
                (context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).deviceId
            }.orEmpty()
        }.getOrElse { "" }

    override val androidId: ByteArray get() = Build.ID.toByteArray()
    override val apn: ByteArray get() = "wifi".toByteArray()

    @Serializable
    actual object Version : DeviceInfo.Version {
        override val incremental: ByteArray get() = Build.VERSION.INCREMENTAL.orEmpty().toByteArray()
        override val release: ByteArray get() = Build.VERSION.RELEASE.orEmpty().toByteArray()
        override val codename: ByteArray get() = Build.VERSION.CODENAME.orEmpty().toByteArray()
        override val sdk: Int get() = Build.VERSION.SDK_INT
    }
}

private val EMPTY_BYTE_ARRAY: ByteArray = ByteArray(0)

@Suppress("NOTHING_TO_INLINE")
private inline fun Result<ByteArray>.getOrEmpty() = this.getOrNull() ?: EMPTY_BYTE_ARRAY