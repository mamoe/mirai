/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import java.io.File
import kotlin.random.Random

@Serializable
public class DeviceInfo(
    public val display: ByteArray,
    public val product: ByteArray,
    public val device: ByteArray,
    public val board: ByteArray,
    public val brand: ByteArray,
    public val model: ByteArray,
    public val bootloader: ByteArray,
    public val fingerprint: ByteArray,
    public val bootId: ByteArray,
    public val procVersion: ByteArray,
    public val baseBand: ByteArray,
    public val version: Version,
    public val simInfo: ByteArray,
    public val osType: ByteArray,
    public val macAddress: ByteArray,
    public val wifiBSSID: ByteArray,
    public val wifiSSID: ByteArray,
    public val imsiMd5: ByteArray,
    public val imei: String,
    public val apn: ByteArray
) {
    public val androidId: ByteArray get() = display
    public val ipAddress: ByteArray get() = byteArrayOf(192.toByte(), 168.toByte(), 1, 123)

    init {
        require(imsiMd5.size == 16) { "Bad `imsiMd5.size`. Required 16, given ${imsiMd5.size}." }
    }

    @Transient
    @MiraiInternalApi
    public val guid: ByteArray = generateGuid(androidId, macAddress)

    @Serializable
    public class Version(
        public val incremental: ByteArray = "5891938".toByteArray(),
        public val release: ByteArray = "10".toByteArray(),
        public val codename: ByteArray = "REL".toByteArray(),
        public val sdk: Int = 29
    ) {
        /**
         * @since 2.9
         */
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Version

            if (!incremental.contentEquals(other.incremental)) return false
            if (!release.contentEquals(other.release)) return false
            if (!codename.contentEquals(other.codename)) return false
            if (sdk != other.sdk) return false

            return true
        }

        /**
         * @since 2.9
         */
        override fun hashCode(): Int {
            var result = incremental.contentHashCode()
            result = 31 * result + release.contentHashCode()
            result = 31 * result + codename.contentHashCode()
            result = 31 * result + sdk
            return result
        }
    }

    public companion object {
        internal val logger = MiraiLogger.Factory.create(DeviceInfo::class, "DeviceInfo")

        /**
         * 加载一个设备信息. 若文件不存在或为空则随机并创建一个设备信息保存.
         */
        @JvmOverloads
        @JvmStatic
        @JvmName("from")
        public fun File.loadAsDeviceInfo(
            json: Json = Json
        ): DeviceInfo {
            if (!this.exists() || this.length() == 0L) {
                return random().also {
                    this.writeText(json.encodeToString(serializer(), it))
                }
            }
            return json.decodeFromString(serializer(), this.readText())
        }

        /**
         * 生成随机 [DeviceInfo]
         *
         * @since 2.0
         */
        @JvmStatic
        public fun random(): DeviceInfo = random(Random.Default)

        /**
         * 使用特定随机数生成器生成 [DeviceInfo]
         *
         * @since 2.9
         */
        @JvmStatic
        public fun random(random: Random): DeviceInfo {
            return DeviceInfo(
                display = "MIRAI.${getRandomString(6, '0'..'9', random)}.001".toByteArray(),
                product = "mirai".toByteArray(),
                device = "mirai".toByteArray(),
                board = "mirai".toByteArray(),
                brand = "mamoe".toByteArray(),
                model = "mirai".toByteArray(),
                bootloader = "unknown".toByteArray(),
                fingerprint = "mamoe/mirai/mirai:10/MIRAI.200122.001/${
                    getRandomIntString(7, random)
                }:user/release-keys".toByteArray(),
                bootId = generateUUID(getRandomByteArray(16, random).md5()).toByteArray(),
                procVersion = "Linux version 3.0.31-${
                    getRandomString(8, random)
                } (android-build@xxx.xxx.xxx.xxx.com)".toByteArray(),
                baseBand = byteArrayOf(),
                version = Version(),
                simInfo = "T-Mobile".toByteArray(),
                osType = "android".toByteArray(),
                macAddress = "02:00:00:00:00:00".toByteArray(),
                wifiBSSID = "02:00:00:00:00:00".toByteArray(),
                wifiSSID = "<unknown ssid>".toByteArray(),
                imsiMd5 = getRandomByteArray(16, random).md5(),
                imei = getRandomIntString(15, random),
                apn = "wifi".toByteArray()
            )
        }
    }

    /**
     * @since 2.9
     */
    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceInfo

        if (!display.contentEquals(other.display)) return false
        if (!product.contentEquals(other.product)) return false
        if (!device.contentEquals(other.device)) return false
        if (!board.contentEquals(other.board)) return false
        if (!brand.contentEquals(other.brand)) return false
        if (!model.contentEquals(other.model)) return false
        if (!bootloader.contentEquals(other.bootloader)) return false
        if (!fingerprint.contentEquals(other.fingerprint)) return false
        if (!bootId.contentEquals(other.bootId)) return false
        if (!procVersion.contentEquals(other.procVersion)) return false
        if (!baseBand.contentEquals(other.baseBand)) return false
        if (version != other.version) return false
        if (!simInfo.contentEquals(other.simInfo)) return false
        if (!osType.contentEquals(other.osType)) return false
        if (!macAddress.contentEquals(other.macAddress)) return false
        if (!wifiBSSID.contentEquals(other.wifiBSSID)) return false
        if (!wifiSSID.contentEquals(other.wifiSSID)) return false
        if (!imsiMd5.contentEquals(other.imsiMd5)) return false
        if (imei != other.imei) return false
        if (!apn.contentEquals(other.apn)) return false
        if (!guid.contentEquals(other.guid)) return false

        return true
    }

    /**
     * @since 2.9
     */
    override fun hashCode(): Int {
        var result = display.contentHashCode()
        result = 31 * result + product.contentHashCode()
        result = 31 * result + device.contentHashCode()
        result = 31 * result + board.contentHashCode()
        result = 31 * result + brand.contentHashCode()
        result = 31 * result + model.contentHashCode()
        result = 31 * result + bootloader.contentHashCode()
        result = 31 * result + fingerprint.contentHashCode()
        result = 31 * result + bootId.contentHashCode()
        result = 31 * result + procVersion.contentHashCode()
        result = 31 * result + baseBand.contentHashCode()
        result = 31 * result + version.hashCode()
        result = 31 * result + simInfo.contentHashCode()
        result = 31 * result + osType.contentHashCode()
        result = 31 * result + macAddress.contentHashCode()
        result = 31 * result + wifiBSSID.contentHashCode()
        result = 31 * result + wifiSSID.contentHashCode()
        result = 31 * result + imsiMd5.contentHashCode()
        result = 31 * result + imei.hashCode()
        result = 31 * result + apn.contentHashCode()
        result = 31 * result + guid.contentHashCode()
        return result
    }
}

@Serializable
private class DevInfo(
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

public fun DeviceInfo.generateDeviceInfoData(): ByteArray {
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

/**
 * Defaults "%4;7t>;28<fc.5*6".toByteArray()
 */
@Suppress("RemoveRedundantQualifierName") // bug
private fun generateGuid(androidId: ByteArray, macAddress: ByteArray): ByteArray =
    (androidId + macAddress).md5()


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