package test

import net.mamoe.mirai.utils.cryptor.contentToString
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.readTLVMap
import net.mamoe.mirai.utils.io.toIoBuffer

fun main(){

    //server to client
    val s2c = """
00 08 00 08 00 00 00 00 08 04 00 00 01 04 00 24 41 75 43 4A 64 56 72 56 34 30 79 67 67 2B 41 4D 59 30 42 31 4B 58 64 44 70 68 61 49 4C 41 67 59 65 41 3D 3D 01 16 00 0E 00 08 F7 FF 7C 00 01 04 00 01 5F 5E 10 E2 01 74 00 61 45 47 39 4A 30 72 79 2D 42 46 57 2D 64 36 39 76 4B 44 62 39 47 5F 61 67 6C 7A 71 46 61 36 35 34 47 33 41 4B 77 63 6D 58 78 61 71 6A 34 31 45 36 76 4D 6C 44 4A 50 68 42 41 6D 4D 71 61 65 71 6B 58 50 43 2D 52 5A 51 34 4D 41 38 54 62 63 48 6D 39 53 66 57 37 57 59 4E 6A 52 4C 52 4B 36 7A 56 6B 01 7A 00 04 00 00 00 09 01 97 00 01 00
         """.trimIndent().hexToBytes().toIoBuffer().readTLVMap()
    //client to server
    val c2s = "00 08 00 08 00 00 00 00 08 04 00 00 01 04 00 24 41 69 4E 54 75 7A 50 2F 48 6D 5A 30 74 37 64 54 71 57 7A 67 79 35 54 4C 77 39 55 69 53 59 69 45 71 67 3D 3D 01 16 00 0E 00 08 F7 FF 7C 00 01 04 00 01 5F 5E 10 E2 01 74 00 61 45 66 43 39 46 4B 63 70 47 30 5F 5A 55 41 4F 6A 4E 4C 6F 72 56 30 77 66 4B 67 49 4D 33 33 6E 58 44 37 5F 4B 61 75 56 6D 4F 6F 54 68 6A 64 38 62 72 44 64 69 5F 62 48 51 5A 66 37 6E 4F 6B 78 43 35 6E 47 4E 38 6B 6A 35 39 6D 37 32 71 47 66 78 4E 76 50 51 53 39 33 66 37 6B 72 71 66 71 78 63 5F 01 7A 00 04 00 00 00 09 01 97 00 01 00".hexToBytes().toIoBuffer().readTLVMap()

    println(get_mpasswd())
    println(s2c.contentToString())

}

