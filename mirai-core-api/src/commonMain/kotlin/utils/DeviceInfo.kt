/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic
import kotlin.random.Random

internal const val DeviceInfoConstructorDeprecationMessage =
    "Constructor and serializer of DeviceInfo is deprecated and will be removed in the future." +
            "This is because new properties can be added and it requires too much effort to maintain public stability." +
            "Please use DeviceInfo.serializeToString and DeviceInfo.deserializeFromString instead."

/**
 * 表示设备信息
 *
 * ## 自定义设备信息
 *
 *
 */
public expect class DeviceInfo
@Deprecated(DeviceInfoConstructorDeprecationMessage, level = DeprecationLevel.WARNING)
@DeprecatedSinceMirai(warningSince = "2.15") // planned internal
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
    apn: ByteArray,
    androidId: ByteArray,
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

    // @Serializable: use DeviceInfoVersionSerializer in commonMain.
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

        internal companion object {
            fun serializer(): KSerializer<Version>
        }
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

        @Deprecated(DeviceInfoConstructorDeprecationMessage, level = DeprecationLevel.WARNING)
        @DeprecatedSinceMirai(warningSince = "2.15") // planned internal
        public fun serializer(): KSerializer<DeviceInfo>

        /**
         * 将此 [DeviceInfo] 序列化为字符串. 序列化的字符串可以在以后通过 [DeviceInfo.deserializeFromString] 反序列化为 [DeviceInfo].
         *
         * 序列化的字符串有兼容性保证, 在旧版 mirai 序列化的字符串, 可以在新版 mirai 使用. 但新版 mirai 序列化的字符串不一定能在旧版使用.
         *
         * @since 2.15
         */
        @JvmStatic
        public fun serializeToString(deviceInfo: DeviceInfo): String

        /**
         * 将通过 [serializeToString] 序列化得到的字符串反序列化为 [DeviceInfo].
         * 此函数兼容旧版 mirai 序列化的字符串.
         * @since 2.15
         */
        @JvmStatic
        public fun deserializeFromString(string: String): DeviceInfo
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

/**
 * 将此 [DeviceInfo] 序列化为字符串. 序列化的字符串可以在以后通过 [DeviceInfo.deserializeFromString] 反序列化为 [DeviceInfo].
 *
 * 序列化的字符串有兼容性保证, 在旧版 mirai 序列化的字符串, 可以在新版 mirai 使用. 但新版 mirai 序列化的字符串不一定能在旧版使用.
 *
 * @since 2.15
 */
@JvmSynthetic
public fun DeviceInfo.serializeToString(): String = DeviceInfo.serializeToString(this)

@Serializable
private class DevInfo @OptIn(ExperimentalSerializationApi::class) constructor(
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

/**
 * 不要使用这个 API, 此 API 在未来可能会被删除
 */
public fun DeviceInfo.generateDeviceInfoData(): ByteArray { // ?? why is this public?

    @OptIn(ExperimentalSerializationApi::class)
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

/**
 * @see DeviceInfoManager
 */
internal object DeviceInfoCommonImpl {
    @Suppress("DEPRECATION")
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
        apn = "wifi".toByteArray(),
        androidId = getRandomByteArray(8, random).toUHexString("").lowercase().encodeToByteArray()
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


    @OptIn(MiraiInternalApi::class)
    @Suppress("DuplicatedCode")
    fun equalsImpl(deviceInfo: DeviceInfo, other: Any?): Boolean = deviceInfo.run {
        if (deviceInfo === other) return true
        if (!isSameType(this, other)) return false

        // also remember to add equal compare to JvmDeviceInfoTest.`can read legacy v1`
        // when adding new field compare here.
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
        if (!androidId.contentEquals(other.androidId)) return false

        return true
    }

    @OptIn(MiraiInternalApi::class)
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
        result = 31 * result + androidId.contentHashCode()
        return result
    }
}