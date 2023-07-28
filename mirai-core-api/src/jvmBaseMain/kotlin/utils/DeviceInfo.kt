/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random

/**
 * 表示设备信息
 * @see DeviceInfoBuilder
 */
@Serializable(DeviceInfoV1LegacySerializer::class)
public actual class DeviceInfo
@Deprecated(DeviceInfoConstructorDeprecationMessage, level = DeprecationLevel.WARNING)
@DeprecatedSinceMirai(warningSince = "2.15") // planned internal
public actual constructor(
    public actual val display: ByteArray,
    public actual val product: ByteArray,
    public actual val device: ByteArray,
    public actual val board: ByteArray,
    public actual val brand: ByteArray,
    public actual val model: ByteArray,
    public actual val bootloader: ByteArray,
    public actual val fingerprint: ByteArray,
    public actual val bootId: ByteArray,
    public actual val procVersion: ByteArray,
    public actual val baseBand: ByteArray,
    public actual val version: Version,
    public actual val simInfo: ByteArray,
    public actual val osType: ByteArray,
    public actual val macAddress: ByteArray,
    public actual val wifiBSSID: ByteArray,
    public actual val wifiSSID: ByteArray,
    public actual val imsiMd5: ByteArray,
    public actual val imei: String,
    public actual val apn: ByteArray,
    public actual val androidId: ByteArray,
) {
    @Deprecated(
        DeviceInfoConstructorDeprecationMessage,
        replaceWith = ReplaceWith(
            "net.mamoe.mirai.utils.DeviceInfo(display, product, device, board, brand, model, " +
                    "bootloader, fingerprint, bootId, procVersion, baseBand, version, simInfo, osType, " +
                    "macAddress, wifiBSSID, wifiSSID, imsiMd5, imei, apn, androidId)"
        ),
        level = DeprecationLevel.WARNING
    )
    @DeprecatedSinceMirai(warningSince = "2.15")
    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    public constructor(
        display: ByteArray,
        product: ByteArray,
        device: ByteArray,
        board: ByteArray,
        brand: ByteArray,
        model: ByteArray,
        bootloader: ByteArray,
        fingerprint: ByteArray,
        bootId: ByteArray,
        procVersion: ByteArray,
        baseBand: ByteArray,
        version: Version,
        simInfo: ByteArray,
        osType: ByteArray,
        macAddress: ByteArray,
        wifiBSSID: ByteArray,
        wifiSSID: ByteArray,
        imsiMd5: ByteArray,
        imei: String,
        apn: ByteArray
    ) : this(
        display, product, device, board, brand, model, bootloader,
        fingerprint, bootId, procVersion, baseBand, version, simInfo,
        osType, macAddress, wifiBSSID, wifiSSID, imsiMd5, imei, apn,
        androidId = display
    )

    public actual val ipAddress: ByteArray get() = byteArrayOf(192.toByte(), 168.toByte(), 1, 123)

    init {
        require(imsiMd5.size == 16) { "Bad `imsiMd5.size`. Required 16, given ${imsiMd5.size}." }
    }

    @Transient
    @MiraiInternalApi
    public actual val guid: ByteArray = generateGuid(androidId, macAddress)

    @Serializable
    public actual class Version actual constructor(
        public actual val incremental: ByteArray,
        public actual val release: ByteArray,
        public actual val codename: ByteArray,
        public actual val sdk: Int
    ) {
        /**
         * @since 2.9
         */
        actual override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Version) return false

            if (!incremental.contentEquals(other.incremental)) return false
            if (!release.contentEquals(other.release)) return false
            if (!codename.contentEquals(other.codename)) return false
            if (sdk != other.sdk) return false

            return true
        }

        /**
         * @since 2.9
         */
        actual override fun hashCode(): Int {
            var result = incremental.contentHashCode()
            result = 31 * result + release.contentHashCode()
            result = 31 * result + codename.contentHashCode()
            result = 31 * result + sdk
            return result
        }
    }

    public actual companion object {
        internal actual val logger = MiraiLogger.Factory.create(DeviceInfo::class, "DeviceInfo")

        /**
         * 加载一个设备信息. 若文件不存在或为空则随机并创建一个设备信息保存.
         */
        @JvmOverloads
        @JvmStatic
        @JvmName("from")
        public fun File.loadAsDeviceInfo(
            json: Json = DeviceInfoManager.format
        ): DeviceInfo {
            if (!this.exists() || this.length() == 0L) {
                return random().also {
                    this.writeText(DeviceInfoManager.serialize(it, json))
                }
            }
            return DeviceInfoManager.deserialize(this.readText(), json) upg@{ upg ->
                if (!this.canWrite()) {
                    logger.warning("Device info file $this is not writable, failed to upgrade legacy device info.")
                    return@upg
                }
                try {
                    this.writeText(DeviceInfoManager.serialize(upg, json))
                } catch (ex: SecurityException) {
                    logger.warning("Device info file $this is not writable, failed to upgrade legacy device info.", ex)
                }
            }
        }

        /**
         * 生成随机 [DeviceInfo]
         *
         * @see DeviceInfoBuilder
         * @since 2.0
         */
        @JvmStatic
        public actual fun random(): DeviceInfo = random(Random.Default)

        /**
         * 使用特定随机数生成器生成 [DeviceInfo]
         *
         * @see DeviceInfoBuilder
         * @since 2.9
         */
        @JvmStatic
        public actual fun random(random: Random): DeviceInfo {
            return DeviceInfoCommonImpl.randomDeviceInfo(random)
        }

        /**
         * 将此 [DeviceInfo] 序列化为字符串. 序列化的字符串可以在以后通过 [DeviceInfo.deserializeFromString] 反序列化为 [DeviceInfo].
         *
         * 序列化的字符串有兼容性保证, 在旧版 mirai 序列化的字符串, 可以在新版 mirai 使用. 但新版 mirai 序列化的字符串不一定能在旧版使用.
         *
         * @since 2.15
         */
        @JvmStatic
        public actual fun serializeToString(deviceInfo: DeviceInfo): String = DeviceInfoManager.serialize(deviceInfo)

        /**
         * 将通过 [serializeToString] 序列化得到的字符串反序列化为 [DeviceInfo].
         * 此函数兼容旧版 mirai 序列化的字符串.
         * @since 2.15
         */
        @JvmStatic
        public actual fun deserializeFromString(string: String): DeviceInfo = DeviceInfoManager.deserialize(string)
    }

    /**
     * @since 2.9
     */
    @Suppress("DuplicatedCode")
    actual override fun equals(other: Any?): Boolean {
        return DeviceInfoCommonImpl.equalsImpl(this, other)
    }


    /**
     * @since 2.9
     */
    actual override fun hashCode(): Int {
        return DeviceInfoCommonImpl.hashCodeImpl(this)
    }

    @Suppress("ClassName")
    @Deprecated("For binary compatibility", level = DeprecationLevel.HIDDEN)
    public object `$serializer` : KSerializer<DeviceInfo> by DeviceInfoV1LegacySerializer
}
