import net.mamoe.mirai.network.packet.server.login.ServerLoginResponseSuccessPacket
import net.mamoe.mirai.util.hexToBytes
import net.mamoe.mirai.util.toHexString
import java.io.DataInputStream

@ExperimentalUnsignedTypes
fun main(){
    /*
    val data = "00 37 13 08 25 31 01 EB 10 08 30 69 50 1C 84 A9 C2 16 D7 52 B9 1C 79 CA 5A CF FD BC EB 10 08 30 69 50 1C 84 A9 C2 16 D7 52 B9 1C 79 CA 5A CF FD BC AE D8 A6 BB DC 21 6E 79 26 E1 A2 23 11 AA B0 9A AE D8 A6 BB DC 21 6E 79 26 E1 A2 23 11 AA B0 9A 76 E4 B8 DD 03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D C3 47 F0 25 A1 8E 74 EF 1E 0B 32 5B 20 8A FA 3B 0B 52 8F 86 E6 04 F1 D6 F8 63 75 60 8C 0C 7D 06 D1 E0 22 F8 49 EF AF 61 EE 7E 69 72 EB 10 08 30 69 50 1C 84 A9 C2 16 D7 52 B9 1C 79 CA 5A CF FD BC AE D8 A6 BB DC 21 6E 79 26 E1 A2 23 11 AA B0 9A 49 39 72 ED 61 12 B6 88 4D A2 56 23 E9 92 11 92 27 4A 70 00 C9 01 7B 03";
    val s = DataInputStream(data.hexToBytes().inputStream())
    val packet = ServerTouchResponsePacket(ServerTouchResponsePacket.Type.TYPE_08_25_31_01, s)
    packet.decode()
    System.out.println(packet.token.toUByteArray().toHexString(" "))
    System.out.println(packet.loginTime.toHexString(" "))
    System.out.println(packet.loginIP)
    */

   // val packet = ClientPasswordSubmissionPacket(1994701021,"xiaoqqq",)

    val data = "00 01 09 00 70 00 01 5C 71 80 A6 BA 20 62 2E C1 BE BF F2 47 37 40 A1 00 38 91 25 85 58 18 D3 67 77 2C 4D 02 D8 66 A6 F7 3E 57 D8 CE 01 47 7F D0 8F 13 C8 3A E5 19 A2 60 BC 4C 9A 35 4E 92 9F 21 48 6C 67 68 36 6B 94 C1 6F 11 8D 55 6B 04 9A 22 C3 00 20 29 7E D4 A7 16 02 07 14 41 90 3A 65 06 AC CB 28 AB 90 DB 46 33 C9 C0 1D 06 44 7A 92 17 C3 A5 F3 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 01 07 01 D3 00 01 00 16 00 00 00 01 00 00 00 64 00 00 0D B6 00 09 3A 80 00 00 00 3C 00 02 2E 29 4E 47 5F 68 78 2C 47 25 5F 59 50 65 60 5D 00 88 00 04 5D 59 45 17 B7 5F F8 D4 00 00 00 00 00 78 38 E0 3B 23 4A C5 0E 93 CB C1 66 96 37 8B 46 B2 86 23 3F 2D 09 45 E0 16 1C E9 9C 11 7A FA 2D A8 50 47 42 74 01 06 84 76 0E 5F C6 04 29 1A 4A 65 AA 93 49 DF BD 00 ED 80 B8 26 CA 80 E8 20 6D 15 43 DD D8 E6 48 C2 8A 5A F8 70 6B 51 3A E2 2D 21 95 4B 6A 75 A8 90 CA B1 C0 E5 73 99 D7 59 D8 DD 3D C9 5C E4 49 61 22 11 60 85 48 C4 7D E0 84 62 AD B3 13 84 61 C1 9E 19 35 41 44 44 37 3F 21 33 64 4B 37 5D 77 6D 61 3F 00 78 00 01 5D 59 45 17 00 70 A4 D9 44 9E 95 51 B4 B0 91 CC 1E DB 34 F9 F7 13 8B 30 08 C0 AE 33 22 9C FF 87 CF 9B A2 B0 E5 E1 D0 E0 AD DD 8F E9 F6 1E 01 1F AA 74 46 66 B4 81 54 B9 29 E5 FC 0B 7F C9 13 AE 32 BA D6 55 2E B0 A1 30 24 B6 F2 E7 62 F9 2E 00 E4 51 61 50 7C D1 36 E8 61 96 36 FF B7 32 74 3C 2A F7 74 63 DA 7D 57 84 18 ED 84 E9 D8 87 6D 66 1D D5 84 D4 23 99 00 83 01 63 2A 69 2E 25 79 28 3B 29 33 29 40 28 54 7E 21 00 70 00 01 5D 59 45 17 00 68 6F 1F FB 31 7B D7 B7 D8 91 32 D7 20 8B 8A F6 02 C8 22 E5 24 8C 25 F2 6A C5 B0 ED 35 01 BF AF 42 72 33 4E FB 3F D3 02 BA F4 46 2B 68 20 0B E3 39 81 B1 D3 8A E0 1B 0F 69 D1 70 AE 49 A5 24 4F BB 58 4F F8 31 A0 37 4C CD F1 12 35 80 99 7D 25 CA F9 E9 45 B6 B0 57 56 66 61 C5 7B 90 57 BF E2 2C 94 91 80 1A B0 D7 21 A8 44 2C 33 4A 29 77 5F 71 40 41 38 3D 7A 41 65 33 01 08 00 29 00 01 00 25 00 1D 02 5B 14 28 E0 B9 91 E2 80 A2 CC 80 CF 89 E2 80 A2 CC 81 E0 B9 91 29 02 13 80 02 00 05 00 04 00 00 00 01 01 15 00 10 F9 86 85 81 30 F6 1B E0 E7 97 98 F6 46 C3 4F B2"
    val s = DataInputStream(data.hexToBytes().inputStream())
    val packet = ServerLoginResponseSuccessPacket(s,(data.length+1)/3)
    packet.decode()

    System.out.println("0828key:  \n" + packet._0828_rec_decr_key.toUByteArray().toHexString(" "))

    System.out.println("token88:  \n" + packet.token88.toUByteArray().toHexString(" "))

    System.out.println("token38:  \n" + packet.token38.toUByteArray().toHexString(" "))

    System.out.println("enckey:  \n" + packet.encryptionKey.toUByteArray().toHexString(" "))

    System.out.println("nick:  " + packet.nick)

    System.out.println("age:  " + packet.age)

    System.out.println("gender:  " + packet.gender)

}