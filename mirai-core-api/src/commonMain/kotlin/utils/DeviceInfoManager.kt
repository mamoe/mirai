/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import io.ktor.utils.io.core.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.utils.DeviceInfoManager.Version.Companion.trans
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmName

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
        deserialize = {
            HexString(it.hexToBytes())
        },
        serialize = { it.data.toUHexString("").lowercase() }
    )

    // Note: property names must be kept intact during obfuscation process if applied.
    @Serializable
    class Wrapper<T : Info>(
        @Suppress("unused") val deviceInfoVersion: Int, // used by plain jsonObject
        val data: T
    )

    internal object DeviceInfoVersionSerializer : KSerializer<DeviceInfo.Version> by SerialData.serializer().map(
        resultantDescriptor = SerialData.serializer().descriptor,
        deserialize = {
            DeviceInfo.Version(incremental, release, codename, sdk)
        },
        serialize = {
            SerialData(incremental, release, codename, sdk)
        }
    ) {
        @SerialName("Version")
        @Serializable
        private class SerialData(
            val incremental: ByteArray = "5891938".toByteArray(),
            val release: ByteArray = "10".toByteArray(),
            val codename: ByteArray = "REL".toByteArray(),
            val sdk: Int = 29
        )
    }

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
            @Suppress("DEPRECATION", "DEPRECATION_ERROR")
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
                apn = apn,
                androidId = getRandomByteArray(8).toUHexString("").lowercase().encodeToByteArray()
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
        @Suppress("DEPRECATION", "DEPRECATION_ERROR")
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
            this.apn.toByteArray(),
            androidId = getRandomByteArray(8).toUHexString("").lowercase().encodeToByteArray()
        )
    }


    @Serializable
    class V3(
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
        val apn: String,
        val androidId: String,
    ) : Info {
        @Suppress("DEPRECATION", "DEPRECATION_ERROR")
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
            this.apn.toByteArray(),
            this.androidId.toByteArray()
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

    fun DeviceInfo.toCurrentInfo(): V3 = V3(
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
        apn.decodeToString(),
        androidId.decodeToString(),
    )

    internal val format = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Suppress("unused")
    @Deprecated("ABI compatibility for device generator", level = DeprecationLevel.HIDDEN)
    @JvmName("deserialize")
    fun deserializeDeprecated(
        string: String,
        format: Json = this.format,
    ): DeviceInfo = deserialize(string, format)

    @Throws(IllegalArgumentException::class, NumberFormatException::class) // in case malformed
    fun deserialize(
        string: String,
        format: Json = this.format,
        onUpgradeVersion: (DeviceInfo) -> Unit = { }
    ): DeviceInfo {
        val element = format.parseToJsonElement(string)
        val version = element.jsonObject["deviceInfoVersion"]?.jsonPrimitive?.content?.toInt() ?: 1

        val deviceInfo = when (version) {
            /**
             * @since 2.0
             */
            1 -> format.decodeFromJsonElement(V1.serializer(), element)
            /**
             * @since 2.9
             */
            2 -> format.decodeFromJsonElement(Wrapper.serializer(V2.serializer()), element).data
            /**
             * @since 2.15
             */
            3 -> format.decodeFromJsonElement(Wrapper.serializer(V3.serializer()), element).data
            else -> throw IllegalArgumentException("Unsupported deviceInfoVersion: $version")
        }.toDeviceInfo()

        if (version < 3) onUpgradeVersion(deviceInfo)

        return deviceInfo
    }

    fun serialize(info: DeviceInfo, format: Json = this.format): String {
        return format.encodeToString(
            Wrapper.serializer(V3.serializer()),
            Wrapper(3, info.toCurrentInfo())
        )
    }

    fun toJsonElement(info: DeviceInfo, format: Json = this.format): JsonElement {
        return format.encodeToJsonElement(
            Wrapper.serializer(V3.serializer()),
            Wrapper(3, info.toCurrentInfo())
        )
    }
}