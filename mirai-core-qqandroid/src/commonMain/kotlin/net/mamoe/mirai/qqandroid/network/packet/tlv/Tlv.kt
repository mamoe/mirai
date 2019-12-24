package net.mamoe.mirai.qqandroid.network.packet.tlv

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import kotlinx.io.core.writeFully
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.md5
import kotlin.random.Random

object Tlv {
    fun BytePacketBuilder.t1(qq: Long, ip: ByteArray) {
        require(ip.size == 4)
        writeShort(0x0001)
        writeShortLVPacket {
            writeShort(1) // ip_ver
            writeInt(Random.nextInt())
            writeInt(qq.toInt())
            writeTime()
            writeFully(ip)
            writeShort(0)
        }
    }

    fun BytePacketBuilder.t2(captchaCode: String, captchaToken: ByteArray, sigVer: Short = 0) {
        writeShort(0x0002)
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
            writeShort(1) //ping_version
            writeInt(1536) //sso_version
            writeInt(appId.toInt())
            writeInt(appClientVersion)
            writeInt(uin.toInt())
            writeShort(constant1_always_0.toShort())
            writeShort(0)
        }
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
        n8: Int = 1
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
                writeInt(n8)
                writeShortLVByteArray(uinAccount)
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
}


private infix fun Int.shouldEqualsTo(int: Int) = require(this == int)

fun randomAndroidId(): String = buildString(15) {
    repeat(15) { append(Random.nextInt(10)) }
}

fun generateGuid(androidId: String, macAddress: String): ByteArray {
    return md5(androidId + macAddress)
}

fun getMacAddr(): String = "02:00:00:00:00:00"