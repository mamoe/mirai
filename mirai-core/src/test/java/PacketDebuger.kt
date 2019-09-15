@file:Suppress("EXPERIMENTAL_API_USAGE")

import jpcap.JpcapCaptor
import jpcap.packet.IPPacket
import jpcap.packet.UDPPacket
import net.mamoe.mirai.network.Protocol
import net.mamoe.mirai.network.packet.*
import net.mamoe.mirai.network.packet.login.ServerLoginResponseFailedPacket
import net.mamoe.mirai.network.packet.login.ServerLoginResponseKeyExchangePacket
import net.mamoe.mirai.network.packet.login.ServerLoginResponseSuccessPacket
import net.mamoe.mirai.network.packet.login.ServerLoginResponseVerificationCodeInitPacket
import net.mamoe.mirai.utils.*
import java.io.DataInputStream

/**
 * 模拟登录并抓取到 session key
 *
 * @author Him188moe
 */
object Main {
    val localIp = "192.168.3.10"

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

        var jpcap: JpcapCaptor? = null
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

            is ServerLoginResponseKeyExchangePacket.Encrypted -> packetReceived(packet.decrypt(tgtgtKey))
            is ServerLoginResponseVerificationCodeInitPacket.Encrypted -> packetReceived(packet.decrypt())
            is ServerLoginResponseSuccessPacket.Encrypted -> packetReceived(packet.decrypt(tgtgtKey))

            is ServerLoginResponseKeyExchangePacket -> {
                tgtgtKey = packet.tgtgtKey
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

    private val qq: Int = 1040400290
    private val password: String = "asdHim188moe"

    lateinit var token0825: ByteArray//56
    var loginTime: Int = 0
    lateinit var loginIp: String
    lateinit var tgtgtKey: ByteArray//16
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
                    println("tim的 passwordSubmissionKey1 = " + it.readNBytes(Protocol.passwordSubmissionTLV1.hexToBytes().size).toUHexString())
                    //it.skipHex(Protocol.passwordSubmissionKey1)
                    println(it.readNBytes(2).toUHexString())
                    println("tim的 publicKey = " + it.readNBytes(Protocol.publicKey.hexToBytes().size).toUHexString())
                    println(it.readNBytes(2).toUHexString())
                    println("tim的 key0836=" + it.readLVByteArray().toUHexString())
                    //it.skipHex(Protocol.key0836)
                    val encrypted = it.readAllBytes()
                    println(encrypted.size)
                    println(encrypted.toUHexString())
                    val tlv0006data = lazyDecode(encrypted.decryptBy(Protocol.shareKey)) { section ->
                        section.skip(2 + 2 + 56 + 2)
                        section.skip(section.readShort())//device name
                        section.skip(6 + 4 + 2 + 2)

                        //tlv0006, encrypted by pwd md5
                        section.readNBytes(160).decryptBy(lazyEncode { md5(md5(password) + "00 00 00 00".hexToBytes() + qq.toUInt().toByteArray()) })
                    }
                    lazyDecode(tlv0006data) { tlv0006 ->
                        tlv0006.skip(4 + 2 + 4)
                        tlv0006.skipHex(Protocol.constantData2)
                        tlv0006.skip(3)
                        tlv0006.skip(16 + 4 + 1 + 4 * 3 + 4 + 8 + 2)
                        tlv0006.skipHex("15 74 C4 89 85 7A 19 F5 5E A9 C9 A3 5E 8A 5A 9B")
                        tgtgtKey = tlv0006.readNBytes(16)
                    }
                    println("Got tgtgtKey=" + tgtgtKey.toUHexString())

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

fun main() {
    val data = "FE A1 06 1C 5F 04 04 F6 E2 F9 B2 A7 48 51 A2 81 4D 92 62 7E 67 29 F8 02 A5 77 97 A5 8F F9 81 1D B3 D9 3D DB 63 5C 22 2F E1 C2 53 67 E8 CD A5 BD AB 5A FB B3 14 48 6C 0D DD 67 EE AC EB A8 08 96 28 A0 20 9F D9 52 B7 DC E5 71 18 68 58 4F E3 31 7E 74 15 A2 3E 4D 11 CA D1 7A 59 D1 EA 8C A0 18 54 E7 4D ED CC D6 4C E3 34 43 3F 20 41 93 94 9D 11 F4 51 8E 5A 3A EA A4 5B EA 69 64 AE 4F DA 16 50 89 93 82 EA B3 DB 68 80 A5 10 78 94 16 7C BC 74 C0 D0 03 C7 BA 33 BD A5 BF 3A 90 B4 FB 66 7E 54 C7 3F A4 42 BC 72 60 A9 4F F0 7A 64 E5 BB AD 59 8B E7 48 0D 0E 5A 58 99 17 77 35 52 C9 28 67 77 81 6B 7F 6F D5 CF 12 DC 31 82 39 E9 F9 6D 91 A6 C7 60 A0 3C 7C 80 29 E9 2E 05 63 BC 59 B0 73 D8 0F 84 E9 D1 88 AC 99 B8 E4 DA 8F 8F E6 F5 06 29 E8 CD 8A A8 38 24 BD 4E BF E6 79 79 9B 91 9E 16 44 FD 87 3B 6E 69 14 AF 32 A0 6E AD AF 5A C8 45 64 F3 4C 3B 20 AA 20 16 A7 FA FF D1 F2 A8 78 5F DE D5 FF 37 76 73 73 52 73 91 32 0D 1C 35 4E 8A 21 29 C2 D7 87 55 B3 6D 65 F6 ED 6D 9E 6A 9E DC 46 6A F9 CC 38 09 72 7A B8 84 D1 4C 76 8B CB 2E AA 05 2A B3 31 0C F3 70 2B 34 70 7F BC 5D 8E 65 4E 91 16 77 CB 7A 07 CE 37 CF 42 D0 99 C6 14 5A 11 B1 7D 1C 7B 9B F4 31 FE 91 0C B0 FD 7B 9D 4B 9D D7 34 CC 1B F3 E0 ED 5B BC 71 D9 D5 D5 A8 83 A9 3E BF 2F A6 90 FB 51 9F 72 CC 0C A5 36 A6 05 55 0C 3F 93 6C 0F DF EA 43 E1 F3 51 10 02 5D 75 F0 83 C6 BD 06 21 6B 07 D6 6E 3A CB 20 21 60 89 3A 77 0E EB 86 F7 45 BE B8 54 5C 3A 45 3A 86 19 A9 75 E6 9C 50 3D 36 F1 51 1E B5 97 41 86 CF F0 6F 0C 0F 7E CF E4 E3 50 F2 6A 19 0A A2 CB 74 88 8C D6 62 EC EC 66 1F 87 D3 6F 1C 83 94 79 CE C9 15 66 07 12 AE A7 9A D9 D1 F2 90 F8 56 28 E7 6E 33 AF 8D 58 3D 8A 7C 49 94 A0 E8 8B 48 77 89 B6 78 13 44 5C A0 D9 A5".hexToBytes()
    println(TEA.decrypt(data, "E4 23 72 92 79 9C 9C 96 28 9D AF 5C 1D 33 D2 7F".hexToBytes()))

}