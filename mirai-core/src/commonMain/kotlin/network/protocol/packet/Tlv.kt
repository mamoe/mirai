/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.internal.network.protocol.packet

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.internal.network.components.encryptServiceOrNull
import net.mamoe.mirai.internal.network.protocol.LoginType
import net.mamoe.mirai.internal.spi.EncryptServiceContext
import net.mamoe.mirai.internal.utils.GuidSource
import net.mamoe.mirai.internal.utils.MacOrAndroidIdChangeFlag
import net.mamoe.mirai.internal.utils.NetworkType
import net.mamoe.mirai.internal.utils.guidFlag
import net.mamoe.mirai.internal.utils.io.encryptAndWrite
import net.mamoe.mirai.internal.utils.io.writeShortLVByteArray
import net.mamoe.mirai.internal.utils.io.writeShortLVByteArrayLimitedLength
import net.mamoe.mirai.internal.utils.io.writeShortLVString
import net.mamoe.mirai.utils.*
import kotlin.random.Random

private val Char.isHumanReadable get() = this in '0'..'9' || this in 'a'..'z' || this in 'A'..'Z' || this in """ <>?,.";':/\][{}~!@#$%^&*()_+-=`""" || this in "\n\r"

internal fun TlvMap.smartToString(leadingLineBreak: Boolean = true, sorted: Boolean = true): String {
    fun ByteArray.valueToString(): String {
        val str = this.decodeToString()
        return if (str.all { it.isHumanReadable }) str
        else this.toUHexString()
    }

    val map = if (sorted) entries.sortedBy { it.key } else this.entries

    return buildString {
        if (leadingLineBreak) appendLine()
        appendLine("count=${map.size}")
        appendLine(map.joinToString("\n") { (key, value) ->
            "0x" + key.toShort().toUHexString("") + " = " + value.valueToString()
        })
    }
}

/**
 * 显式表示一个 [ByteArray] 是一个 tlv 的 body
 */
@JvmInline
internal value class Tlv(val value: ByteArray)

internal fun TlvMapWriter.t1(uin: Long, timeSeconds: Int, ipv4: ByteArray) {
    require(ipv4.size == 4) { "ip.size must == 4" }

    tlv(0x01) {
        writeShort(1) // _ip_ver
        writeInt(Random.nextInt())
        writeInt(uin.toInt())
        writeInt(timeSeconds)
        writeFully(ipv4)
        writeShort(0)
    }
}

internal fun TlvMapWriter.t2(captchaCode: String, captchaToken: ByteArray, sigVer: Short = 0) {
    tlv(0x02) {
        writeShort(sigVer)
        writeShortLVString(captchaCode)
        writeShortLVByteArray(captchaToken)
    }
}

internal fun TlvMapWriter.t8(
    localId: Int = 2052
) {
    tlv(0x08) {
        writeShort(0)
        writeInt(localId) // localId
        writeShort(0)
    }
}

internal fun TlvMapWriter.t16(
    ssoVersion: Int,
    subAppId: Long,
    guid: ByteArray,
    apkId: ByteArray,
    apkVersionName: ByteArray,
    apkSignatureMd5: ByteArray
) {
    tlv(0x16) {
        writeInt(ssoVersion)
        writeInt(16)
        writeInt(subAppId.toInt())
        writeFully(guid)
        writeShortLVByteArray(apkId)
        writeShortLVByteArray(apkVersionName)
        writeShortLVByteArray(apkSignatureMd5)
    }
}

internal fun TlvMapWriter.t18(
    appId: Long,
    appClientVersion: Int = 0,
    uin: Long,
    constant1_always_0: Int = 0
) {
    tlv(0x18) {
        writeShort(1) //_ping_version
        writeInt(0x00_00_06_00) //_sso_version=1536
        writeInt(appId.toInt())
        writeInt(appClientVersion)
        writeInt(uin.toInt())
        writeShort(constant1_always_0.toShort())
        writeShort(0)
    }
}

internal fun TlvMapWriter.t1b(
    micro: Int = 0,
    version: Int = 0,
    size: Int = 3,
    margin: Int = 4,
    dpi: Int = 72,
    ecLevel: Int = 2,
    hint: Int = 2
) {
    tlv(0x1b) {
        writeInt(micro)
        writeInt(version)
        writeInt(size)
        writeInt(margin)
        writeInt(dpi)
        writeInt(ecLevel)
        writeInt(hint)
        writeShort(0)
    }
}

internal fun TlvMapWriter.t1d(
    miscBitmap: Int,
) {
    tlv(0x1d) {
        writeByte(1)
        writeInt(miscBitmap)
        writeInt(0)
        writeByte(0)
        writeInt(0)
    }
}

internal fun TlvMapWriter.t1f(
    isRoot: Boolean = false,
    osName: ByteArray,
    osVersion: ByteArray,
    simVendor: ByteArray,
    apn: ByteArray,
    networkType: Short = 2,
) {
    tlv(0x1f) {
        writeByte(if (isRoot) 1 else 0)
        writeShortLVByteArray(osName)
        writeShortLVByteArray(osVersion)
        writeShort(networkType)
        writeShortLVByteArray(simVendor)
        writeShortLVByteArray(EMPTY_BYTE_ARRAY)
        writeShortLVByteArray(apn)
    }
}

internal fun TlvMapWriter.t33(
    guid: ByteArray,
) {
    tlv(0x33, guid)
}

internal fun TlvMapWriter.t35(
    productType: Int
) {
    tlv(0x35) {
        writeInt(productType)
    }
}

internal fun TlvMapWriter.t106(
    client: QQAndroidClient,
    appId: Long = 16L,
    passwordMd5: ByteArray,
) {
    return t106(
        appId,
        client.subAppId /* maybe 1*/,
        client.appClientVersion,
        client.uin,
        client.device.ipAddress,
        true,
        passwordMd5,
        0,
        client.uin.toByteArray(),
        client.tgtgtKey,
        true,
        client.device.guid,
        LoginType.PASSWORD,
        client.ssoVersion
    )
}

internal fun TlvMapWriter.t106(
    encryptA1: ByteArray
) {
    tlv(0x106) {
        writeFully(encryptA1)
    }
}

/**
 * A1
 */
internal fun TlvMapWriter.t106(
    appId: Long = 16L,
    subAppId: Long,
    appClientVersion: Int = 0,
    uin: Long,
    ipv4: ByteArray,
    isSavePassword: Boolean = true,
    passwordMd5: ByteArray,
    salt: Long,
    uinAccountString: ByteArray,
    tgtgtKey: ByteArray,
    isGuidAvailable: Boolean = true,
    guid: ByteArray?,
    loginType: LoginType,
    ssoVersion: Int,
) {
    passwordMd5.requireSize(16)
    tgtgtKey.requireSize(16)
    guid?.requireSize(16)
    ipv4.requireSize(4)

    tlv(0x106) {
        encryptAndWrite(
            (passwordMd5 + ByteArray(4) + (salt.takeIf { it != 0L } ?: uin).toInt()
                .toByteArray()).md5()
        ) {
            writeShort(4)//TGTGTVer
            writeInt(Random.nextInt())
            writeInt(ssoVersion)//ssoVer
            writeInt(appId.toInt())
            writeInt(appClientVersion)

            if (uin == 0L) {
                writeLong(salt)
            } else {
                writeLong(uin)
            }

            writeInt(currentTimeSeconds().toInt())
            writeFully(ipv4) //
            writeByte(isSavePassword.toByte())
            writeFully(passwordMd5)
            writeFully(tgtgtKey)
            writeInt(0) // wtf
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

internal fun TlvMapWriter.t116(
    miscBitmap: Int,
    subSigMap: Int,
    appIdList: LongArray = longArrayOf(1600000226L)
) {
    tlv(0x116) {
        writeByte(0) // _ver
        writeInt(miscBitmap) // 184024956
        writeInt(subSigMap) // 66560
        writeByte(appIdList.size.toByte())
        appIdList.forEach {
            writeInt(it.toInt())
        }
    }
}


internal fun TlvMapWriter.t100(
    appId: Long = 16,
    subAppId: Long,
    appClientVersion: Int,
    ssoVersion: Int,
    mainSigMap: Int
) {
    tlv(0x100) {
        writeShort(1)//db_buf_ver
        writeInt(ssoVersion)//sso_ver
        writeInt(appId.toInt())
        writeInt(subAppId.toInt())
        writeInt(appClientVersion)
        writeInt(mainSigMap) // sigMap, 34869472?
    }
}

internal fun TlvMapWriter.t10a(
    tgt: ByteArray,
) {
    tlv(0x10a) {
        writeFully(tgt)
    }
}


internal fun TlvMapWriter.t107(
    picType: Int,
    capType: Int = 0,
    picSize: Int = 0,
    retType: Int = 1
) {
    tlv(0x107) {
        writeShort(picType.toShort())
        writeByte(capType.toByte())
        writeShort(picSize.toShort())
        writeByte(retType.toByte())
    }
}

internal fun TlvMapWriter.t108(
    ksid: ByteArray
) {
    // require(ksid.size == 16) { "ksid should length 16" }
    tlv(0x108) {
        writeFully(ksid)
    }
}

internal fun TlvMapWriter.t104(
    t104Data: ByteArray
) {
    tlv(0x104) {
        writeFully(t104Data)
    }
}

internal fun TlvMapWriter.t547(
    t547Data: ByteArray
) {
    tlv(0x547) {
        writeFully(t547Data)
    }
}

internal fun TlvMapWriter.t174(
    t174Data: ByteArray
) {
    tlv(0x174) {
        writeFully(t174Data)
    }
}


internal fun TlvMapWriter.t17a(
    smsAppId: Int = 0
) {
    tlv(0x17a) {
        writeInt(smsAppId)
    }
}

internal fun TlvMapWriter.t197(
    devLockMobileType: Byte = 0
) {
    tlv(0x197) {
        writeByte(devLockMobileType)
    }
}

internal fun TlvMapWriter.t198() {
    tlv(0x198) {
        writeByte(0)
    }
}

internal fun TlvMapWriter.t19e(
    value: Int = 0
) {
    tlv(0x19e) {
        writeShort(1)
        writeByte(value.toByte())
    }
}

internal fun TlvMapWriter.t17c(
    t17cData: ByteArray
) {
    tlv(0x17c) {
        writeShort(t17cData.size.toShort())
        writeFully(t17cData)
    }
}

internal fun TlvMapWriter.t401(
    t401Data: ByteArray
) {
    tlv(0x401) {
        writeFully(t401Data)
    }
}

/**
 * @param apkId application.getPackageName().getBytes()
 */
internal fun TlvMapWriter.t142(
    apkId: ByteArray
) {
    tlv(0x142) {
        writeShort(0) //_version
        writeShortLVByteArrayLimitedLength(apkId, 32)
    }
}

internal fun TlvMapWriter.t143(
    d2: ByteArray
) {
    tlv(0x143) {
        writeFully(d2)
    }
}

internal fun TlvMapWriter.t112(
    nonNumberUin: ByteArray
) {
    tlv(0x112) {
        writeFully(nonNumberUin)
    }
}

internal fun TlvMapWriter.t144(
    client: QQAndroidClient
) {
    return t144(
        androidId = client.device.androidId,
        androidDevInfo = client.device.generateDeviceInfoData(),
        osType = client.device.osType,
        osVersion = client.device.version.release,
        networkType = client.networkType,
        simInfo = client.device.simInfo,
        unknown = byteArrayOf(),
        apn = client.device.apn,
        isGuidFromFileNull = false,
        isGuidAvailable = true,
        isGuidChanged = false,
        guidFlag = guidFlag(GuidSource.FROM_STORAGE, MacOrAndroidIdChangeFlag(0)),
        buildModel = client.device.model,
        guid = client.device.guid,
        buildBrand = client.device.brand,
        tgtgtKey = client.tgtgtKey
    )
}

internal fun TlvMapWriter.t144(
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
    tlv(0x144) {
        encryptAndWrite(tgtgtKey) {
            _writeTlvMap {
                t109(androidId)
                t52d(androidDevInfo)
                t124(osType, osVersion, networkType, simInfo, unknown, apn)
                t128(isGuidFromFileNull, isGuidAvailable, isGuidChanged, guidFlag, buildModel, guid, buildBrand)
                t16e(buildModel)
            }
        }
    }
}


internal fun TlvMapWriter.t109(
    androidId: ByteArray
) {
    tlv(0x109) {
        writeFully(androidId.md5())
    }
}

internal fun TlvMapWriter.t52d(
    androidDevInfo: ByteArray // oicq.wlogin_sdk.tools.util#get_android_dev_info
) {
    tlv(0x52d) {
        writeFully(androidDevInfo)

        // 0A  07  75  6E  6B  6E  6F  77  6E  12  7E  4C  69  6E  75  78  20  76  65  72  73  69  6F  6E  20  34  2E  39  2E  33  31  20  28  62  75  69  6C  64  40  42  75  69  6C  64  32  29  20  28  67  63  63  20  76  65  72  73  69  6F  6E  20  34  2E  39  20  32  30  31  35  30  31  32  33  20  28  70  72  65  72  65  6C  65  61  73  65  29  20  28  47  43  43  29  20  29  20  23  31  20  53  4D  50  20  50  52  45  45  4D  50  54  20  54  68  75  20  44  65  63  20  31  32  20  31  35  3A  33  30  3A  35  35  20  49  53  54  20  32  30  31  39  1A  03  52  45  4C  22  03  33  32  37  2A  41  4F  6E  65  50  6C  75  73  2F  4F  6E  65  50  6C  75  73  35  2F  4F  6E  65  50  6C  75  73  35  3A  37  2E  31  2E  31  2F  4E  4D  46  32  36  58  2F  31  30  31  37  31  36  31  37  3A  75  73  65  72  2F  72  65  6C  65  61  73  65  2D  6B  65  79  73  32  24  36  63  39  39  37  36  33  66  2D  66  62  34  32  2D  34  38  38  31  2D  62  37  32  65  2D  63  37  61  61  38  61  36  63  31  63  61  34  3A  10  65  38  63  37  30  35  34  64  30  32  66  33  36  33  64  30  42  0A  6E  6F  20  6D  65  73  73  61  67  65  4A  03  33  32  37

    }
}

internal fun TlvMapWriter.t124(
    osType: ByteArray = "android".toByteArray(),
    osVersion: ByteArray, // Build.VERSION.RELEASE.toByteArray()
    networkType: NetworkType,  //oicq.wlogin_sdk.tools.util#get_network_type
    simInfo: ByteArray, // oicq.wlogin_sdk.tools.util#get_sim_operator_name
    address: ByteArray, // always new byte[0]
    apn: ByteArray = "wifi".toByteArray() // oicq.wlogin_sdk.tools.util#get_apn_string
) {
    tlv(0x124) {
        writeShortLVByteArrayLimitedLength(osType, 16)
        writeShortLVByteArrayLimitedLength(osVersion, 16)
        writeShort(networkType.value.toShort())
        writeShortLVByteArrayLimitedLength(simInfo, 16)
        writeShortLVByteArrayLimitedLength(address, 32)
        writeShortLVByteArrayLimitedLength(apn, 16)
    }
}

internal fun TlvMapWriter.t128(
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
    tlv(0x128) {
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

internal fun TlvMapWriter.t16e(
    buildModel: ByteArray
) {
    tlv(0x16e) {
        writeFully(buildModel)
    }
}

internal fun TlvMapWriter.t145(
    guid: ByteArray
) {
    tlv(0x145) {
        writeFully(guid)
    }
}

internal fun TlvMapWriter.t147(
    appId: Long,
    apkVersionName: ByteArray,
    apkSignatureMd5: ByteArray
) {
    tlv(0x147) {
        writeInt(appId.toInt())
        writeShortLVByteArrayLimitedLength(apkVersionName, 32)
        writeShortLVByteArrayLimitedLength(apkSignatureMd5, 32)
    }
}

internal fun TlvMapWriter.t166(
    imageType: Int
) {
    tlv(0x166) {
        writeByte(imageType.toByte())
    }
}

internal fun TlvMapWriter.t16a(
    noPicSig: ByteArray // unknown source
) {
    tlv(0x16a) {
        writeFully(noPicSig)
    }
}

internal fun TlvMapWriter.t154(
    ssoSequenceId: Int // starts from 0
) {
    tlv(0x154) {
        writeInt(ssoSequenceId)
    }
}

internal fun TlvMapWriter.t141(
    simInfo: ByteArray,
    networkType: NetworkType,
    apn: ByteArray
) {
    tlv(0x141) {
        writeShort(1) // version
        writeShortLVByteArray(simInfo)
        writeShort(networkType.value.toShort())
        writeShortLVByteArray(apn)
    }
}

internal fun TlvMapWriter.t511(
    domains: List<String> = listOf(
        "tenpay.com",
        "openmobile.qq.com",
        "docs.qq.com",
        "connect.qq.com",
        "qzone.qq.com",
        "vip.qq.com",
        "gamecenter.qq.com",
        "qun.qq.com",
        "game.qq.com",
        "qqweb.qq.com",
        "office.qq.com",
        "ti.qq.com",
        "mail.qq.com",
        "mma.qq.com",
    )
) {
    tlv(0x511) {
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

internal fun TlvMapWriter.t172(
    rollbackSig: ByteArray // 由服务器发来的 tlv_t172 获得
) {
    tlv(0x172) {
        writeFully(rollbackSig)
    }
}

internal fun TlvMapWriter.t185() {
    tlv(0x185) {
        writeByte(1)
        writeByte(1)
    }
}

internal fun TlvMapWriter.t400(
    /**
     *  if (var1[2] != null && var1[2].length > 0) {
    this._G = (byte[])var1[2].clone();
    }
     */
    g: ByteArray, // 用于加密这个 tlv
    uin: Long,
    guid: ByteArray,
    dpwd: ByteArray,
    appId: Long,
    subAppId: Long,
    randomSeed: ByteArray
) {
    tlv(0x400) {
        encryptAndWrite(g) {
            writeByte(1) // version
            writeLong(uin)
            writeFully(guid)
            writeFully(dpwd)
            writeInt(appId.toInt())
            writeInt(subAppId.toInt())
            writeInt(currentTimeSeconds().toInt())
            writeFully(randomSeed)
        }
    }
}


internal fun TlvMapWriter.t187(
    macAddress: ByteArray
) {
    tlv(0x187) {
        writeFully(macAddress.md5()) // may be md5
    }
}


internal fun TlvMapWriter.t188(
    androidId: ByteArray
) {
    tlv(0x188) {
        writeFully(androidId.md5())
    }
}

internal fun TlvMapWriter.t193(
    ticket: String
) {
    tlv(0x193) {
        writeFully(ticket.toByteArray())
    }
}

internal fun TlvMapWriter.t194(
    imsiMd5: ByteArray
) {
    imsiMd5 requireSize 16

    tlv(0x194) {
        writeFully(imsiMd5)
    }
}

internal fun TlvMapWriter.t191(
    K: Int = 0x82
) {
    tlv(0x191) {
        writeByte(K.toByte())
    }
}

internal fun TlvMapWriter.t201(
    L: ByteArray = byteArrayOf(), // unknown
    channelId: ByteArray = byteArrayOf(),
    clientType: ByteArray = "qq".toByteArray(),
    N: ByteArray
) {
    tlv(0x201) {
        writeShortLVByteArray(L)
        writeShortLVByteArray(channelId)
        writeShortLVByteArray(clientType)
        writeShortLVByteArray(N)
    }
}

internal fun TlvMapWriter.t202(
    wifiBSSID: ByteArray,
    wifiSSID: ByteArray
) {
    tlv(0x202) {
        writeShortLVByteArrayLimitedLength(wifiBSSID, 16)
        writeShortLVByteArrayLimitedLength(wifiSSID, 32)
    }
}

internal fun TlvMapWriter.t177(
    buildTime: Long = 1571193922L, // wtLogin BuildTime
    buildVersion: String = "6.0.0.2413" // wtLogin SDK Version
) {
    tlv(0x177) {
        writeByte(1)
        writeInt(buildTime.toInt())
        writeShortLVString(buildVersion)
    } // shouldEqualsTo 0x11
}

internal fun TlvMapWriter.t516( // 1302
    sourceType: Int = 0 // always 0
) {
    tlv(0x516) {
        writeInt(sourceType)
    }
}

internal fun TlvMapWriter.t521( // 1313
    productType: Int = 0, // coz setProductType is never used
    unknown: Short = 0 // const
) {
    tlv(0x521) {
        writeInt(productType)
        writeShort(unknown)
    }
}

internal fun TlvMapWriter.t52c(
    // ?
) {
    tlv(0x52c) {
        writeByte(1)
        writeLong(-1)
    }
}

internal fun TlvMapWriter.t536( // 1334
    loginExtraData: ByteArray
) {
    tlv(0x536) {
        writeFully(loginExtraData)
    }
}

internal fun TlvMapWriter.t536( // 1334
    loginExtraData: Collection<LoginExtraData>
) {
    tlv(0x536) {
        //com.tencent.loginsecsdk.ProtocolDet#packExtraData
        writeByte(1) // const
        writeByte(loginExtraData.size.toByte()) // data count
        for (extraData in loginExtraData) {
            writeLoginExtraData(extraData)
        }
    }
}

internal fun TlvMapWriter.t525(
    loginExtraData: Collection<LoginExtraData>,
) {
    tlv(0x525) {
        _writeTlvMap {
            t536(loginExtraData)
        }
    }
}

internal fun TlvMapWriter.t525(
    t536: ByteReadPacket = buildPacket {
        _writeTlvMap(includeCount = false) {
            t536(buildPacket {
                //com.tencent.loginsecsdk.ProtocolDet#packExtraData
                writeByte(1) // const
                writeByte(0) // data count
            }.readBytes())
        }
    }
) {
    tlv(0x525) {
        writeShort(1)
        writePacket(t536)
    }
}

internal fun TlvMapWriter.t542(
    value: ByteArray
) {
    tlv(0x542) {
        writeFully(value)
    }
}

internal fun TlvMapWriter.t545(
    qimei: String
) {
    tlv(0x545) {
        writeFully(qimei.toByteArray())
    }
}

internal fun TlvMapWriter.t548(
    nativeGetTestData: ByteArray = (
            "01 02 01 01 00 0A 00 00 00 80 5E C1 1A B0 39 A0 " +
                    "E0 5C 67 DF 44 F8 E5 86 91 A2 A4 5D 92 2B 25 3A " +
                    "B6 6E 2F F1 A1 E3 60 B8 36 1E 2F 6B 6F F7 2D F7 " +
                    "F8 21 F1 0B 75 7D 2A 4F 63 B8 83 9C 41 0B AA C7 " +
                    "C9 69 0D 70 AB F3 0F 46 28 C2 CD DB 81 CC 74 18 " +
                    "ED 97 CD 31 3E 1A 17 F1 94 96 AB 6C 6B 25 4F 83 " +
                    "5B 15 82 B0 8F 53 82 3F 59 FE 6E B5 EA B5 EA 7A " +
                    "0C E7 2B 31 CA 4C FD 43 9A DB 40 7A CA 51 D7 9A " +
                    "3C AD 6D 8F 3C C6 84 A5 4A 5F 00 20 BE FB 91 06 " +
                    "F0 67 42 8B CC 59 27 4E BC 91 78 55 4E E4 5C 98 " +
                    "4B 8B 0F C9 A3 83 56 06 E8 AE 5A 0D 00 AC 01 02 " +
                    "01 02 00 0A 00 00 00 80 5E C1 1A B0 39 A0 E0 5C " +
                    "67 DF 44 F8 E5 86 91 A2 A4 5D 92 2B 25 3A B6 6E " +
                    "2F F1 A1 E3 60 B8 36 1E 2F 6B 6F F7 2D F7 F8 21 " +
                    "F1 0B 75 7D 2A 4F 63 B8 83 9C 41 0B AA C7 C9 69 " +
                    "0D 70 AB F3 0F 46 28 C2 CD DB 81 CC 74 18 ED 97 " +
                    "CD 31 3E 1A 17 F1 94 96 AB 6C 6B 25 4F 83 5B 15 " +
                    "82 B0 8F 53 82 3F 59 FE 6E B5 EA B5 EA 7A 0C E7 " +
                    "2B 31 CA 4C FD 43 9A DB 40 7A CA 51 D7 9A 3C AD " +
                    "6D 8F 3C C6 84 A5 4A 5F 00 20 BE FB 91 06 F0 67 " +
                    "42 8B CC 59 27 4E BC 91 78 55 4E E4 5C 98 4B 8B " +
                    "0F C9 A3 83 56 06 E8 AE 5A 0D 00 80 5E C1 1A B0 " +
                    "39 A0 E0 5C 67 DF 44 F8 E5 86 91 A2 A4 5D 92 2B " +
                    "25 3A B6 6E 2F F1 A1 E3 60 B8 36 1E 2F 6B 6F F7 " +
                    "2D F7 F8 21 F1 0B 75 7D 2A 4F 63 B8 83 9C 41 0B " +
                    "AA C7 C9 69 0D 70 AB F3 0F 46 28 C2 CD DB 81 CC " +
                    "74 18 ED 97 CD 31 3E 1A 17 F1 94 96 AB 6C 6B 25 " +
                    "4F 83 5B 15 82 B0 8F 53 82 3F 59 FE 6E B5 EA B5 " +
                    "EA 7A 0C E7 2B 31 CA 4C FD 43 9A DB 40 7A CA 51 " +
                    "D7 9A 3C AD 6D 8F 3C C6 84 A5 71 6F 00 00 00 1F " +
                    "00 00 27 10").hexToBytes()
) {
    tlv(0x548) {
        writeFully(nativeGetTestData)
    }
}


internal fun TlvMapWriter.t544ForToken( // 1348
    client: QQAndroidClient,
    uin: Long,
    protocol: BotConfiguration.MiraiProtocol,
    guid: ByteArray,
    sdkVersion: String,
    subCommandId: Int,
    commandStr: String
) {
    val service = client.bot.encryptServiceOrNull ?: return
    tlv(0x544) {
        buildPacket {
            writeFully(buildPacket {
                writeLong(uin)
            }.readBytes(4))
            writeShortLVByteArray(guid)
            writeShortLVString(sdkVersion)
            writeInt(subCommandId)
            writeInt(0)
        }.use { dataIn ->
            service.encryptTlv(EncryptServiceContext(uin, buildTypeSafeMap {
                set(EncryptServiceContext.KEY_COMMAND_STR, commandStr)
                set(EncryptServiceContext.KEY_BOT_PROTOCOL, protocol)
            }), 0x544, dataIn.readBytes())
        }.let { result ->
            writeFully(result ?: "".toByteArray()) // Empty str means native throws exception
        }
    }
}

internal fun TlvMapWriter.t544ForVerify( // 1348
    client: QQAndroidClient,
    uin: Long,
    protocol: BotConfiguration.MiraiProtocol,
    guid: ByteArray,
    sdkVersion: String,
    subCommandId: Int,
    commandStr: String
) {
    val service = client.bot.encryptServiceOrNull ?: return
    tlv(0x544) {
        buildPacket {
            writeLong(uin)
            writeShortLVByteArray(guid)
            writeShortLVString(sdkVersion)
            writeInt(subCommandId)
        }.use { dataIn ->
            service.encryptTlv(EncryptServiceContext(uin, buildTypeSafeMap {
                set(EncryptServiceContext.KEY_COMMAND_STR, commandStr)
                set(EncryptServiceContext.KEY_BOT_PROTOCOL, protocol)
            }), 0x544, dataIn.readBytes())
        }.let { result ->
            writeFully(result ?: "".toByteArray()) // Empty str means native throws exception
        }
    }
}

internal fun TlvMapWriter.t318(
    tgtQR: ByteArray // unknown
) {
    tlv(0x318) {
        writeFully(tgtQR)
    }
}

private inline fun Boolean.toByte(): Byte = if (this) 1 else 0
private inline fun Boolean.toInt(): Int = if (this) 1 else 0

// noinline: wrong exception stacktrace reported

private infix fun Int.shouldEqualsTo(int: Int) = check(this == int) { "Required $int, but found $this" }
private infix fun ByteArray.requireSize(exactSize: Int) =
    check(this.size == exactSize) { "Required size $exactSize, but found ${this.size}" }
