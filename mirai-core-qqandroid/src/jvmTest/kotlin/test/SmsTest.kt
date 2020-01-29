package test

import kotlinx.io.core.ByteReadPacket
import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.readTLVMap
import net.mamoe.mirai.utils.io.toIoBuffer

fun main(){

    //server to client
    val s2c = "01 46 00 33 00 00 00 A3 00 0F E9 AA 8C E7 9F AD E4 BF A1 E5 A4 B1 E8 B4 A5 00 18 E9 AA 8C E8 AF 81 E7 A0 81 E8 BE 93 E5 85 A5 E9 94 99 E8 AF AF E3 80 82 00 00 00 00 05 08 00 22 01 00 00 0B B8 00 1B 02 00 00 00 10 20 02 ED BD 08 10 00 00 00 A3 00 00 00 00 3E 03 3F A2 00 00 00 A3".hexToBytes().toIoBuffer().readTLVMap()
    //client to server
    val c2s = "00 08 00 08 00 00 00 00 08 04 00 00 01 04 00 24 41 69 4E 54 75 7A 50 2F 48 6D 5A 30 74 37 64 54 71 57 7A 67 79 35 54 4C 77 39 55 69 53 59 69 45 71 67 3D 3D 01 16 00 0E 00 08 F7 FF 7C 00 01 04 00 01 5F 5E 10 E2 01 74 00 61 45 66 43 39 46 4B 63 70 47 30 5F 5A 55 41 4F 6A 4E 4C 6F 72 56 30 77 66 4B 67 49 4D 33 33 6E 58 44 37 5F 4B 61 75 56 6D 4F 6F 54 68 6A 64 38 62 72 44 64 69 5F 62 48 51 5A 66 37 6E 4F 6B 78 43 35 6E 47 4E 38 6B 6A 35 39 6D 37 32 71 47 66 78 4E 76 50 51 53 39 33 66 37 6B 72 71 66 71 78 63 5F 01 7A 00 04 00 00 00 09 01 97 00 01 00".hexToBytes().toIoBuffer().readTLVMap()

    println(c2s.contentToString())
}