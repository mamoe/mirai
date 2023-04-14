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
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import net.mamoe.mirai.utils.DeviceInfoManager.toCurrentInfo
import kotlin.contracts.contract

@Suppress("DuplicatedCode")
@Deprecated(
    message = "Dont use this serializer directly, it will be removed in future. " +
            "ABI compatibility is not guaranteed.",
    level = DeprecationLevel.WARNING
)
public object DeviceInfoDelegateSerializer : KSerializer<DeviceInfo> {

    private val v2OrV3Descriptor = buildClassSerialDescriptor("Wrapper") {
        element<String>("display")
        element<String>("product")
        element<String>("device")
        element<String>("board")
        element<String>("brand")
        element<String>("model")
        element<String>("bootloader")
        element<String>("fingerprint")
        element<String>("bootId")
        element<String>("procVersion")
        element<DeviceInfoManager.HexString>("baseBand")
        element<DeviceInfoManager.Version>("version")
        element<String>("simInfo")
        element<String>("osType")
        element<String>("macAddress")
        element<String>("wifiBSSID")
        element<String>("wifiSSID")
        element<DeviceInfoManager.HexString>("imsiMd5")
        element<String>("imei")
        element<String>("apn")
        element<String>("androidId", isOptional = true)
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("DeviceInfo") {
        element<Int>("deviceInfoVersion", isOptional = true)
        element("data", v2OrV3Descriptor, isOptional = true)
        element<ByteArray>("display", isOptional = true)
        element<ByteArray>("product", isOptional = true)
        element<ByteArray>("device", isOptional = true)
        element<ByteArray>("board", isOptional = true)
        element<ByteArray>("brand", isOptional = true)
        element<ByteArray>("model", isOptional = true)
        element<ByteArray>("bootloader", isOptional = true)
        element<ByteArray>("fingerprint", isOptional = true)
        element<ByteArray>("bootId", isOptional = true)
        element<ByteArray>("procVersion", isOptional = true)
        element<ByteArray>("baseBand", isOptional = true)
        element(
            "version",
            DeviceInfoManager.DeviceInfoVersionSerializer.SerialData.serializer().descriptor,
            isOptional = true
        )
        element<ByteArray>("simInfo", isOptional = true)
        element<ByteArray>("osType", isOptional = true)
        element<ByteArray>("macAddress", isOptional = true)
        element<ByteArray>("wifiBSSID", isOptional = true)
        element<ByteArray>("wifiSSID", isOptional = true)
        element<ByteArray>("imsiMd5", isOptional = true)
        element<String>("imei", isOptional = true)
        element<ByteArray>("apn", isOptional = true)
    }

    override fun deserialize(decoder: Decoder): DeviceInfo {
        return decoder.decodeStructure(descriptor) {
            var v2OrV3DeviceInfo: DeviceInfo? = null

            var displayV1: ByteArray? = null
            var productV1: ByteArray? = null
            var deviceV1: ByteArray? = null
            var boardV1: ByteArray? = null
            var brandV1: ByteArray? = null
            var modelV1: ByteArray? = null
            var bootloaderV1: ByteArray? = null
            var fingerprintV1: ByteArray? = null
            var bootIdV1: ByteArray? = null
            var procVersionV1: ByteArray? = null
            var baseBandV1: ByteArray? = null
            var versionV1: DeviceInfoManager.DeviceInfoVersionSerializer.SerialData? = null
            var simInfoV1: ByteArray? = null
            var osTypeV1: ByteArray? = null
            var macAddressV1: ByteArray? = null
            var wifiBSSIDV1: ByteArray? = null
            var wifiSSIDV1: ByteArray? = null
            var imsiMd5V1: ByteArray? = null
            var imeiV1: String? = null
            var apnV1: ByteArray? = null

            while (true) {
                val index = decodeElementIndex(descriptor)
                when (index) {
                    0 -> decodeIntElement(descriptor, index) // version
                    1 -> { // version 2 or 3
                        decoder.decodeStructure(v2OrV3Descriptor) {
                            var display: String? = null
                            var product: String? = null
                            var device: String? = null
                            var board: String? = null
                            var brand: String? = null
                            var model: String? = null
                            var bootloader: String? = null
                            var fingerprint: String? = null
                            var bootId: String? = null
                            var procVersion: String? = null
                            var baseBand: DeviceInfoManager.HexString? = null
                            var version: DeviceInfoManager.Version? = null
                            var simInfo: String? = null
                            var osType: String? = null
                            var macAddress: String? = null
                            var wifiBSSID: String? = null
                            var wifiSSID: String? = null
                            var imsiMd5: DeviceInfoManager.HexString? = null
                            var imei: String? = null
                            var apn: String? = null
                            var androidId: String? = null

                            while (true) {
                                when (val innerIndex = decodeElementIndex(v2OrV3Descriptor)) {
                                    0 -> display = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    1 -> product = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    2 -> device = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    3 -> board = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    4 -> brand = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    5 -> model = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    6 -> bootloader = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    7 -> fingerprint = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    8 -> bootId = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    9 -> procVersion = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    10 -> baseBand = decodeSerializableElement(
                                        v2OrV3Descriptor,
                                        innerIndex,
                                        DeviceInfoManager.HexStringSerializer
                                    )

                                    11 -> version = decodeSerializableElement(
                                        v2OrV3Descriptor,
                                        innerIndex,
                                        DeviceInfoManager.Version.serializer()
                                    )

                                    12 -> simInfo = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    13 -> osType = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    14 -> macAddress = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    15 -> wifiBSSID = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    16 -> wifiSSID = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    17 -> imsiMd5 = decodeSerializableElement(
                                        v2OrV3Descriptor,
                                        innerIndex,
                                        DeviceInfoManager.HexStringSerializer
                                    )

                                    18 -> imei = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    19 -> apn = decodeStringElement(v2OrV3Descriptor, innerIndex)
                                    20 -> androidId = decodeStringElement(v2OrV3Descriptor, innerIndex)

                                    CompositeDecoder.DECODE_DONE -> break
                                }
                            }

                            display = assertNotNullOrDecodeFailed(display)
                            product = assertNotNullOrDecodeFailed(product)
                            device = assertNotNullOrDecodeFailed(device)
                            board = assertNotNullOrDecodeFailed(board)
                            brand = assertNotNullOrDecodeFailed(brand)
                            model = assertNotNullOrDecodeFailed(model)
                            bootloader = assertNotNullOrDecodeFailed(bootloader)
                            fingerprint = assertNotNullOrDecodeFailed(fingerprint)
                            bootId = assertNotNullOrDecodeFailed(bootId)
                            procVersion = assertNotNullOrDecodeFailed(procVersion)
                            baseBand = assertNotNullOrDecodeFailed(baseBand)
                            version = assertNotNullOrDecodeFailed(version)
                            simInfo = assertNotNullOrDecodeFailed(simInfo)
                            osType = assertNotNullOrDecodeFailed(osType)
                            macAddress = assertNotNullOrDecodeFailed(macAddress)
                            wifiBSSID = assertNotNullOrDecodeFailed(wifiBSSID)
                            wifiSSID = assertNotNullOrDecodeFailed(wifiSSID)
                            imsiMd5 = assertNotNullOrDecodeFailed(imsiMd5)
                            imei = assertNotNullOrDecodeFailed(imei)
                            apn = assertNotNullOrDecodeFailed(apn)

                            v2OrV3DeviceInfo = if (androidId == null) {
                                DeviceInfoManager.V2(
                                    display, product, device, board, brand, model, bootloader,
                                    fingerprint, bootId, procVersion, baseBand, version, simInfo,
                                    osType, macAddress, wifiBSSID, wifiSSID, imsiMd5, imei, apn
                                ).toDeviceInfo()
                            } else {
                                DeviceInfoManager.V3(
                                    display, product, device, board, brand, model, bootloader,
                                    fingerprint, bootId, procVersion, baseBand, version, simInfo,
                                    osType, macAddress, wifiBSSID, wifiSSID, imsiMd5, imei, apn, androidId
                                ).toDeviceInfo()
                            }
                        }
                        break
                    }

                    2 -> displayV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    3 -> productV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    4 -> deviceV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    5 -> boardV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    6 -> brandV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    7 -> modelV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    8 -> bootloaderV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    9 -> fingerprintV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    10 -> bootIdV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    11 -> procVersionV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    12 -> baseBandV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    13 -> versionV1 = decodeSerializableElement(
                        descriptor,
                        index,
                        DeviceInfoManager.DeviceInfoVersionSerializer.SerialData.serializer()
                    )

                    14 -> simInfoV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    15 -> osTypeV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    16 -> macAddressV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    17 -> wifiBSSIDV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    18 -> wifiSSIDV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    19 -> imsiMd5V1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                    20 -> imeiV1 = decodeStringElement(descriptor, index)
                    21 -> apnV1 = decodeSerializableElement(descriptor, index, ByteArraySerializer())
                }
                if (index == CompositeDecoder.DECODE_DONE) break
            }

            val r = v2OrV3DeviceInfo
            if (r == null) {
                displayV1 = assertNotNullOrDecodeFailed(displayV1)
                productV1 = assertNotNullOrDecodeFailed(productV1)
                deviceV1 = assertNotNullOrDecodeFailed(deviceV1)
                boardV1 = assertNotNullOrDecodeFailed(boardV1)
                brandV1 = assertNotNullOrDecodeFailed(brandV1)
                modelV1 = assertNotNullOrDecodeFailed(modelV1)
                bootloaderV1 = assertNotNullOrDecodeFailed(bootloaderV1)
                fingerprintV1 = assertNotNullOrDecodeFailed(fingerprintV1)
                bootIdV1 = assertNotNullOrDecodeFailed(bootIdV1)
                procVersionV1 = assertNotNullOrDecodeFailed(procVersionV1)
                baseBandV1 = assertNotNullOrDecodeFailed(baseBandV1)
                versionV1 = assertNotNullOrDecodeFailed(versionV1)
                simInfoV1 = assertNotNullOrDecodeFailed(simInfoV1)
                osTypeV1 = assertNotNullOrDecodeFailed(osTypeV1)
                macAddressV1 = assertNotNullOrDecodeFailed(macAddressV1)
                wifiBSSIDV1 = assertNotNullOrDecodeFailed(wifiBSSIDV1)
                wifiSSIDV1 = assertNotNullOrDecodeFailed(wifiSSIDV1)
                imsiMd5V1 = assertNotNullOrDecodeFailed(imsiMd5V1)
                imeiV1 = assertNotNullOrDecodeFailed(imeiV1)
                apnV1 = assertNotNullOrDecodeFailed(apnV1)

                DeviceInfoManager.V1(
                    displayV1, productV1, deviceV1, boardV1, brandV1, modelV1, bootloaderV1,
                    fingerprintV1, bootIdV1, procVersionV1, baseBandV1,
                    versionV1.run { DeviceInfo.Version(incremental, release, codename, sdk) },
                    simInfoV1, osTypeV1, macAddressV1, wifiBSSIDV1, wifiSSIDV1, imsiMd5V1, imeiV1, apnV1
                ).toDeviceInfo()
            } else {
                r
            }
        }

    }

    override fun serialize(encoder: Encoder, value: DeviceInfo) {
        DeviceInfoManager.V3.serializer().serialize(encoder, value.toCurrentInfo())
    }

    private fun <T : Any> assertNotNullOrDecodeFailed(value: T?): T {
        contract {
            returns() implies (value != null)
        }
        if (value == null) error("Failed to deserialize DeviceInfo.")
        return value
    }

}