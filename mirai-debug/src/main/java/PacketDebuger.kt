@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "EXPERIMENTAL_UNSIGNED_LITERALS")

import Main.dataReceived
import Main.dataSent
import Main.localIp
import Main.qq
import Main.sessionKey
import kotlinx.coroutines.*
import kotlinx.io.core.discardExact
import kotlinx.io.core.readBytes
import kotlinx.io.core.readUInt
import net.mamoe.mirai.message.internal.readMessageChain
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.ServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.UnknownServerPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.event.UnknownServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.idHexString
import net.mamoe.mirai.utils.DecryptionFailedException
import net.mamoe.mirai.utils.decryptBy
import net.mamoe.mirai.utils.io.*
import org.pcap4j.core.BpfProgram.BpfCompileMode
import org.pcap4j.core.PacketListener
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode
import org.pcap4j.core.Pcaps

suspend fun main() {
    val nif: PcapNetworkInterface = Pcaps.findAllDevs()[0]
    println(nif.name + "(" + nif.addresses + ")")

    val sender = nif.openLive(65536, PromiscuousMode.PROMISCUOUS, 3000)
    val sendListener = GlobalScope.launch {
        sender.setFilter("src $localIp && udp port 8000", BpfCompileMode.OPTIMIZE)
        withContext(Dispatchers.IO) {
            sender.loop(Int.MAX_VALUE, PacketListener {
                dataSent(it.rawData.drop(42).toByteArray())
            })
        }
    }

    val receiver = nif.openLive(65536, PromiscuousMode.PROMISCUOUS, 3000)
    val receiveListener = GlobalScope.launch {
        receiver.setFilter("dst $localIp && udp port 8000", BpfCompileMode.OPTIMIZE)
        withContext(Dispatchers.IO) {
            receiver.loop(Int.MAX_VALUE, PacketListener {
                dataReceived(it.rawData.drop(42).toByteArray())
            })
        }
    }

    joinAll(sendListener, receiveListener)
}

/**
 * 抓包分析器.
 * 设置好 [sessionKey], [localIp] 和 [qq] 后运行即可开始抓包和自动解密
 *
 * @author Him188moe
 */
object Main {
    /**
     * 会话密匙, 用于解密数据.
     * 在一次登录中会话密匙不会改变.
     *
     * 从 TIM 内存中读取, windows 方法:
     * 1. x32dbg 附加 TIM
     * 2. `符号` 中找到 common.dll
     * 3. 搜索函数 `oi_symmetry_encrypt2` (TEA 加密函数)
     * 4. 双击跳转
     * 5. 设置断点
     * 6. 在 TIM 发送一条消息触发断点
     * 7. 运行完 `mov eax,dword ptr ss:[ebp+10]`
     * 8. 查看内存, `eax` 到 `eax+10` 的 16 字节就是 `sessionKey`
     */
    val sessionKey: ByteArray = "98 BA DE 20 53 AB EC B5 F5 50 26 E9 6D 41 B6 05".hexToBytes()
    const val qq: UInt = 1040400290u
    const val localIp = "192.168.3.10"

    fun dataReceived(data: ByteArray) {
        //println("raw = " + data.toUHexString())
        data.read {
            discardExact(3)
            val idHex = readInt().toUHexString(" ")
            if (idHex.startsWith("00 81")) {
                return@read
            }
            if (readUInt() != qq) {
                return@read
            }
            println("--------------")
            println("接收数据包")

            discardExact(3)//0x00 0x00 0x00. 但更可能是应该 discard 8
            println("id=$idHex")
            val remaining = this.readRemainingBytes().cutTail(1)
            try {
                val decrypted = remaining.decryptBy(sessionKey)
                println("解密body=${decrypted.toUHexString()}")

                packetReceived(data.read { parseServerPacket(data.size) })
            } catch (e: DecryptionFailedException) {
                println("密文body=" + remaining.toUHexString())
                println("解密body=解密失败")
            }
        }
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

        // 02 38 03
        // 03 52 78 9F
        // 3E 03 3F A2 04 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 00 00 00

        // 02 38 03
        // 01 BD 63 D6
        // 3E 03 3F A2 02 00 00 00 01 2E 01 00 00 69 35

        println("---------------------------")
        discardExact(3)//head
        val idHex = readBytes(4).toUHexString()
        println("发出包ID = $idHex")
        if (readUInt() != qq) {
            return@read
        }

        println(
            "fixVer2=" + when (val flag = readByte().toInt()) {
                2 -> byteArrayOf(2) + readBytes(TIMProtocol.fixVer2.hexToBytes().size - 1)
                4 -> byteArrayOf(4) + readBytes(TIMProtocol.fixVer2.hexToBytes().size - 1 + 8)//8个0
                0 -> byteArrayOf(0) + readBytes(2)
                else -> error("unknown fixVer2 flag=$flag. Remaining =${readBytes().toUHexString()}")
            }.toUHexString()
        )

        //39 27 DC E2 04 00 00 00 00 00 00 00 1E 0E 89 00 00 01 05 0F 05 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 3E 03 3F A2 00 00 00 00 00 00 00 00 00 00 00

        val encryptedBody = readRemainingBytes()
        try {
            println("解密body=${encryptedBody.decryptBy(sessionKey).toUHexString()}")
        } catch (e: DecryptionFailedException) {
            println("密文=" + encryptedBody.toUHexString())
            println("解密body=解密失败")
        }

        encryptedBody.read {

            when (idHex.substring(0, 5)) {
                "00 CD" -> {
                    println("好友消息")

                    val raw = readRemainingBytes()
                    //println("解密前数据: " + raw.toUHexString())
                    val messageData = raw.decryptBy(sessionKey)
                    //println("解密结果: " + messageData.toUHexString())
                    println("尝试解消息")

                    try {
                        messageData.read {
                            discardExact(
                                4 + 4 + 12 + 2 + 4 + 4 + 16 + 2 + 2 + 4 + 2 + 16 + 4 + 4 + 7 + 15 + 2
                                        + 1
                            )
                            val chain = readMessageChain()
                            println(chain)
                        }
                    } catch (e: Exception) {
                        println("失败")
                    }
                }

                "03 88" -> {
                    println("0388上传图片-获取图片ID")
                    discardExact(8)

                    //val body = readRemainingBytes().decryptBy(sessionKey)
                    //println(body.toUHexString())
                }
            }
        }

    }
}