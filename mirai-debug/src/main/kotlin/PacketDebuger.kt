@file:Suppress("EXPERIMENTAL_API_USAGE")

import jpcap.JpcapCaptor
import jpcap.packet.IPPacket
import jpcap.packet.UDPPacket
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.*
import java.io.DataInputStream

/**
 * 模拟登录并抓取到 session key
 *
 * @author Him188moe
 */
object Main {
    const val localIp = "192.168.3.10"

    @JvmStatic
    fun main(args: Array<String>) {
        /*--------------	第一步绑定网络设备       --------------*/
        val devices = JpcapCaptor.getDeviceList()

        /*
        \Device\NPF_{0E7103E4-BF96-4B66-A23B-F6F630D814CD}     |     Microsoft
        \Device\NPF_{2CCA31E2-93D5-42F2-92C1-5882E18A8E95}     |     VMware Virtual Ethernet Adapter
        \Device\NPF_{A12C8971-858B-4BC8-816C-4077E1636AC5}     |     VMware Virtual Ethernet Adapter
        \Device\NPF_{231C4E27-AF20-4362-BCA3-107236CB8A2E}     |     MS NDIS 6.0 LoopBack Driver
        \Device\NPF_{500B5537-AA10-4E2F-8F7D-E6BD365BDCD1}     |     Microsoft
        \Device\NPF_{A177317B-903A-45B5-8AEA-3698E423ABD6}     |     Microsoft
         */
        /*
        for (n in devices) {
            println(n.name + "     |     " + n.description)
        }
        println("-------------------------------------------")
        exitProcess(0)*/

        val jpcap: JpcapCaptor?
        val caplen = 4096
        val promiscCheck = true

        jpcap = JpcapCaptor.openDevice(devices[1], caplen, promiscCheck, 50)


        /*----------第二步抓包-----------------*/
        while (true) {
            assert(jpcap != null)
            val pk = jpcap!!.packet
            if (pk is IPPacket && pk.version.toInt() == 4) {

                if (pk is UDPPacket) {
                    if (pk.dst_port != 8000 && pk.src_port != 8000) {
                        continue
                    }

                    if (localIp == pk.dst_ip.hostAddress) {//接受
                        dataReceived(pk.data)
                    } else {
                        try {
                            dataSent(pk.data)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                //pk.dst_ip
            }
        }
    }

    fun dataReceived(data: ByteArray) {
        if (!debugStarted) {
            return
        }

        packetReceived(ServerPacket.ofByteArray(data))
    }

    fun packetReceived(packet: ServerPacket) {
        when (packet) {
            is ServerTouchResponsePacket.Encrypted -> packetReceived(packet.decrypt())
            is ServerTouchResponsePacket -> {
                if (packet.serverIP == null) {
                    loginTime = packet.loginTime
                    loginIp = packet.loginIP
                    token0825 = packet.token0825
                }

                //then send 08 36 31 03
            }

            is ServerLoginResponseFailedPacket -> {
                println("login failed")
            }

            is ServerLoginResponseKeyExchangePacket.Encrypted -> packetReceived(packet.decrypt(privateKey))
            is ServerLoginResponseVerificationCodeInitPacket.Encrypted -> packetReceived(packet.decrypt())
            is ServerLoginResponseSuccessPacket.Encrypted -> packetReceived(packet.decrypt(privateKey))

            is ServerLoginResponseKeyExchangePacket -> {
                privateKey = packet.privateKey
                //then 31 04 or 31 06
            }

            is ServerLoginResponseSuccessPacket -> {
                sessionResponseDecryptionKey = packet.sessionResponseDecryptionKey
            }

            is ServerSessionKeyResponsePacket.Encrypted -> packetReceived(packet.decrypt(sessionResponseDecryptionKey))

            is ServerSessionKeyResponsePacket -> {
                sessionKey = packet.sessionKey
                println("Got sessionKey=" + sessionKey.toUHexString())
            }

            else -> {
            }
        }
    }

    @Volatile
    private var debugStarted = false

    private val qq: Int = 1994701021
    private val password: String = "xiaoqqq"

    lateinit var token0825: ByteArray//56
    var loginTime: Int = 0
    lateinit var loginIp: String
    lateinit var privateKey: ByteArray//16
    lateinit var sessionKey: ByteArray

    lateinit var sessionResponseDecryptionKey: ByteArray

    fun dataSent(data: ByteArray) {
        //println("Sent:     " + data.toUByteArray().toUHexString())

        lazyDecode(data.cutTail(1)) {
            it.skip(3)
            val idHex = it.readNBytes(4).toUHexString()
            println("qq=" + it.readUInt())
            println(idHex)
            when (idHex.substring(0, 5)) {
                "08 25" -> {
                    debugStarted = true
                    println("Detected touch, debug start!!")
                }

                "08 36" -> {
                    println(data.toUHexString())
                    println("tim的 passwordSubmissionKey1 = " + it.readNBytes(TIMProtocol.passwordSubmissionTLV1.hexToBytes().size).toUHexString())
                    //it.skipHex(Protocol.passwordSubmissionKey1)
                    println(it.readNBytes(2).toUHexString())
                    println("tim的 publicKey = " + it.readNBytes(TIMProtocol.publicKey.hexToBytes().size).toUHexString())
                    println(it.readNBytes(2).toUHexString())
                    println("tim的 key0836=" + it.readLVByteArray().toUHexString())
                    //it.skipHex(Protocol.key0836)
                    val encrypted = it.readAllBytes()
                    println(encrypted.size)
                    println(encrypted.toUHexString())
                    val tlv0006data = lazyDecode(encrypted.decryptBy(TIMProtocol.shareKey)) { section ->
                        section.skip(2 + 2 + 56 + 2)
                        section.skip(section.readShort())//device name
                        section.skip(6 + 4 + 2 + 2)

                        //tlv0006, encrypted by pwd md5
                        section.readNBytes(160).decryptBy(lazyEncode { md5(md5(password) + "00 00 00 00".hexToBytes() + qq.toUInt().toByteArray()) })
                    }
                    lazyDecode(tlv0006data) { tlv0006 ->
                        tlv0006.skip(4 + 2 + 4)
                        tlv0006.skipHex(TIMProtocol.constantData2)
                        tlv0006.skip(3)
                        tlv0006.skip(16 + 4 + 1 + 4 * 3 + 4 + 8 + 2)
                        tlv0006.skipHex("15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B")
                        privateKey = tlv0006.readNBytes(16)
                    }
                    println("Got privateKey=" + privateKey.toUHexString())

                    //then receive
                }
                else -> {
                }
            }
        }
    }

    private fun ByteArray.decryptBy(key: ByteArray): ByteArray = TEA.decrypt(this, key)

    private fun ByteArray.decryptBy(key: String): ByteArray = TEA.decrypt(this, key)


    private fun DataInputStream.skipHex(uHex: String) {
        this.skip(uHex.hexToBytes().size.toLong())
    }
}

val shareKeyFromCS = "60 42 3B 51 C3 B1 F6 0F 67 E8 9C 00 F0 A7 BD A3"

fun main() {
    val data = "76 AF AE 95 EB 89 BE B5 1C 83 D2 87 23 3B 5A 3B 6B 4C 78 AD F9 93 86 CA 13 D7 86 B5 0C D1 84 FB 2B ED 59 26 42 3B E0 6F 1A 91 A5 98 91 20 25 3F 6D C0 F6 FC 27 3D F8 34 EA 50 95 8C 2A BB 22 73 BD 76 60 2A 6B 68 51 07 4A 2F 37 6D 97 42 51 C5 14 47 96 3A A9 6B 8F 66 F8 D4 F4 52 22 13 D5 CC 9F B1 B4 06 BC 4B 35 B6 CF D8 CB 70 0F 0C E6 AA D9 12 E9 A2 C7 7F D8 24 7E 1B 2D 97 67 DA 34 0A FD 8E 44 D3 58 50 0D F0 0A 20 08 0A 46 28 68 0A 06 17 36 84 94 2C 97 2A 22 32 7B 01 67 3F E4 90 71 88 B2 F9 7B 7B AC 1A 00 CD 54 4A D7 AE 71 68 B3 FB E5 F3 94 9A C2 A1 C3 CA A5 4E AB 2C B0 78 AD EE 63 3F E6 24 6E AC 31 A5 00 F4 DB C7 4B 65 44 7B 92 87 30 7D 73 B3 21 81 C8 99 33 06 65 28 0C 98 56 EF 41 DC 64 79 55 69 AD B7 F4 A4 CF 4A 28 4B 3B E3 5A 2B C1 72 20 95 D9 8E 9F 1E A5 DE 9A DD 39 0B BE 76 A8 BE 95 9D 7C C2 C5 A8 3A D3 76 B6 D4 ED 15 34 5D 3C 8E 96 C6 93 64 78 A1 89 78 DA F8 17 E5 96 75 5F B6 97 FC 41 18 A4 54 67 BA 3B ED 97 27 B7 E3 90 81 1E DC 8D 17 25 46 2D 08 0D BB 95 D0 CB C8 9B 78 36 2D 70 E3 C6 4C 21 E9 C0 02 69 3B C5 F7 91 6B 62 D8 E4 10 F0 01 5B 7F 1A 3E 9F 1A D4 D3 A9 2B 4A C2 BD 6D 8B B0 0A AE A4 E9 72 71 F4 39 28 CE 18 42 ED FD BB 61 08 B1 95 93 8E F6 29 D7 B6 CB 15 2A AA AF A7 81 AD DF 3B D5 3F 47 29 AB 61 0C 86 48 82 93 AE 8C 2C 32 CC 83 83 68 08 C6 9D 10 81 82 BA 92 24 0E ED 71 B1 83 E1 08 D0 01 BB DF E2 26 D0 20 DF 8C 95 E1 A6 42 C2 A2 E7 85 00 E6 AA 54 A8 0C 5D BB 8D 46 37 AD 47 88 38 B9 D7 3B 48 13 13 81 3B A5 05 4D 32 24 A4 CE 08 73 6D 89 FD 6D CC F5 AB 8B 6A 39 4B 9D 30 33 73 F1 01 7F E4 43 03 72 44 67 3A 24 28 40 51 2B EB 48 EB F9 05 A9 3C 20 EB 4D B7 45 56 D3 4E BD A0 B5 40 65 D1 16 57 73 A4 81 B1 A6 8C 3F 68 28 AA EB 83".hexToBytes()
    println(TEA.decrypt(data, TIMProtocol.shareKey.hexToBytes()))

}


/*
00 19
tim的 publicKey = 02 F4 07 37 2D F1 82 1D 45 E8 30 14 41 74 AF E3 03 AB 29 D7 82 D9 E2 E5 89
00 00
tim的 key0836=70 BE 41 20 3A FA 05 B2 2D 66 2E 29 33 55 99 7E
552
76 AF AE 95 EB 89 BE B5 1C 83 D2 87 23 3B 5A 3B 6B 4C 78 AD F9 93 86 CA 13 D7 86 B5 0C D1 84 FB 2B ED 59 26 42 3B E0 6F 1A 91 A5 98 91 20 25 3F 6D C0 F6 FC 27 3D F8 34 EA 50 95 8C 2A BB 22 73 BD 76 60 2A 6B 68 51 07 4A 2F 37 6D 97 42 51 C5 14 47 96 3A A9 6B 8F 66 F8 D4 F4 52 22 13 D5 CC 9F B1 B4 06 BC 4B 35 B6 CF D8 CB 70 0F 0C E6 AA D9 12 E9 A2 C7 7F D8 24 7E 1B 2D 97 67 DA 34 0A FD 8E 44 D3 58 50 0D F0 0A 20 08 0A 46 28 68 0A 06 17 36 84 94 2C 97 2A 22 32 7B 01 67 3F E4 90 71 88 B2 F9 7B 7B AC 1A 00 CD 54 4A D7 AE 71 68 B3 FB E5 F3 94 9A C2 A1 C3 CA A5 4E AB 2C B0 78 AD EE 63 3F E6 24 6E AC 31 A5 00 F4 DB C7 4B 65 44 7B 92 87 30 7D 73 B3 21 81 C8 99 33 06 65 28 0C 98 56 EF 41 DC 64 79 55 69 AD B7 F4 A4 CF 4A 28 4B 3B E3 5A 2B C1 72 20 95 D9 8E 9F 1E A5 DE 9A DD 39 0B BE 76 A8 BE 95 9D 7C C2 C5 A8 3A D3 76 B6 D4 ED 15 34 5D 3C 8E 96 C6 93 64 78 A1 89 78 DA F8 17 E5 96 75 5F B6 97 FC 41 18 A4 54 67 BA 3B ED 97 27 B7 E3 90 81 1E DC 8D 17 25 46 2D 08 0D BB 95 D0 CB C8 9B 78 36 2D 70 E3 C6 4C 21 E9 C0 02 69 3B C5 F7 91 6B 62 D8 E4 10 F0 01 5B 7F 1A 3E 9F 1A D4 D3 A9 2B 4A C2 BD 6D 8B B0 0A AE A4 E9 72 71 F4 39 28 CE 18 42 ED FD BB 61 08 B1 95 93 8E F6 29 D7 B6 CB 15 2A AA AF A7 81 AD DF 3B D5 3F 47 29 AB 61 0C 86 48 82 93 AE 8C 2C 32 CC 83 83 68 08 C6 9D 10 81 82 BA 92 24 0E ED 71 B1 83 E1 08 D0 01 BB DF E2 26 D0 20 DF 8C 95 E1 A6 42 C2 A2 E7 85 00 E6 AA 54 A8 0C 5D BB 8D 46 37 AD 47 88 38 B9 D7 3B 48 13 13 81 3B A5 05 4D 32 24 A4 CE 08 73 6D 89 FD 6D CC F5 AB 8B 6A 39 4B 9D 30 33 73 F1 01 7F E4 43 03 72 44 67 3A 24 28 40 51 2B EB 48 EB F9 05 A9 3C 20 EB 4D B7 45 56 D3 4E BD A0 B5 40 65 D1 16 57 73 A4 81 B1 A6 8C 3F 68 28 AA EB 83
 */

