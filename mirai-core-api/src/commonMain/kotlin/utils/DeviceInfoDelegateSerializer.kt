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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlin.contracts.contract

@Suppress("DuplicatedCode")
@Deprecated(
    message = "Dont use this serializer directly, it will be removed in future. " +
            "ABI compatibility is not guaranteed.",
    level = DeprecationLevel.WARNING
)
public object DeviceInfoDelegateSerializer : KSerializer<DeviceInfo> {


    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(DeviceInfo::class.simpleName ?: "DeviceInfo") {
            element<ByteArray>("display")
            element<ByteArray>("product")
            element<ByteArray>("device")
            element<ByteArray>("board")
            element<ByteArray>("brand")
            element<ByteArray>("model")
            element<ByteArray>("bootloader")
            element<ByteArray>("fingerprint")
            element<ByteArray>("bootId")
            element<ByteArray>("procVersion")
            element<ByteArray>("baseBand")
            element("version", DeviceInfo.Version.serializer().descriptor)
            element<ByteArray>("simInfo")
            element<ByteArray>("osType")
            element<ByteArray>("macAddress")
            element<ByteArray>("wifiBSSID")
            element<ByteArray>("wifiSSID")
            element<ByteArray>("imsiMd5")
            element<String>("imei")
            element<ByteArray>("apn")
            element<ByteArray>("androidId", isOptional = true)
        }

    override fun deserialize(decoder: Decoder): DeviceInfo {
        return decoder.decodeStructure(descriptor) {
            var display: ByteArray? = null
            var product: ByteArray? = null
            var device: ByteArray? = null
            var board: ByteArray? = null
            var brand: ByteArray? = null
            var model: ByteArray? = null
            var bootloader: ByteArray? = null
            var fingerprint: ByteArray? = null
            var bootId: ByteArray? = null
            var procVersion: ByteArray? = null
            var baseBand: ByteArray? = null
            var version: DeviceInfo.Version? = null
            var simInfo: ByteArray? = null
            var osType: ByteArray? = null
            var macAddress: ByteArray? = null
            var wifiBSSID: ByteArray? = null
            var wifiSSID: ByteArray? = null
            var imsiMd5: ByteArray? = null
            var imei: String? = null
            var apn: ByteArray? = null
            var androidId: ByteArray? = null

            while (true) {
                val index = decodeElementIndex(descriptor)
                when (index) {
                    0 -> display = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    1 -> product = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    2 -> device = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    3 -> board = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    4 -> brand = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    5 -> model = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    6 -> bootloader = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    7 -> fingerprint = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    8 -> bootId = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    9 -> procVersion = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    10 -> baseBand = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    11 -> version = decodeSerializableElement(descriptor, index, DeviceInfo.Version.serializer())
                    12 -> simInfo = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    13 -> osType = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    14 -> macAddress = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    15 -> wifiBSSID = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    16 -> wifiSSID = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    17 -> imsiMd5 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    18 -> imei = decodeStringElement(descriptor, index)
                    19 -> apn = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    20 -> androidId = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                }
                if (index == CompositeDecoder.DECODE_DONE) break
            }
            display = assertNotNullOrDecodeFailed(display, "display")
            product = assertNotNullOrDecodeFailed(product, "product")
            device = assertNotNullOrDecodeFailed(device, "device")
            board = assertNotNullOrDecodeFailed(board, "board")
            brand = assertNotNullOrDecodeFailed(brand, "brand")
            model = assertNotNullOrDecodeFailed(model, "model")
            bootloader = assertNotNullOrDecodeFailed(bootloader, "bootloader")
            fingerprint = assertNotNullOrDecodeFailed(fingerprint, "fingerprint")
            bootId = assertNotNullOrDecodeFailed(bootId, "bootId")
            procVersion = assertNotNullOrDecodeFailed(procVersion, "procVersion")
            baseBand = assertNotNullOrDecodeFailed(baseBand, "baseBand")
            version = assertNotNullOrDecodeFailed(version, "version")
            simInfo = assertNotNullOrDecodeFailed(simInfo, "simInfo")
            osType = assertNotNullOrDecodeFailed(osType, "osType")
            macAddress = assertNotNullOrDecodeFailed(macAddress, "macAddress")
            wifiBSSID = assertNotNullOrDecodeFailed(wifiBSSID, "wifiBSSID")
            wifiSSID = assertNotNullOrDecodeFailed(wifiSSID, "wifiSSID")
            imsiMd5 = assertNotNullOrDecodeFailed(imsiMd5, "imsiMd5")
            imei = assertNotNullOrDecodeFailed(imei, "imei")
            apn = assertNotNullOrDecodeFailed(apn, "apn")

            return@decodeStructure DeviceInfo(
                display, product, device, board, brand, model, bootloader,
                fingerprint, bootId, procVersion, baseBand, version, simInfo,
                osType, macAddress, wifiBSSID, wifiSSID, imsiMd5, imei, apn,
                androidId ?: getRandomByteArray(8).toUHexString("").lowercase().encodeToByteArray()
            )
        }

    }

    override fun serialize(encoder: Encoder, value: DeviceInfo) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, ByteArraySerializer(), value.display)
            encodeSerializableElement(descriptor, 1, ByteArraySerializer(), value.product)
            encodeSerializableElement(descriptor, 2, ByteArraySerializer(), value.device)
            encodeSerializableElement(descriptor, 3, ByteArraySerializer(), value.board)
            encodeSerializableElement(descriptor, 4, ByteArraySerializer(), value.brand)
            encodeSerializableElement(descriptor, 5, ByteArraySerializer(), value.model)
            encodeSerializableElement(descriptor, 6, ByteArraySerializer(), value.bootloader)
            encodeSerializableElement(descriptor, 7, ByteArraySerializer(), value.fingerprint)
            encodeSerializableElement(descriptor, 8, ByteArraySerializer(), value.bootId)
            encodeSerializableElement(descriptor, 9, ByteArraySerializer(), value.procVersion)
            encodeSerializableElement(descriptor, 10, ByteArraySerializer(), value.baseBand)
            encodeSerializableElement(descriptor, 11, DeviceInfo.Version.serializer(), value.version)
            encodeSerializableElement(descriptor, 12, ByteArraySerializer(), value.simInfo)
            encodeSerializableElement(descriptor, 13, ByteArraySerializer(), value.osType)
            encodeSerializableElement(descriptor, 14, ByteArraySerializer(), value.macAddress)
            encodeSerializableElement(descriptor, 15, ByteArraySerializer(), value.wifiBSSID)
            encodeSerializableElement(descriptor, 16, ByteArraySerializer(), value.wifiSSID)
            encodeSerializableElement(descriptor, 17, ByteArraySerializer(), value.imsiMd5)
            encodeSerializableElement(descriptor, 18, String.serializer(), value.imei)
            encodeSerializableElement(descriptor, 19, ByteArraySerializer(), value.apn)
            encodeSerializableElement(descriptor, 20, ByteArraySerializer(), value.androidId)
        }
    }

    private fun <T : Any> assertNotNullOrDecodeFailed(value: T?, fieldName: String): T {
        contract {
            returns() implies (value != null)
        }
        return value ?: throw SerializationException("Failed to deserialize DeviceInfo: missing field \"$fieldName\".")
    }

}