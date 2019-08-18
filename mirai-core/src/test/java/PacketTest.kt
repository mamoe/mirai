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

    val data = "00 01 09 00 70 00 01 B8 51 6F CD 6A 1B 27 7B 76 41 68 F3 BA 33 7F 73 00 38 35 3E 7C 76 37 D4 AD A1 2C E0 C7 23 9A 44 0D 2C 38 53 9F 94 80 E1 20 B9 2F F5 9F 80 3A 8C CB F6 1E 54 3D 09 12 B9 6A FA DB E3 02 63 A9 E1 5A 2C EE 02 39 10 AF 4B 3F 74 00 20 BF AC F4 D7 4F BD 5A 33 1E 08 3E BE D0 E7 61 79 F4 79 14 9D C9 24 67 01 67 AB 7B 23 94 E8 42 A7 00 00 01 03 00 14 00 01 00 10 60 C9 5D A7 45 70 04 7F 21 7D 84 50 5C 66 A5 C6 01 10 00 3C 00 01 00 38 8A 51 0A CC 9D D6 AC A6 BA BA 32 0E 4E E8 04 28 F9 33 E3 2A D5 0A 99 13 14 C0 DE DA 3C 01 B6 CB 58 5C 5F E0 4B 0C C4 1C 1F 28 36 DC 6A B0 6E CE AF 0B D1 17 AD 4B C3 BD 01 07 01 D3 00 01 00 16 00 00 00 01 00 00 00 64 00 00 0D CE 00 09 3A 80 00 00 00 3C 00 02 67 35 45 62 5B 35 2C 46 7D 47 48 5F 52 70 5F 38 00 88 00 04 5D 59 1F DD B7 5F F8 D4 00 00 00 00 00 78 4A 9E EB 20 77 9C D0 02 98 85 9A 48 EA 05 11 AC F4 25 4C 7C 1F E2 18 CD 78 EF 22 07 8D FD E8 51 23 29 CF 64 97 DC E2 1B 3E 99 00 54 37 F8 35 8F 70 0A CB 7C CD 74 6C 9C 3D 81 32 C6 9B 8A B4 16 D2 41 0D B3 27 CC C7 F9 07 71 EB 6D FD 75 2F 95 75 31 21 D5 22 39 16 FA F6 14 3C 10 81 20 21 E4 31 D2 A3 A4 49 EB EC DA AC 38 0C 27 C9 3F 8A A4 0C 32 58 56 E4 70 47 AB 6A 3E 28 57 2E 55 45 24 56 69 69 43 51 27 2C 71 00 78 00 01 5D 59 1F DD 00 70 98 9E 49 87 39 20 2A 9E 75 28 3D D5 05 9C AF 0F 60 BC 95 2E B9 CF 86 E6 C6 54 FD E7 CF EA 1E 1A 2E E2 99 DE EB CC FF 39 11 6E 5C 8C 17 E0 E4 7E DD 55 CF 09 F7 EA 1A 9E AF CB BC 36 E0 B8 7A 9E 47 34 68 61 E9 1E A0 29 AE 17 A2 EA A6 72 3D 5D 6B 8A 22 A2 1C 24 B8 B2 A0 00 56 11 A9 12 B3 31 D6 3A 2D 5A C0 21 D2 52 56 14 C1 8F EE D9 2B C3 00 83 01 66 33 7A 39 39 32 79 40 49 37 42 4B 62 5E 7B 68 00 70 00 01 5D 59 1F DD 00 68 04 49 23 69 F2 FC C3 81 DC F1 D8 E6 AE E7 69 C9 78 EB EB 90 B1 B4 42 DC DD 1D EF 87 30 72 36 20 1E B6 89 5B AF A5 88 25 60 B7 E3 66 30 D4 0A E5 D9 FA 74 2C 18 E2 26 6C DC F7 42 38 21 23 25 08 57 45 2F 4B BF BF 83 9F CA C5 04 5C 7A 97 8C 4B 1B 6F F7 EE A7 5D 4C 94 B2 CF F6 8D 32 00 21 46 9B C6 9D C5 4B 75 32 19 5E 3D 51 32 3A 79 55 71 25 5F 4B 6B 4A 6E 5D 27 01 08 00 29 00 01 00 25 00 1D 02 5B 14 28 E0 B9 91 E2 80 A2 CC 80 CF 89 E2 80 A2 CC 81 E0 B9 91 29 02 13 80 02 00 05 00 04 00 00 00 01 01 15 00 10 A0 D6 11 4B 1F 12 26 55 0D 21 1D BB 53 5B 4E 12"
    val s = DataInputStream(data.hexToBytes().inputStream())
    val packet = ServerLoginResponseSuccessPacket(s,(data.length+1)/3)
    packet.decode()

    System.out.println("0828key:  \n" + packet._0828_rec_decr_key.toUByteArray().toHexString(" "))

    System.out.println("token88:  \n" + packet.token88.toUByteArray().toHexString(" "))

    System.out.println("token38:  \n" + packet.token38.toUByteArray().toHexString(" "))

    System.out.println("enckey:  \n" + packet.encryptionKey.toUByteArray().toHexString(" "))

    System.out.println("nick:  " + packet.nick)

}