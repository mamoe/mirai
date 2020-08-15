/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.io.core.toByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import net.mamoe.mirai.utils.internal.md5
import java.io.File
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * 加载一个设备信息. 若文件不存在或为空则随机并创建一个设备信息保存.
 */
public fun File.loadAsDeviceInfo(json: Json, context: Context = ContextImpl()): DeviceInfo {
    if (!this.exists() || this.length() == 0L) {
        return SystemDeviceInfo(context).also {
            this.writeText(json.encodeToString(SystemDeviceInfo.serializer(), it))
        }
    }
    return json.decodeFromString(DeviceInfoData.serializer(), this.readText()).also {
        it.context = context
    }
}

@Serializable
public actual open class SystemDeviceInfo actual constructor() : DeviceInfo() {
    public actual constructor(context: Context) : this() {
        this.context = context
    }

    @Transient
    public final override lateinit var context: Context

    public override val display: ByteArray = "MIRAI.${getRandomString(6, '0'..'9')}.001".toByteArray()
    public override val product: ByteArray = "mirai".toByteArray()
    public override val device: ByteArray = "mirai".toByteArray()
    public override val board: ByteArray = "mirai".toByteArray()
    public override val brand: ByteArray = "mamoe".toByteArray()
    public override val model: ByteArray = "mirai".toByteArray()
    public override val bootloader: ByteArray = "unknown".toByteArray()
    public override val fingerprint: ByteArray =
        "mamoe/mirai/mirai:10/MIRAI.200122.001/${getRandomString(7, '0'..'9')}:user/release-keys".toByteArray()
    public override val bootId: ByteArray = ExternalImage.generateUUID(getRandomByteArray(16).md5()).toByteArray()
    public override val procVersion: ByteArray =
        "Linux version 3.0.31-${getRandomString(8)} (android-build@xxx.xxx.xxx.xxx.com)".toByteArray()
    public override val baseBand: ByteArray = byteArrayOf()
    public override val version: Version = Version
    public override val simInfo: ByteArray = "T-Mobile".toByteArray()
    public override val osType: ByteArray = "android".toByteArray()
    public override val macAddress: ByteArray = "02:00:00:00:00:00".toByteArray()
    public override val wifiBSSID: ByteArray? = "02:00:00:00:00:00".toByteArray()
    public override val wifiSSID: ByteArray? = "<unknown ssid>".toByteArray()
    public override val imsiMd5: ByteArray = getRandomByteArray(16).md5()
    public override val imei: String = getRandomString(15, '0'..'9')
    public override val androidId: ByteArray get() = display
    public override val apn: ByteArray = "wifi".toByteArray()

    @Serializable
    public actual object Version : DeviceInfo.Version {
        public override val incremental: ByteArray = "5891938".toByteArray()
        public override val release: ByteArray = "10".toByteArray()
        public override val codename: ByteArray = "REL".toByteArray()
        public override val sdk: Int = 29
    }
}

/**
 * 生成长度为 [length], 元素为随机 `0..255` 的 [ByteArray]
 */
@JvmSynthetic
internal fun getRandomByteArray(length: Int): ByteArray = ByteArray(length) { Random.nextInt(0..255).toByte() }

/**
 * 随机生成长度为 [length] 的 [String].
 */
@JvmSynthetic
internal fun getRandomString(length: Int): String =
    getRandomString(length, *defaultRanges)

private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')

/**
 * 根据所给 [charRange] 随机生成长度为 [length] 的 [String].
 */
@JvmSynthetic
internal fun getRandomString(length: Int, charRange: CharRange): String =
    String(CharArray(length) { charRange.random() })

/**
 * 根据所给 [charRanges] 随机生成长度为 [length] 的 [String].
 */
@JvmSynthetic
internal fun getRandomString(length: Int, vararg charRanges: CharRange): String =
    String(CharArray(length) { charRanges[Random.Default.nextInt(0..charRanges.lastIndex)].random() })
