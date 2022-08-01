/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.utils.DeviceInfoManager.Version.Companion.trans
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic
import kotlin.random.Random

public expect class DeviceInfo(
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
) {

    public val display: ByteArray
    public val product: ByteArray
    public val device: ByteArray
    public val board: ByteArray
    public val brand: ByteArray
    public val model: ByteArray
    public val bootloader: ByteArray
    public val fingerprint: ByteArray
    public val bootId: ByteArray
    public val procVersion: ByteArray
    public val baseBand: ByteArray
    public val version: Version
    public val simInfo: ByteArray
    public val osType: ByteArray
    public val macAddress: ByteArray
    public val wifiBSSID: ByteArray
    public val wifiSSID: ByteArray
    public val imsiMd5: ByteArray
    public val imei: String
    public val apn: ByteArray

    public val androidId: ByteArray
    public val ipAddress: ByteArray

    @Transient
    @MiraiInternalApi
    public val guid: ByteArray

    public class Version(
        incremental: ByteArray = "5891938".toByteArray(),
        release: ByteArray = "10".toByteArray(),
        codename: ByteArray = "REL".toByteArray(),
        sdk: Int = 29
    ) {
        public val incremental: ByteArray
        public val release: ByteArray
        public val codename: ByteArray
        public val sdk: Int

        /**
         * @since 2.9
         */
        override fun equals(other: Any?): Boolean

        /**
         * @since 2.9
         */
        override fun hashCode(): Int
    }

    public companion object {
        internal val logger: MiraiLogger

        /**
         * 生成随机 [DeviceInfo]
         *
         * @since 2.0
         */
        @JvmStatic
        public fun random(): DeviceInfo

        /**
         * 使用特定随机数生成器生成 [DeviceInfo]
         *
         * @since 2.9
         */
        @JvmStatic
        public fun random(random: Random): DeviceInfo
    }

    /**
     * @since 2.9
     */
    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean

    /**
     * @since 2.9
     */
    override fun hashCode(): Int
}

internal object DeviceInfoCommonImpl {
    fun randomDeviceInfo(random: Random) = DeviceInfo(
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
        version = DeviceInfo.Version(),
        simInfo = "T-Mobile".toByteArray(),
        osType = "android".toByteArray(),
        macAddress = "02:00:00:00:00:00".toByteArray(),
        wifiBSSID = "02:00:00:00:00:00".toByteArray(),
        wifiSSID = "<unknown ssid>".toByteArray(),
        imsiMd5 = getRandomByteArray(16, random).md5(),
        imei = "86${getRandomIntString(12, random)}".let { it + luhn(it) },
        apn = "wifi".toByteArray()
    )

    /**
     * 计算 imei 校验位
     */
    private fun luhn(imei: String): Int {
        var odd = false
        val zero = '0'
        val sum = imei.sumOf { char ->
            odd = !odd
            if (odd) {
                char.code - zero.code
            } else {
                val s = (char.code - zero.code) * 2
                s % 10 + s / 10
            }
        }
        return (10 - sum % 10) % 10
    }


    @Suppress("DuplicatedCode")
    fun equalsImpl(deviceInfo: DeviceInfo, other: Any?): Boolean = deviceInfo.run {
        if (deviceInfo === other) return true
        if (!isSameType(this, other)) return false

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

    @Suppress("DuplicatedCode")
    fun hashCodeImpl(deviceInfo: DeviceInfo): Int = deviceInfo.run {
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

internal object DeviceInfoManager {
    sealed interface Info {
        fun toDeviceInfo(): DeviceInfo
    }

    @Serializable(HexStringSerializer::class)
    @JvmInline
    value class HexString(
        val data: ByteArray
    )

    object HexStringSerializer : KSerializer<HexString> by String.serializer().map(
        String.serializer().descriptor.copy("HexString"),
        deserialize = { HexString(it.hexToBytes()) },
        serialize = { it.data.toUHexString("").lowercase() }
    )

    // Note: property names must be kept intact during obfuscation process if applied.
    @Serializable
    class Wrapper<T : Info>(
        @Suppress("unused") val deviceInfoVersion: Int, // used by plain jsonObject
        val data: T
    )

    @Serializer(forClass = DeviceInfo.Version::class)
    private object DeviceInfoVersionSerializer

    @Serializable
    class V1(
        val display: ByteArray,
        val product: ByteArray,
        val device: ByteArray,
        val board: ByteArray,
        val brand: ByteArray,
        val model: ByteArray,
        val bootloader: ByteArray,
        val fingerprint: ByteArray,
        val bootId: ByteArray,
        val procVersion: ByteArray,
        val baseBand: ByteArray,
        val version: @Serializable(DeviceInfoVersionSerializer::class) DeviceInfo.Version,
        val simInfo: ByteArray,
        val osType: ByteArray,
        val macAddress: ByteArray,
        val wifiBSSID: ByteArray,
        val wifiSSID: ByteArray,
        val imsiMd5: ByteArray,
        val imei: String,
        val apn: ByteArray
    ) : Info {
        override fun toDeviceInfo(): DeviceInfo {
            return DeviceInfo(
                display = display,
                product = product,
                device = device,
                board = board,
                brand = brand,
                model = model,
                bootloader = bootloader,
                fingerprint = fingerprint,
                bootId = bootId,
                procVersion = procVersion,
                baseBand = baseBand,
                version = version,
                simInfo = simInfo,
                osType = osType,
                macAddress = macAddress,
                wifiBSSID = wifiBSSID,
                wifiSSID = wifiSSID,
                imsiMd5 = imsiMd5,
                imei = imei,
                apn = apn
            )
        }
    }


    @Serializable
    class V2(
        val display: String,
        val product: String,
        val device: String,
        val board: String,
        val brand: String,
        val model: String,
        val bootloader: String,
        val fingerprint: String,
        val bootId: String,
        val procVersion: String,
        val baseBand: HexString,
        val version: Version,
        val simInfo: String,
        val osType: String,
        val macAddress: String,
        val wifiBSSID: String,
        val wifiSSID: String,
        val imsiMd5: HexString,
        val imei: String,
        val apn: String
    ) : Info {
        override fun toDeviceInfo(): DeviceInfo = DeviceInfo(
            this.display.toByteArray(),
            this.product.toByteArray(),
            this.device.toByteArray(),
            this.board.toByteArray(),
            this.brand.toByteArray(),
            this.model.toByteArray(),
            this.bootloader.toByteArray(),
            this.fingerprint.toByteArray(),
            this.bootId.toByteArray(),
            this.procVersion.toByteArray(),
            this.baseBand.data,
            this.version.trans(),
            this.simInfo.toByteArray(),
            this.osType.toByteArray(),
            this.macAddress.toByteArray(),
            this.wifiBSSID.toByteArray(),
            this.wifiSSID.toByteArray(),
            this.imsiMd5.data,
            this.imei,
            this.apn.toByteArray()
        )
    }

    @Serializable
    class Version(
        val incremental: String,
        val release: String,
        val codename: String,
        val sdk: Int = 29
    ) {
        companion object {
            fun DeviceInfo.Version.trans(): Version {
                return Version(incremental.decodeToString(), release.decodeToString(), codename.decodeToString(), sdk)
            }

            fun Version.trans(): DeviceInfo.Version {
                return DeviceInfo.Version(incremental.toByteArray(), release.toByteArray(), codename.toByteArray(), sdk)
            }
        }
    }

    fun DeviceInfo.toCurrentInfo(): V2 = V2(
        display.decodeToString(),
        product.decodeToString(),
        device.decodeToString(),
        board.decodeToString(),
        brand.decodeToString(),
        model.decodeToString(),
        bootloader.decodeToString(),
        fingerprint.decodeToString(),
        bootId.decodeToString(),
        procVersion.decodeToString(),
        HexString(baseBand),
        version.trans(),
        simInfo.decodeToString(),
        osType.decodeToString(),
        macAddress.decodeToString(),
        wifiBSSID.decodeToString(),
        wifiSSID.decodeToString(),
        HexString(imsiMd5),
        imei,
        apn.decodeToString()
    )

    internal val format = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Throws(IllegalArgumentException::class, NumberFormatException::class) // in case malformed
    fun deserialize(string: String, format: Json = this.format): DeviceInfo {
        val element = format.parseToJsonElement(string)

        return when (val version = element.jsonObject["deviceInfoVersion"]?.jsonPrimitive?.content?.toInt() ?: 1) {
            /**
             * @since 2.0
             */
            1 -> format.decodeFromJsonElement(V1.serializer(), element)
            /**
             * @since 2.9
             */
            2 -> format.decodeFromJsonElement(Wrapper.serializer(V2.serializer()), element).data
            else -> throw IllegalArgumentException("Unsupported deviceInfoVersion: $version")
        }.toDeviceInfo()
    }

    fun serialize(info: DeviceInfo, format: Json = this.format): String {
        return format.encodeToString(
            Wrapper.serializer(V2.serializer()),
            Wrapper(2, info.toCurrentInfo())
        )
    }

    fun toJsonElement(info: DeviceInfo, format: Json = this.format): JsonElement {
        return format.encodeToJsonElement(
            Wrapper.serializer(V2.serializer()),
            Wrapper(2, info.toCurrentInfo())
        )
    }
}

/**
 * Defaults "%4;7t>;28<fc.5*6".toByteArray()
 */
@Suppress("RemoveRedundantQualifierName") // bug
internal fun generateGuid(androidId: ByteArray, macAddress: ByteArray): ByteArray =
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