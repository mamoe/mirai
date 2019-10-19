@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate")

import jpcap.JpcapCaptor
import jpcap.packet.IPPacket
import jpcap.packet.UDPPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.UnknownServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.UnknownServerEventPacket
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
        val devices = JpcapCaptor.getDeviceList()
        val jpcap: JpcapCaptor?
        val caplen = 4096
        val promiscCheck = true
        jpcap = JpcapCaptor.openDevice(devices[0], caplen, promiscCheck, 50)
        while (true) {
            assert(jpcap != null)
            val pk = jpcap!!.packet
            if (pk is IPPacket && pk.version.toInt() == 4) {

                if (pk is UDPPacket) {
                    if (pk.dst_port != 8000 && pk.src_port != 8000) {
                        continue
                    }

                    if (localIp in pk.dst_ip.hostAddress) {//接受
                        try {
                            dataReceived(pk.data)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        try {
                            dataSent(pk.data)
                            println()
                        } catch (e: Throwable) {
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
    val sessionKey: ByteArray = "99 91 B9 8B 79 45 FD CF 51 4A B9 DE 14 61 ED E3".hexToBytes()

    fun dataReceived(data: ByteArray) {
        println("--------------")
        println("接收数据包")
        //println("raw = " + data.toUHexString())
        data.read {
            discardExact(3)
            val idHex = readInt().toUHexString(" ")
            discardExact(7)//4 for qq number, 3 for 0x00 0x00 0x00. 但更可能是应该 discard 8
            println("id=$idHex")
            println("解密body=${this.readRemainingBytes().cutTail(1).decryptBy(sessionKey).toUHexString()}")
        }

        packetReceived(data.read { this.parseServerPacket(data.size) })
    }

    fun packetReceived(packet: ServerPacket) {
        when (packet) {
            is ServerEventPacket.Raw.Encrypted -> {
                packetReceived(packet.decrypt(sessionKey))
            }

            is ServerEventPacket.Raw -> packetReceived(packet.distribute())

            is UnknownServerEventPacket -> {
                println("--------------")
                println("未知事件ID=" + packet.idHexString)
                println("未知事件: " + packet.input.readBytes().toUHexString())
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

    fun dataSent(rawPacket: ByteArray) = rawPacket.cutTail(1).read {
        println("---------------------------")
        discardExact(3)//head
        val idHex = readBytes(4).toUHexString()
        println("发出包ID = $idHex")
        discardExact(TIMProtocol.fixVer2.hexToBytes().size + 1 + 5 - 3 + 1)

        val encryptedBody = readRemainingBytes()
        println("解密body = ${encryptedBody.decryptBy(sessionKey).toUHexString()}")

        encryptedBody.read {

            when (idHex.substring(0, 5)) {
                "00 CD" -> {
                    println("好友消息")

                    val raw = readRemainingBytes()
                    println("解密前数据: " + raw.toUHexString())
                    val messageData = raw.decryptBy(sessionKey)
                    println("解密结果: " + messageData.toUHexString())
                    println("尝试解消息")
                    messageData.read {
                        discardExact(
                                4 + 4 + 12 + 2 + 4 + 4 + 16 + 2 + 2 + 4 + 2 + 16 + 4 + 4 + 7 + 15 + 2
                                        + 1
                        )
                        val chain = readMessageChain()
                        println(chain)
                    }
                }

                "03 88" -> {
                    println("上传图片-获取图片ID")
                    discardExact(8)
                    val body = readRemainingBytes().decryptBy(sessionKey)
                    println(body.toUHexString())
                }
            }
        }

    }
}

fun main() {
    println("00 01 00 23 24 8B 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 29 4E 22 4E 25 4E 26 4E 27 4E 29 4E 2A 4E 2B 4E 2D 4E 2E 4E 2F 4E 30 4E 31 4E 33 4E 35 4E 36 4E 37 4E 38 4E 3F 4E 40 4E 41 4E 42 4E 43 4E 45 4E 49 4E 4B 4E 4F 4E 54 4E 5B 52 0B 52 0F 5D C2 5D C8 65 97 69 9D 69 A9 9D A5 A4 91 A4 93 A4 94 A4 9C A4 B5".hexToBytes().stringOfWitch())
}

