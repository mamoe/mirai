package net.mamoe.mirai.qqandroid.network.protocol.packet.tlv

import kotlinx.io.core.*
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.md5
import kotlin.random.Random


fun BytePacketBuilder.writeTLVList(block: TlvBuilder.() -> Unit) {
    var tlvCount = 0
    val tlvList = buildPacket { block(TlvBuilder { tlvCount++ }) }
    writeShort(tlvCount.toShort())
    writePacket(tlvList)
}


inline class LoginType(
    val value: Int
) {
    companion object {
        val SMS = LoginType(3)
        val PASSWORD = LoginType(1)
        val WE_CHAT = LoginType(4)
    }
}

inline class TlvBuilder(
    val counter: () -> Unit
) {
    fun BytePacketBuilder.t1(uin: Long, ip: ByteArray) {
        require(ip.size == 4)
        writeShort(0x1)
        writeShortLVPacket {
            writeShort(1) // _ip_ver
            writeInt(Random.nextInt())
            writeInt(uin.toInt())
            writeTime()
            writeFully(ip)
            writeShort(0)
        } shouldEqualsTo 20
    }

    fun BytePacketBuilder.t2(captchaCode: String, captchaToken: ByteArray, sigVer: Short = 0) {
        writeShort(0x2)
        writeShortLVPacket {
            writeShort(sigVer)
            writeShortLVString(captchaCode)
            writeShortLVByteArray(captchaToken)
        }
    }

    fun BytePacketBuilder.t8() {
        writeShort(0x0008)
        writeShortLVPacket {
            writeShort(0)
            writeInt(2052) // localId
            writeShort(0)
        }
    }

    fun BytePacketBuilder.t18(appId: Long, appClientVersion: Int, uin: Long, constant1_always_0: Int) {
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

    fun BytePacketBuilder.t106(
        appId: Long,
        subAppId: Long,
        appClientVersion: Int,
        uin: Long,
        ipAddress: ByteArray,
        n5_always_1: Int = 1,
        temp_pwd: ByteArray,
        salt: Long,
        uinAccount: ByteArray,
        tgtgtKey: ByteArray,
        n7: Int,
        array_6_may_be_null: ByteArray?,
        loginType: LoginType
    ) {
        writeShort(0x106)

        writeShortLVPacket {
            encryptAndWrite(
                if (salt == 0L) {
                    md5(buildPacket { writeFully(temp_pwd); writeInt(uin.toInt()) }.readBytes())
                } else {
                    md5(buildPacket { writeFully(temp_pwd); writeInt(salt.toInt()) }.readBytes())
                }
            ) {
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

                writeTime()
                writeFully(ipAddress)
                writeByte(n5_always_1.toByte())
                writeFully(temp_pwd)
                writeFully(tgtgtKey)
                writeInt(0)
                writeByte(n7.toByte())
                if (array_6_may_be_null == null) {
                    repeat(4) {
                        writeInt(Random.nextInt())
                    }
                } else {
                    writeFully(array_6_may_be_null)
                }
                writeInt(subAppId.toInt())
                writeInt(loginType.value)
                writeShortLVByteArray(uinAccount)
            }
        } shouldEqualsTo 98
    }

    fun BytePacketBuilder.t116(
        miscBitmap: Int,
        subSigMap: Int,
        appIdList: LongArray
    ) {
        writeShort(0x116)
        writeShortLVPacket {
            writeByte(0) // _ver
            writeInt(miscBitmap)
            writeInt(subSigMap)
            writeByte(appIdList.size.toByte())
            appIdList.forEach {
                writeInt(it.toInt())
            }
        }
    }

    fun BytePacketBuilder.t100(
        appId: Long,
        subAppId: Long,
        appClientVersion: Int,
        mainSigMap: Int
    ) {
        writeShort(0x100)
        writeShortLVPacket {
            writeShort(1)//db_buf_ver
            writeInt(5)//sso_ver
            writeInt(appId.toInt())
            writeInt(subAppId.toInt())
            writeInt(appClientVersion)
            writeInt(mainSigMap)
        } shouldEqualsTo 22
    }

    fun BytePacketBuilder.t107(
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

    fun BytePacketBuilder.t108(
        to_verify_passwd_img: ByteArray
    ) {
        writeShort(0x108)
        writeShortLVPacket {
            writeFully(to_verify_passwd_img)
        }
    }

    fun BytePacketBuilder.t104(
        t104Data: ByteArray
    ) {
        writeShort(0x104)
        writeShortLVPacket {
            writeFully(t104Data)
        }
    }

    /**
     * @param apkId application.getPackageName().getBytes()
     */
    fun BytePacketBuilder.t142(
        apkId: ByteArray
    ) {
        writeShort(0x142)
        writeShortLVPacket {
            writeShort(0) //_version
            writeShortLVByteArrayLimitedLength(apkId, 32)
        }
    }

    fun BytePacketBuilder.t112(
        nonNumberUin: ByteArray
    ) {
        writeShort(0x112)
        writeShortLVPacket {
            writeFully(nonNumberUin)
        }
    }

    fun BytePacketBuilder.t144(
        // t109
        androidId: ByteArray,

        // t52d
        androidDevInfo: ByteArray,

        // t124
        osType: ByteArray = "android".toByteArray(),
        osVersion: ByteArray,
        ipv6NetType: Int,
        simInfo: ByteArray,
        unknown: ByteArray,
        apn: ByteArray = "wifi".toByteArray(),

        // t128
        isGuidFromFileNull: Boolean = false,
        isGuidAvailable: Boolean = true,
        isGuidChanged: Boolean = false,
        guidFlag: Int,
        buildModel: ByteArray,
        guid: ByteArray,
        buildBrand: ByteArray,

        // encrypt
        tgtgtKey: ByteArray
    ) {
        writeShort(0x144)
        writeShortLVPacket {
            encryptAndWrite(tgtgtKey) {
                t109(androidId)
                t52d(androidDevInfo)
                t124(osType, osVersion, ipv6NetType, simInfo, unknown, apn)
                t128(isGuidFromFileNull, isGuidAvailable, isGuidChanged, guidFlag, buildModel, guid, buildBrand)
                t16e(buildModel)
            }
        }
    }

    fun BytePacketBuilder.t109(
        androidId: ByteArray
    ) {
        writeShort(0x109)
        writeShortLVPacket {
            writeFully(androidId)
        }
    }

    fun BytePacketBuilder.t52d(
        androidDevInfo: ByteArray // oicq.wlogin_sdk.tools.util#get_android_dev_info
    ) {
        writeShort(0x52d)
        writeShortLVPacket {
            writeFully(androidDevInfo)
        }
    }

    fun BytePacketBuilder.t124(
        osType: ByteArray = "android".toByteArray(),
        osVersion: ByteArray, // Build.VERSION.RELEASE.toByteArray()
        ipv6NetType: Int,  //oicq.wlogin_sdk.tools.util#get_network_type
        simInfo: ByteArray, // oicq.wlogin_sdk.tools.util#get_sim_operator_name
        unknown: ByteArray,
        apn: ByteArray = "wifi".toByteArray() // oicq.wlogin_sdk.tools.util#get_apn_string
    ) {
        writeShort(0x124)
        writeShortLVPacket {
            writeShortLVByteArrayLimitedLength(osType, 16)
            writeShortLVByteArrayLimitedLength(osVersion, 16)
            writeShort(ipv6NetType.toShort())
            writeShortLVByteArrayLimitedLength(simInfo, 16)
            writeShortLVByteArrayLimitedLength(unknown, 32)
            writeShortLVByteArrayLimitedLength(apn, 16)
        }
    }

    fun BytePacketBuilder.t128(
        isGuidFromFileNull: Boolean = false, // 保存到文件的 GUID 是否为 null
        isGuidAvailable: Boolean = true, // GUID 是否可用(计算/读取成功)
        isGuidChanged: Boolean = false, // GUID 是否有变动
        /**
         * guidFlag:
         * ```java
         * GUID_FLAG |= GUID_SRC << 24 & 0xFF000000;
         * GUID_FLAG |= FLAG_MAC_ANDROIDID_GUID_CHANGE << 8 & 0xFF00;
         * ```
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
        guidFlag: Int,
        buildModel: ByteArray,  // android.os.Build.MODEL
        /**
         * [generateGuid] or `"%4;7t>;28<fc.5*6".toByteArray()`
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
            writeInt(guidFlag)
            writeShortLVByteArrayLimitedLength(buildModel, 32)
            writeShortLVByteArrayLimitedLength(guid, 16)
            writeShortLVByteArrayLimitedLength(buildBrand, 16)
        }
    }

    fun BytePacketBuilder.t16e(
        buildModel: ByteArray
    ) {
        writeShort(0x16e)
        writeShortLVPacket {
            writeFully(buildModel)
        }
    }

    fun BytePacketBuilder.t145(
        guid: ByteArray
    ) {
        writeShort(0x145)
        writeShortLVPacket {
            writeFully(guid)
        }
    }

    fun BytePacketBuilder.t147(
        appId: Long,
        apkVersionName: ByteArray,
        apkSignatureMd5: ByteArray
    ) {
        writeShort(0x147)
        writeShortLVPacket {
            writeLong(appId)
            writeShortLVByteArrayLimitedLength(apkVersionName, 32)
            writeShortLVByteArrayLimitedLength(apkSignatureMd5, 32)
        }
    }

    fun BytePacketBuilder.t166(
        imageType: Int
    ) {
        writeShort(0x166)
        writeShortLVPacket {
            writeByte(imageType.toByte())
        }
    }
}

private fun Boolean.toByte(): Byte = if (this) 1 else 0

private infix fun Int.shouldEqualsTo(int: Int) = require(this == int)

fun randomAndroidId(): String = buildString(15) {
    repeat(15) { append(Random.nextInt(10)) }
}

/**
 * Defaults "%4;7t>;28<fc.5*6".toByteArray()
 */
fun generateGuid(androidId: String, macAddress: String): ByteArray {
    return md5(androidId + macAddress)
}

fun getMacAddr(): String = "02:00:00:00:00:00"


// AndroidDevInfo: oicq.wlogin_sdk.tools.util#get_android_dev_info
