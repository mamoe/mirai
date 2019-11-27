@file:Suppress("EXPERIMENTAL_API_USAGE", "MemberVisibilityCanBePrivate", "EXPERIMENTAL_UNSIGNED_LITERALS")

import PacketDebugger.dataSent
import PacketDebugger.qq
import PacketDebugger.sessionKey
import kotlinx.coroutines.*
import kotlinx.io.core.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.BotSession
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.handler.DataPacketSocketAdapter
import net.mamoe.mirai.network.protocol.tim.handler.TemporaryPacketHandler
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.login.CaptchaKey
import net.mamoe.mirai.network.protocol.tim.packet.login.LoginResult
import net.mamoe.mirai.network.protocol.tim.packet.login.ShareKey
import net.mamoe.mirai.network.protocol.tim.packet.login.TouchKey
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DecryptionFailedException
import net.mamoe.mirai.utils.decryptBy
import net.mamoe.mirai.utils.io.*
import org.pcap4j.core.BpfProgram.BpfCompileMode
import org.pcap4j.core.PacketListener
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode
import org.pcap4j.core.Pcaps
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.io.use

/**
 * 避免 print 重叠. 单线程处理足够调试
 */
val DISPATCHER = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

private fun listenDevice(localIp: String, device: PcapNetworkInterface) {
    val sender = device.openLive(65536, PromiscuousMode.PROMISCUOUS, 10)
    thread {
        sender.setFilter("src $localIp && udp port 8000", BpfCompileMode.OPTIMIZE)
        try {
            sender.loop(Int.MAX_VALUE, PacketListener {
                runBlocking {
                    withContext(DISPATCHER) {
                        try {
                            dataSent(it.rawData.drop(42).toByteArray())
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    val receiver = device.openLive(65536, PromiscuousMode.PROMISCUOUS, 10)
    thread {
        receiver.setFilter("dst $localIp && udp port 8000", BpfCompileMode.OPTIMIZE)
        try {
            receiver.loop(Int.MAX_VALUE, PacketListener {
                runBlocking {
                    withContext(DISPATCHER) {
                        try {
                            PacketDebugger.dataReceived(it.rawData.drop(42).toByteArray())
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

fun main() {
    /*
    check(System.getProperty("os.arch") == "x86") { "Jpcap can only work with x86 JDK" }

    JpcapCaptor.getDeviceList().forEach {
        println(it)
    }
    JpcapCaptor.openDevice(JpcapCaptor.getDeviceList()[0], 65535, true, 1000).loopPacket(Int.MAX_VALUE) {
        println(it)
    }*/
    val localIp = Pcaps.findAllDevs()[0].addresses[0].address.hostAddress
    println("localIp = $localIp")
    Pcaps.findAllDevs().forEach {
        listenDevice(localIp, it)
    }
    println("Ready perfectly")
}

/**
 * 抓包分析器.
 * 设置好 [sessionKey], 和 [qq] 后运行即可开始抓包和自动解密
 *
 * @author Him188moe
 */
object PacketDebugger {
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
    val sessionKey: SessionKey = SessionKey("10 52 CD 27 0B A5 9C 74 1B A2 15 7E 41 19 0C 7B".hexToBytes())
    const val qq: UInt = 761025446u

    val IgnoredPacketIdList: List<PacketId> = listOf(
        KnownPacketId.FRIEND_ONLINE_STATUS_CHANGE,
        KnownPacketId.CHANGE_ONLINE_STATUS,
        KnownPacketId.HEARTBEAT
    )

    suspend fun dataReceived(data: ByteArray) {
        //println("raw = " + data.toUHexString())
        data.read {
            discardExact(3)
            val id = matchPacketId(readUShort())
            val sequenceId = readUShort()
            if (id == KnownPacketId.HEARTBEAT || readUInt() != qq)
                return@read

            if (IgnoredPacketIdList.contains(id)) {
                return
            }


            println("--------------")

            discardExact(3)//0x00 0x00 0x00. 但更可能是应该 discard 8
            println(
                "接收包id=$id, " +
                        "\nsequence=${sequenceId.toUHexString()}"
            )
            // val remaining = this.readRemainingBytes().cutTail(1)
            try {
                val packet = use {
                    with(id.factory) {
                        provideDecrypter(id.factory)
                            .decrypt(this@read.readRemainingBytes().let { ByteReadPacket(it, 0, it.size - 1) })
                            .let {
                                it.debugPrint("  解密body", it.remaining)
                            }
                            .decode(id, sequenceId, DebugNetworkHandler)
                    }
                }

                handlePacket(id, sequenceId, packet, id.factory)
            } catch (e: DecryptionFailedException) {
                // println("密文body=" + remaining.toUHexString())
                println("  解密body=解密失败")
            }
        }
    }

    internal fun ByteReadPacket.debugPrint(name: String = "", length: Long = this.remaining): ByteReadPacket {
        val bytes = this.readBytes(length.toInt())
        println("$name=" + bytes.toUHexString())
        return bytes.toReadPacket()
    }

    /**
     * 提供解密密匙. 无需修改
     */
    @Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
    internal fun <D : Decrypter> provideDecrypter(factory: PacketFactory<*, D>): D =
        when (factory.decrypterType) {
            TouchKey -> TouchKey
            CaptchaKey -> CaptchaKey
            ShareKey -> ShareKey

            NoDecrypter -> NoDecrypter

            SessionKey -> sessionKey

            else -> error("No decrypter is found")
        } as? D ?: error("Internal error: could not cast decrypter which is found for factory to class Decrypter")

    /**
     * 处理一个包
     */
    @Suppress("UNUSED_PARAMETER")
    fun <TPacket : Packet> handlePacket(
        id: PacketId,
        sequenceId: UShort,
        packet: TPacket,
        factory: PacketFactory<TPacket, *>
    ) {
        println("  解析body=$packet")
        return
    }

    fun dataSent(rawPacket: ByteArray) = rawPacket.cutTail(1).read {

        // 02 38 03
        // 03 52 78 9F
        // 3E 03 3F A2 04 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 00 00 00

        // 02 38 03
        // 01 BD 63 D6
        // 3E 03 3F A2 02 00 00 00 01 2E 01 00 00 69 35

        discardExact(3)//head
        val id = matchPacketId(readUShort())
        val sequence = readUShort().toUHexString()
        if (IgnoredPacketIdList.contains(id)) {
            return
        }
        if (readUInt() != qq) {
            return@read
        }
        println("---------------------------")
        println("发出包ID = $id")
        println("sequence = $sequence")

        println(
            "  fixVer2=" + when (val flag = readByte().toInt()) {
                2 -> byteArrayOf(2) + readBytes(TIMProtocol.fixVer2.size - 1)
                3 -> byteArrayOf(3) + readBytes(TIMProtocol.fixVer2.size - 1 + 4)//4 个0
                4 -> byteArrayOf(4) + readBytes(TIMProtocol.fixVer2.size - 1 + 8)//8 个 0
                0 -> byteArrayOf(0) + readBytes(2)
                else -> error("unknown fixVer2 flag=$flag. Remaining =${readBytes().toUHexString()}")
            }.toUHexString()
        )

        //39 27 DC E2 04 00 00 00 00 00 00 00 1E 0E 89 00 00 01 05 0F 05 0F 00 00 00 00 00 00 00 00 00 00 00 00 00 3E 03 3F A2 00 00 00 00 00 00 00 00 00 00 00

        val encryptedBody = readRemainingBytes()
        try {
            println("  解密body=${encryptedBody.decryptBy(sessionKey.value).toUHexString()}")
        } catch (e: DecryptionFailedException) {
            println("  密文=" + encryptedBody.toUHexString())
            println("  解密body=解密失败")
        }

        encryptedBody.read {

            /*
when (idHex.substring(0, 5)) {
   "00 CD" -> {
       println("好友消息")

       val raw = readRemainingBytes()
       //println("解密前数据: " + raw.toUHexString())
       val messageData = raw.decryptBy(sessionKey.value)
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
   }*/

            /*
            "03 88" -> {
                println("0388上传图片-获取图片ID")
                discardExact(8)

                //val body = readRemainingBytes().decryptBy(sessionKey)
                //println(body.toUHexString())
            }
        }*/
        }

    }
}


internal object DebugNetworkHandler : BotNetworkHandler<DataPacketSocketAdapter>, CoroutineScope {
    override val socket: DataPacketSocketAdapter = object : DataPacketSocketAdapter {
        override val serverIp: String
            get() = ""
        override val channel: PlatformDatagramChannel
            get() = error("UNSUPPORTED")
        override val isOpen: Boolean
            get() = true

        override suspend fun sendPacket(packet: OutgoingPacket) {

        }

        override fun close() {
        }

        override val owner: Bot
            get() = bot

    }
    override val bot: Bot = Bot(qq, "")
    override val session = BotSession(bot, sessionKey, socket, this)

    override suspend fun login(configuration: BotConfiguration): LoginResult = LoginResult.SUCCESS

    override suspend fun addHandler(temporaryPacketHandler: TemporaryPacketHandler<*, *>) {
    }

    override suspend fun sendPacket(packet: OutgoingPacket) {
    }

    override suspend fun awaitDisconnection() {
    }

    override val coroutineContext: CoroutineContext
        get() = GlobalScope.coroutineContext
}