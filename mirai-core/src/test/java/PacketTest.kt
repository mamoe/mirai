import net.mamoe.mirai.network.packet.client.toHexString
import net.mamoe.mirai.network.packet.server.touch.ServerTouchResponsePacket
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.toHexString
import java.io.DataInputStream

@ExperimentalUnsignedTypes
fun main(){
    val data = "00 37 13 08 25 31 01 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D C3 47 F0 25 A1 8E 74 EF 1E 0B 32 5B 20 8A FA 3B 0B 52 8F 86 E6 04 F1 D6 F8 63 75 60 8C 0C 7D 06 D1 E0 22 F8 49 EF AF 61 EE 7E 69 72 EB 10 08 30 69 50 1C 84 A9 C2 16 D7 52 B9 1C 79 CA 5A CF FD BC AE D8 A6 BB DC 21 6E 79 26 E1 A2 23 11 AA B0 9A 49 39 72 ED 61 12 B6 88 4D A2 56 23 E9 92 11 92 27 4A 70 00 C9 01 7B 03"
    val s = DataInputStream(data.hexToBytes().inputStream())
    val packet = ServerTouchResponsePacket(ServerTouchResponsePacket.Type.TYPE_08_25_31_01, s)
    packet.decode()
    System.out.println(packet.token.toUByteArray().toHexString(" "))
    System.out.println(packet.loginTime.toHexString(" "))
    System.out.println(packet.loginIP)
}