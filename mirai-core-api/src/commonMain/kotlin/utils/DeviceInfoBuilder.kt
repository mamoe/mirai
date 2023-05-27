/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.utils.DeviceInfoBuilder.Companion.fromPrototype
import net.mamoe.mirai.utils.DeviceInfoBuilder.Companion.fromRandom
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.random.Random

/**
 * [DeviceInfo] 的构建器.
 *
 * 通过 [fromPrototype] 或 [fromRandom] 可以构造一个构建器, 调用其各属性方法后使用 [build] 即可构建实例.
 *
 * @see build
 *
 * @since 2.15
 */
public class DeviceInfoBuilder internal constructor(
    private val prototype: DeviceInfo = DeviceInfo.random()
) {
    private var display: ByteArray? = null
    public fun display(value: ByteArray): DeviceInfoBuilder = apply {
        this.display = value
    }

    public fun display(value: String): DeviceInfoBuilder = apply {
        this.display = value.encodeToByteArray()
    }

    private var product: ByteArray? = prototype.product
    public fun product(value: ByteArray): DeviceInfoBuilder = apply {
        this.product = value
    }

    public fun product(value: String): DeviceInfoBuilder = apply {
        this.product = value.encodeToByteArray()
    }

    private var device: ByteArray? = null
    public fun device(value: ByteArray): DeviceInfoBuilder = apply {
        this.device = value
    }

    public fun device(value: String): DeviceInfoBuilder = apply {
        this.device = value.encodeToByteArray()
    }

    private var board: ByteArray? = null
    public fun board(value: ByteArray): DeviceInfoBuilder = apply {
        this.board = value
    }

    public fun board(value: String): DeviceInfoBuilder = apply {
        this.board = value.encodeToByteArray()
    }

    private var brand: ByteArray? = null
    public fun brand(value: ByteArray): DeviceInfoBuilder = apply {
        this.brand = value
    }

    public fun brand(value: String): DeviceInfoBuilder = apply {
        this.brand = value.encodeToByteArray()
    }

    private var model: ByteArray? = null
    public fun model(value: ByteArray): DeviceInfoBuilder = apply {
        this.model = value
    }

    public fun model(value: String): DeviceInfoBuilder = apply {
        this.model = value.encodeToByteArray()
    }

    private var bootloader: ByteArray? = null
    public fun bootloader(value: ByteArray): DeviceInfoBuilder = apply {
        this.bootloader = value
    }

    public fun bootloader(value: String): DeviceInfoBuilder = apply {
        this.bootloader = value.encodeToByteArray()
    }

    private var fingerprint: ByteArray? = null
    public fun fingerprint(value: ByteArray): DeviceInfoBuilder = apply {
        this.fingerprint = value
    }

    public fun fingerprint(value: String): DeviceInfoBuilder = apply {
        this.fingerprint = value.encodeToByteArray()
    }

    private var bootId: ByteArray? = null
    public fun bootId(value: ByteArray): DeviceInfoBuilder = apply {
        this.bootId = value
    }

    public fun bootId(value: String): DeviceInfoBuilder = apply {
        this.bootId = value.encodeToByteArray()
    }

    private var procVersion: ByteArray? = null
    public fun procVersion(value: ByteArray): DeviceInfoBuilder = apply {
        this.procVersion = value
    }

    public fun procVersion(value: String): DeviceInfoBuilder = apply {
        this.procVersion = value.encodeToByteArray()
    }

    private var baseBand: ByteArray? = null
    public fun baseBand(value: ByteArray): DeviceInfoBuilder = apply {
        this.baseBand = value
    }

    public fun baseBand(value: String): DeviceInfoBuilder = apply {
        this.baseBand = value.encodeToByteArray()
    }

    private var version: DeviceInfo.Version? = null
    public fun version(value: DeviceInfo.Version): DeviceInfoBuilder = apply {
        this.version = value
    }

    private var simInfo: ByteArray? = null
    public fun simInfo(value: ByteArray): DeviceInfoBuilder = apply {
        this.simInfo = value
    }

    public fun simInfo(value: String): DeviceInfoBuilder = apply {
        this.simInfo = value.encodeToByteArray()
    }

    private var osType: ByteArray? = null
    public fun osType(value: ByteArray): DeviceInfoBuilder = apply {
        this.osType = value
    }

    public fun osType(value: String): DeviceInfoBuilder = apply {
        this.osType = value.encodeToByteArray()
    }

    private var macAddress: ByteArray? = null
    public fun macAddress(value: ByteArray): DeviceInfoBuilder = apply {
        this.macAddress = value
    }

    public fun macAddress(value: String): DeviceInfoBuilder = apply {
        this.macAddress = value.encodeToByteArray()
    }

    private var wifiBSSID: ByteArray? = null
    public fun wifiBSSID(value: ByteArray): DeviceInfoBuilder = apply {
        this.wifiBSSID = value
    }

    public fun wifiBSSID(value: String): DeviceInfoBuilder = apply {
        this.wifiBSSID = value.encodeToByteArray()
    }

    private var wifiSSID: ByteArray? = null
    public fun wifiSSID(value: ByteArray): DeviceInfoBuilder = apply {
        this.wifiSSID = value
    }

    public fun wifiSSID(value: String): DeviceInfoBuilder = apply {
        this.wifiSSID = value.encodeToByteArray()
    }

    private var imsiMd5: ByteArray? = null
    public fun imsiMd5(value: ByteArray): DeviceInfoBuilder = apply {
        this.imsiMd5 = value
    }

    public fun imsiMd5(value: String): DeviceInfoBuilder = apply {
        this.imsiMd5 = value.encodeToByteArray()
    }

    private var imei: String? = null
    public fun imei(value: String): DeviceInfoBuilder = apply {
        this.imei = value
    }


    private var apn: ByteArray? = null
    public fun apn(value: ByteArray): DeviceInfoBuilder = apply {
        this.apn = value
    }

    public fun apn(value: String): DeviceInfoBuilder = apply {
        this.apn = value.encodeToByteArray()
    }

    private var androidId: ByteArray? = null
    public fun androidId(value: ByteArray): DeviceInfoBuilder = apply {
        this.androidId = value
    }

    public fun androidId(value: String): DeviceInfoBuilder = apply {
        this.androidId = value.encodeToByteArray()
    }


    public fun build(): DeviceInfo {
        @Suppress("DEPRECATION")
        return DeviceInfo(
            display = display ?: prototype.display.copyOf(),
            product = product ?: prototype.product.copyOf(),
            device = device ?: prototype.device.copyOf(),
            board = board ?: prototype.board.copyOf(),
            brand = brand ?: prototype.brand.copyOf(),
            model = model ?: prototype.model.copyOf(),
            bootloader = bootloader ?: prototype.bootloader.copyOf(),
            fingerprint = fingerprint ?: prototype.fingerprint.copyOf(),
            bootId = bootId ?: prototype.bootId.copyOf(),
            procVersion = procVersion ?: prototype.procVersion.copyOf(),
            baseBand = baseBand ?: prototype.baseBand.copyOf(),
            version = version ?: prototype.version,
            simInfo = simInfo ?: prototype.simInfo.copyOf(),
            osType = osType ?: prototype.osType.copyOf(),
            macAddress = macAddress ?: prototype.macAddress.copyOf(),
            wifiBSSID = wifiBSSID ?: prototype.wifiBSSID.copyOf(),
            wifiSSID = wifiSSID ?: prototype.wifiSSID.copyOf(),
            imsiMd5 = imsiMd5 ?: prototype.imsiMd5.copyOf(),
            imei = imei ?: prototype.imei,
            apn = apn ?: prototype.apn.copyOf(),
            androidId = androidId ?: prototype.androidId.copyOf(),
        )
    }

    public companion object {
        /**
         * 构造一个以随机属性填充的 [DeviceInfoBuilder].
         */
        @JvmStatic
        @JvmOverloads
        public fun fromRandom(random: Random = Random.Default): DeviceInfoBuilder =
            DeviceInfoBuilder(DeviceInfo.random(random))

        /**
         * 构造一个复制 [prototype] 属性的 [DeviceInfoBuilder].
         */
        @JvmStatic
        public fun fromPrototype(prototype: DeviceInfo): DeviceInfoBuilder = DeviceInfoBuilder(prototype)
    }
}
