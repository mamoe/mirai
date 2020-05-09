/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.qqandroid.network.protocol.packet

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.toByteArray
import kotlinx.io.core.writeFully
import net.mamoe.mirai.qqandroid.network.protocol.LoginType
import net.mamoe.mirai.qqandroid.utils.MiraiPlatformUtils
import net.mamoe.mirai.qqandroid.utils.NetworkType
import net.mamoe.mirai.qqandroid.utils.io.*
import net.mamoe.mirai.qqandroid.utils.toByteArray
import net.mamoe.mirai.utils.currentTimeMillis
import kotlin.random.Random

/**
 * 显式表示一个 [ByteArray] 是一个 tlv 的 body
 */
internal inline class Tlv(val value: ByteArray)

internal fun BytePacketBuilder.t1(uin: Long, ip: ByteArray) {
    require(ip.size == 4) { "ip.size must == 4" }
    writeShort(0x1)
    writeShortLVPacket {
        writeShort(1) // _ip_ver
        writeInt(Random.nextInt())
        writeInt(uin.toInt())
        writeInt(currentTimeMillis.toInt())
        writeFully(ip)
        writeShort(0)
    } shouldEqualsTo 20
}

internal fun BytePacketBuilder.t2(captchaCode: String, captchaToken: ByteArray, sigVer: Short = 0) {
    writeShort(0x2)
    writeShortLVPacket {
        writeShort(sigVer)
        writeShortLVString(captchaCode)
        writeShortLVByteArray(captchaToken)
    }
}

internal fun BytePacketBuilder.t8(
    localId: Int = 2052
) {
    writeShort(0x8)
    writeShortLVPacket {
        writeShort(0)
        writeInt(localId) // localId
        writeShort(0)
    }
}

internal fun BytePacketBuilder.t18(
    appId: Long,
    appClientVersion: Int = 0,
    uin: Long,
    constant1_always_0: Int = 0
) {
    writeShort(0x18)
    writeShortLVPacket {
        writeShort(1) //_ping_version
        writeInt(1536) //_sso_version
        writeInt(appId.toInt())
        writeInt(appClientVersion)
        writeInt(uin.toInt())
        writeShort(constant1_always_0.toShort())
        writeShort(0)
    } shouldEqualsTo 22
}


internal fun BytePacketBuilder.t106(
    appId: Long = 16L,
    subAppId: Long,
    appClientVersion: Int = 0,
    uin: Long,
    n5_always_1: Int = 1,
    passwordMd5: ByteArray,
    salt: Long,
    uinAccountString: ByteArray,
    tgtgtKey: ByteArray,
    isGuidAvailable: Boolean = true,
    guid: ByteArray?,
    loginType: LoginType
) {
    writeShort(0x106)
    passwordMd5.requireSize(16)
    tgtgtKey.requireSize(16)
    guid?.requireSize(16)

    writeShortLVPacket {
        encryptAndWrite(MiraiPlatformUtils.md5(passwordMd5 + ByteArray(4) + (salt.takeIf { it != 0L } ?: uin).toInt().toByteArray())) {
            writeShort(4)//TGTGTVer
            writeInt(Random.nextInt())
            writeInt(5)//ssoVer
            writeInt(appId.toInt())
            writeInt(appClientVersion)

            if (uin == 0L) {
                writeLong(salt)
            } else {
                writeLong(uin)
            }

            writeInt(currentTimeMillis.toInt())
            writeFully(ByteArray(4)) // ip // no need to write actual ip
            writeByte(n5_always_1.toByte())
            writeFully(passwordMd5)
            writeFully(tgtgtKey)
            writeInt(0)
            writeByte(isGuidAvailable.toByte())
            if (isGuidAvailable) {
                require(guid != null) { "Guid must not be null when isGuidAvailable==true" }
            }
            if (guid == null) {
                repeat(4) {
                    writeInt(Random.nextInt())
                }
            } else {
                writeFully(guid)
            }
            writeInt(subAppId.toInt())
            writeInt(loginType.value)
            writeShortLVByteArray(uinAccountString)
            writeShort(0)
        }
    }
}

internal fun BytePacketBuilder.t116(
    miscBitmap: Int,
    subSigMap: Int,
    appIdList: LongArray = longArrayOf(1600000226L)
) {
    writeShort(0x116)
    writeShortLVPacket {
        writeByte(0) // _ver
        writeInt(miscBitmap) // 184024956
        writeInt(subSigMap) // 66560
        writeByte(appIdList.size.toByte())
        appIdList.forEach {
            writeInt(it.toInt())
        }
    }
}


internal fun BytePacketBuilder.t100(
    appId: Long = 16,
    subAppId: Long,
    appClientVersion: Int
) {
    writeShort(0x100)
    writeShortLVPacket {
        writeShort(1)//db_buf_ver
        writeInt(5)//sso_ver
        writeInt(appId.toInt())
        writeInt(subAppId.toInt())
        writeInt(appClientVersion)
        writeInt(34869472) // sigMap, 34869472?
    } shouldEqualsTo 22
}

internal fun BytePacketBuilder.t107(
    picType: Int,
    const1_always_0: Int = 0,
    const2_always_0: Int = 0,
    const3_always_1: Int = 1
) {
    writeShort(0x107)
    writeShortLVPacket {
        writeShort(picType.toShort())
        writeByte(const1_always_0.toByte())
        writeShort(const2_always_0.toShort())
        writeByte(const3_always_1.toByte())
    } shouldEqualsTo 6
}

internal fun BytePacketBuilder.t108(
    ksid: ByteArray
) {
    require(ksid.size == 16) { "ksid should length 16" }
    writeShort(0x108)
    writeShortLVPacket {
        writeFully(ksid)
    }
}

internal fun BytePacketBuilder.t104(
    t104Data: ByteArray
) {
    writeShort(0x104)
    writeShortLVPacket {
        writeFully(t104Data)
    }
}

internal fun BytePacketBuilder.t174(
    t174Data: ByteArray
) {
    writeShort(0x174)
    writeShortLVPacket {
        writeFully(t174Data)
    }
}


internal fun BytePacketBuilder.t17a(
    value: Int = 0
) {
    writeShort(0x17a)
    writeShortLVPacket {
        writeInt(value)
    }
}

internal fun BytePacketBuilder.t197(
    value: ByteArray
) {
    writeShort(0x197)
    writeShortLVPacket {
        writeFully(value)
    }
}

internal fun BytePacketBuilder.t19e(
    value: Int = 0
) {
    writeShort(0x19e)
    writeShortLVPacket {
        writeShort(1)
        writeByte(value.toByte())
    }
}

internal fun BytePacketBuilder.t17c(
    t17cData: ByteArray
) {
    writeShort(0x17c)
    writeShortLVPacket {
        writeShort(t17cData.size.toShort())
        writeFully(t17cData)
    }
}

internal fun BytePacketBuilder.t401(
    t401Data: ByteArray
) {
    writeShort(0x401)
    writeShortLVPacket {
        writeFully(t401Data)
    }
}

/**
 * @param apkId application.getPackageName().getBytes()
 */
internal fun BytePacketBuilder.t142(
    apkId: ByteArray
) {
    writeShort(0x142)
    writeShortLVPacket {
        writeShort(0) //_version
        writeShortLVByteArrayLimitedLength(apkId, 32)
    }
}

internal fun BytePacketBuilder.t112(
    nonNumberUin: ByteArray
) {
    writeShort(0x112)
    writeShortLVPacket {
        writeFully(nonNumberUin)
    }
}

internal fun BytePacketBuilder.t144(
    // t109
    androidId: ByteArray,

    // t52d
    androidDevInfo: ByteArray,

    // t124
    osType: ByteArray = "android".toByteArray(),
    osVersion: ByteArray,
    networkType: NetworkType,
    simInfo: ByteArray,
    unknown: ByteArray,
    apn: ByteArray = "wifi".toByteArray(),

    // t128
    isGuidFromFileNull: Boolean = false,
    isGuidAvailable: Boolean = true,
    isGuidChanged: Boolean = false,
    guidFlag: Long,
    buildModel: ByteArray,
    guid: ByteArray,
    buildBrand: ByteArray,

    // encrypt
    tgtgtKey: ByteArray
) {
    writeShort(0x144)
    writeShortLVPacket {
        encryptAndWrite(tgtgtKey) {
            writeShort(5) // tlv count
            t109(androidId)
            t52d(androidDevInfo)
            t124(osType, osVersion, networkType, simInfo, unknown, apn)
            t128(isGuidFromFileNull, isGuidAvailable, isGuidChanged, guidFlag, buildModel, guid, buildBrand)
            t16e(buildModel)
        }
    }
}


internal fun BytePacketBuilder.t109(
    androidId: ByteArray
) {
    writeShort(0x109)
    writeShortLVPacket {
        writeFully(MiraiPlatformUtils.md5(androidId))
    } shouldEqualsTo 16
}

internal fun BytePacketBuilder.t52d(
    androidDevInfo: ByteArray // oicq.wlogin_sdk.tools.util#get_android_dev_info
) {
    writeShort(0x52d)
    writeShortLVPacket {
        writeFully(androidDevInfo)

        // 0A  07  75  6E  6B  6E  6F  77  6E  12  7E  4C  69  6E  75  78  20  76  65  72  73  69  6F  6E  20  34  2E  39  2E  33  31  20  28  62  75  69  6C  64  40  42  75  69  6C  64  32  29  20  28  67  63  63  20  76  65  72  73  69  6F  6E  20  34  2E  39  20  32  30  31  35  30  31  32  33  20  28  70  72  65  72  65  6C  65  61  73  65  29  20  28  47  43  43  29  20  29  20  23  31  20  53  4D  50  20  50  52  45  45  4D  50  54  20  54  68  75  20  44  65  63  20  31  32  20  31  35  3A  33  30  3A  35  35  20  49  53  54  20  32  30  31  39  1A  03  52  45  4C  22  03  33  32  37  2A  41  4F  6E  65  50  6C  75  73  2F  4F  6E  65  50  6C  75  73  35  2F  4F  6E  65  50  6C  75  73  35  3A  37  2E  31  2E  31  2F  4E  4D  46  32  36  58  2F  31  30  31  37  31  36  31  37  3A  75  73  65  72  2F  72  65  6C  65  61  73  65  2D  6B  65  79  73  32  24  36  63  39  39  37  36  33  66  2D  66  62  34  32  2D  34  38  38  31  2D  62  37  32  65  2D  63  37  61  61  38  61  36  63  31  63  61  34  3A  10  65  38  63  37  30  35  34  64  30  32  66  33  36  33  64  30  42  0A  6E  6F  20  6D  65  73  73  61  67  65  4A  03  33  32  37

    }
}

internal fun BytePacketBuilder.t124(
    osType: ByteArray = "android".toByteArray(),
    osVersion: ByteArray, // Build.VERSION.RELEASE.toByteArray()
    networkType: NetworkType,  //oicq.wlogin_sdk.tools.util#get_network_type
    simInfo: ByteArray, // oicq.wlogin_sdk.tools.util#get_sim_operator_name
    unknown: ByteArray,
    apn: ByteArray = "wifi".toByteArray() // oicq.wlogin_sdk.tools.util#get_apn_string
) {
    writeShort(0x124)
    writeShortLVPacket {
        writeShortLVByteArrayLimitedLength(osType, 16)
        writeShortLVByteArrayLimitedLength(osVersion, 16)
        writeShort(networkType.value.toShort())
        writeShortLVByteArrayLimitedLength(simInfo, 16)
        writeShortLVByteArrayLimitedLength(unknown, 32)
        writeShortLVByteArrayLimitedLength(apn, 16)
    }
}

internal fun BytePacketBuilder.t128(
    isGuidFromFileNull: Boolean = false, // 保存到文件的 GUID 是否为 null
    isGuidAvailable: Boolean = true, // GUID 是否可用(计算/读取成功)
    isGuidChanged: Boolean = false, // GUID 是否有变动
    /**
     * guidFlag:
     * ```java
     * GUID_FLAG = 0;
     * GUID_FLAG |= GUID_SRC << 24 & 0xFF000000;
     * GUID_FLAG |= FLAG_MAC_ANDROIDID_GUID_CHANGE << 8 & 0xFF00;
     * ```
     *
     *
     * GUID_SRC:
     * 0: 初始值;
     * 1: 以前保存的文件;
     * 20: 以前没保存且现在生成失败;
     * 17: 以前没保存但现在生成成功;
     *
     *
     * FLAG_MAC_ANDROIDID_GUID_CHANGE:
     * ```java
     * if (!Arrays.equals(currentMac, get_last_mac)) {
     *     oicq.wlogin_sdk.request.t.FLAG_MAC_ANDROIDID_GUID_CHANGEMENT |= 0x1;
     * }
     * if (!Arrays.equals(currentAndroidId, get_last_android_id)) {
     *     oicq.wlogin_sdk.request.t.FLAG_MAC_ANDROIDID_GUID_CHANGEMENT |= 0x2;
     * }
     * if (!Arrays.equals(currentGuid, get_last_guid)) {
     *     oicq.wlogin_sdk.request.t.FLAG_MAC_ANDROIDID_GUID_CHANGEMENT |= 0x4;
     * }
     * ```
     */
    guidFlag: Long,
    buildModel: ByteArray,  // android.os.Build.MODEL
    /**
     * defaults `"%4;7t>;28<fc.5*6".toByteArray()`
     */
    guid: ByteArray,
    buildBrand: ByteArray // android.os.Build.BRAND
) {
    writeShort(0x128)
    writeShortLVPacket {
        writeShort(0)
        writeByte(isGuidFromFileNull.toByte())
        writeByte(isGuidAvailable.toByte())
        writeByte(isGuidChanged.toByte())
        writeInt(guidFlag.toInt()) // 11 00 00 00
        writeShortLVByteArrayLimitedLength(buildModel, 32)
        writeShortLVByteArrayLimitedLength(guid, 16)
        writeShortLVByteArrayLimitedLength(buildBrand, 16)
    }
}

internal fun BytePacketBuilder.t16e(
    buildModel: ByteArray
) {
    writeShort(0x16e)
    writeShortLVPacket {
        writeFully(buildModel)
    }
}

internal fun BytePacketBuilder.t145(
    guid: ByteArray
) {
    writeShort(0x145)
    writeShortLVPacket {
        writeFully(guid)
    }
}

internal fun BytePacketBuilder.t147(
    appId: Long,
    apkVersionName: ByteArray,
    apkSignatureMd5: ByteArray
) {
    writeShort(0x147)
    writeShortLVPacket {
        writeInt(appId.toInt())
        writeShortLVByteArrayLimitedLength(apkVersionName, 32)
        writeShortLVByteArrayLimitedLength(apkSignatureMd5, 32)
    }
}

internal fun BytePacketBuilder.t166(
    imageType: Int
) {
    writeShort(0x166)
    writeShortLVPacket {
        writeByte(imageType.toByte())
    }
}

internal fun BytePacketBuilder.t16a(
    noPicSig: ByteArray // unknown source
) {
    writeShort(0x16a)
    writeShortLVPacket {
        writeFully(noPicSig)
    }
}

internal fun BytePacketBuilder.t154(
    ssoSequenceId: Int // starts from 0
) {
    writeShort(0x154)
    writeShortLVPacket {
        writeInt(ssoSequenceId)
    }
}

internal fun BytePacketBuilder.t141(
    simInfo: ByteArray,
    networkType: NetworkType,
    apn: ByteArray
) {
    writeShort(0x141)
    writeShortLVPacket {
        writeShort(1) // version
        writeShortLVByteArray(simInfo)
        writeShort(networkType.value.toShort())
        writeShortLVByteArray(apn)
    }
}

internal fun BytePacketBuilder.t511(
    domains: List<String>
) {
    writeShort(0x511)
    writeShortLVPacket {
        val list = domains.filter { it.isNotEmpty() }
        writeShort(list.size.toShort())
        list.forEach { element ->
            if (element.startsWith('(')) {
                val split = element.drop(1).split(')')

                val flag = split[0].toInt()
                var n = (flag and 0x100000 > 0).toInt()
                if (flag and 0x8000000 > 0) {
                    n = n or 0x2
                }
                writeByte(n.toByte())

                writeShortLVString(split[1])
            } else {
                writeByte(1)
                writeShortLVString(element)
            }
        }
    }
}

internal fun BytePacketBuilder.t172(
    rollbackSig: ByteArray // 由服务器发来的 tlv_t172 获得
) {
    writeShort(0x172)
    writeShortLVPacket {
        writeFully(rollbackSig)
    }
}

internal fun BytePacketBuilder.t185() {
    writeShort(0x185)
    writeShortLVPacket {
        writeByte(1)
        writeByte(1)
    }
}

internal fun BytePacketBuilder.t400(
    g: ByteArray, // 用于加密这个 tlv
    uin: Long,
    guid: ByteArray,
    dpwd: ByteArray,
    appId: Long,
    subAppId: Long,
    randomSeed: ByteArray
) {
    writeShort(0x400)
    writeShortLVPacket {
        writeByte(1) // version
        writeLong(uin)

        encryptAndWrite(g) {
            writeFully(guid)
            writeFully(dpwd)
            writeInt(appId.toInt())
            writeInt(subAppId.toInt())
            writeLong(currentTimeMillis)
            writeFully(randomSeed)
        }
    }
}


internal fun BytePacketBuilder.t187(
    macAddress: ByteArray
) {
    writeShort(0x187)
    writeShortLVPacket {
        writeFully(MiraiPlatformUtils.md5(macAddress)) // may be md5
    }
}


internal fun BytePacketBuilder.t188(
    androidId: ByteArray
) {
    writeShort(0x188)
    writeShortLVPacket {
        writeFully(MiraiPlatformUtils.md5(androidId))
    } shouldEqualsTo 16
}

internal fun BytePacketBuilder.t193(
    ticket: String
) {
    writeShort(0x193)
    writeShortLVPacket {
        writeFully(ticket.toByteArray())
    }
}

internal fun BytePacketBuilder.t194(
    imsiMd5: ByteArray
) {
    imsiMd5 requireSize 16

    writeShort(0x194)
    writeShortLVPacket {
        writeFully(imsiMd5)
    } shouldEqualsTo 16
}

internal fun BytePacketBuilder.t191(
    K: Int = 0x82
) {
    writeShort(0x191)
    writeShortLVPacket {
        writeByte(K.toByte())
    }
}

internal fun BytePacketBuilder.t201(
    L: ByteArray = byteArrayOf(), // unknown
    channelId: ByteArray = byteArrayOf(),
    clientType: ByteArray = "qq".toByteArray(),
    N: ByteArray
) {
    writeShort(0x201)
    writeShortLVPacket {
        writeShortLVByteArray(L)
        writeShortLVByteArray(channelId)
        writeShortLVByteArray(clientType)
        writeShortLVByteArray(N)
    }
}

internal fun BytePacketBuilder.t202(
    wifiBSSID: ByteArray,
    wifiSSID: ByteArray
) {
    writeShort(0x202)
    writeShortLVPacket {
        writeShortLVByteArrayLimitedLength(wifiBSSID, 16)
        writeShortLVByteArrayLimitedLength(wifiSSID, 32)
    }
}

internal fun BytePacketBuilder.t177(
    unknown1: Long = 1571193922L,
    unknown2: String = "6.0.0.2413"
) {
    writeShort(0x177)
    writeShortLVPacket {
        writeByte(1)
        writeInt(unknown1.toInt())
        writeShortLVString(unknown2)
    } shouldEqualsTo 0x11
}

internal fun BytePacketBuilder.t516( // 1302
    sourceType: Int = 0 // always 0
) {
    writeShort(0x516)
    writeShortLVPacket {
        writeInt(sourceType)
    } shouldEqualsTo 4
}

internal fun BytePacketBuilder.t521( // 1313
    productType: Int = 0, // coz setProductType is never used
    unknown: Short = 0 // const
) {
    writeShort(0x521)
    writeShortLVPacket {
        writeInt(productType)
        writeShort(unknown)
    } shouldEqualsTo 6
}

internal fun BytePacketBuilder.t536( // 1334
    loginExtraData: ByteArray
) {
    writeShort(0x536)
    writeShortLVPacket {
        writeFully(loginExtraData)
    }
}

internal fun BytePacketBuilder.t525(
    t536: ByteReadPacket
) {
    writeShort(0x525)
    writeShortLVPacket {
        writeShort(1)
        writePacket(t536)
    }
}

internal fun BytePacketBuilder.t318(
    tgtQR: ByteArray // unknown
) {
    writeShort(0x318)
    writeShortLVPacket {
        writeFully(tgtQR)
    }
}

private inline fun Boolean.toByte(): Byte = if (this) 1 else 0
private inline fun Boolean.toInt(): Int = if (this) 1 else 0

// noinline: wrong exception stacktrace reported

private infix fun Int.shouldEqualsTo(int: Int) = check(this == int) { "Required $int, but found $this" }
private infix fun ByteArray.requireSize(exactSize: Int) = check(this.size == exactSize) { "Required size $exactSize, but found ${this.size}" }