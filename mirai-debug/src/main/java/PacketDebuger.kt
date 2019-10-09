@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate")

import jpcap.JpcapCaptor
import jpcap.packet.IPPacket
import jpcap.packet.UDPPacket
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.UnknownServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.UnknownServerPacket
import net.mamoe.mirai.utils.*

/**
 * 抓包分析器
 *
 * @author Him188moe
 */
object Main {
    const val localIp = "192.168.3."

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

        jpcap = JpcapCaptor.openDevice(devices[0], caplen, promiscCheck, 50)


        /*----------第二步抓包-----------------*/
        while (true) {
            assert(jpcap != null)
            val pk = jpcap!!.packet
            if (pk is IPPacket && pk.version.toInt() == 4) {

                if (pk is UDPPacket) {
                    if (pk.dst_port != 8000 && pk.src_port != 8000) {
                        continue
                    }

                    if (localIp in pk.dst_ip.hostAddress) {//接受
                        dataReceived(pk.data)
                    } else {
                        try {
                            dataSent(pk.data)
                            println()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                //pk.dst_ip
            }
        }
    }


    /**
     * 可从 TIM 内存中读取
     *
     * 方法:
     * 1. x32dbg 附加 TIM
     * 2. `符号` 中找到 common.dll
     * 3. 搜索函数 `oi_symmetry_encrypt2` (TEA 加密函数)
     * 4. 双击跳转
     * 5. 断点并在TIM发送消息以触发
     * 6. 运行到 `mov eax,dword ptr ss:[ebp+10]`
     * 7. 查看内存, 从 `eax` 开始的 16 bytes 便是 `sessionKey`
     */
    val sessionKey: ByteArray = "48 C0 11 42 2D FD 8F 36 6E BA BF FD D3 AA B7 AE".hexToBytes()

    fun dataReceived(data: ByteArray) {
        //println("--------------")
        //println("接收数据包")
        //println("raw packet = " + data.toUHexString())
        packetReceived(ServerPacket.ofByteArray(data))
    }

    fun packetReceived(packet: ServerPacket) {
        when (packet) {
            is ServerEventPacket.Raw.Encrypted -> {
                packetReceived(packet.decrypt(sessionKey))
            }

            is ServerEventPacket.Raw -> packetReceived(packet.distribute())

            is UnknownServerEventPacket -> {
                println("--------------")
                println("未知事件ID=" + packet.packetId.toUHexString())
                println("未知事件: " + packet.input.readAllBytes().toUHexString())
            }

            is ServerEventPacket -> {
                println("事件")
                println(packet)
            }

            is UnknownServerPacket -> {
                //ignore
            }

            else -> {
            }
        }
    }

    fun dataSent(rawPacket: ByteArray) = rawPacket.cutTail(1).decode { packet ->
        println("---------------------------")
        packet.skip(3)//head
        val idHex = packet.readNBytes(4).toUHexString()
        println("发出包ID = $idHex")
        packet.skip(TIMProtocol.fixVer2.hexToBytes().size + 1 + 5 - 3 + 1)

        val encryptedBody = packet.readAllBytes()
        println("body = ${encryptedBody.toUHexString()}")

        encryptedBody.decode { data ->

            when (idHex.substring(0, 5)) {
                "00 CD" -> {
                    println("好友消息")

                    val raw = data.readAllBytes()
                    println("解密前数据: " + raw.toUHexString())
                    val messageData = raw.decryptBy(sessionKey)
                    println("解密结果: " + messageData.toUHexString())
                    println("尝试解消息")
                    messageData.decode {
                        it.skip(
                                4 + 4 + 12 + 2 + 4 + 4 + 16 + 2 + 2 + 4 + 2 + 16 + 4 + 4 + 7 + 15 + 2
                                        + 1
                        )
                        val chain = it.readMessageChain()
                        println(chain)
                    }
                }

                "03 88" -> {
                    println("上传图片-获取图片ID")
                    data.skip(8)
                    val body = data.readAllBytes().decryptBy(sessionKey)
                    println(body.toUHexString())
                }
            }
        }

    }
}