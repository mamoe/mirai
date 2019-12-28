@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package test

import kotlinx.serialization.*
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.cryptor.readProtoMap
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.reflect.KClass

@Serializable
data class ProtoTest(
    //@SerialId(1) val string: String,
    //@SerialId(1) val int: Int,
    //@SerialId(1) val boolean: Boolean,
    //@SerialId(1) val short: Short,
    //@SerialId(1) val byte: Byte,
    @SerialId(1) @ProtoType(ProtoNumberType.FIXED) val fixedByte: Byte
)

@UseExperimental(MiraiInternalAPI::class)
suspend fun main() {
    println("PNG".toUtf8Bytes().toUHexString())
    deserializeTest()
}

suspend fun deserializeTest() {
    val bytes =
        """
            
           23 00 40 67 42 E8 E5 08 2D D2 87 83 DB A6 D3 56 51 6F 43 A5 DB 67 CD 31 24 DE 2D AF 5A D2 13 F6 5D 7B D1 26 55 61 DB 95 80 C6 B1 74 66 DB C2 8C EC 71 0E DA 74 D0 6D 80 BB 88 B5 12 6A 30 24 DB 65 95 1C 
                02 BC 89 50 4E 47 0D 0A 1A 0A 00 00 00 0D 49 48 44 52 00 00 00 82 00 00 00 35 08 03 00 00 00 BA 12 C3 02 00 00 00 04 67 41 4D 41 00 00 B1 8F 0B FC 61 05 00 00 00 01 73 52 47 42 00 AE CE 1C E9 00 00 00 3C 50 4C 54 45 FF F2 EC 55 96 73 F2 EF E4 79 AD 8F EC FD EC DC FF EB 23 70 4A FC FE F3 FD F7 ED F5 FA EC 30 84 5B AC E1 C3 12 78 49 CC E4 D0 99 C3 A9 12 87 53 E2 EF DE 49 7D 5F 3A 6F 51 BF F9 DA 26 D7 2D 0B 00 00 0B 58 49 44 41 54 58 C3 8C 98 8B 82 AC AA 0E 44 45 40 44 84 46 FD FF 7F BD AB E2 A3 9D D9 E7 EC 73 ED 99 7E 22 14 49 A5 92 30 CC AF 6B B8 AE 39 85 9E C6 79 98 E7 31 DB 47 FB 32 E7 FC 1A F3 EB E2 97 C4 08 EE D4 E0 39 C4 A0 DB E7 F3 E9 EF D7 60 8F FB 0D 57 D2 73 4E 29 0B C2 90 C2 9C C6 91 E9 13 83 5E 10 E6 5F 60 00 AB 41 F3 1C 42 18 46 20 84 CC ED FC 0F 86 E2 AF 7F 0F D4 E1 B1 03 33 64 4D C1 87 31 85 C0 BE 78 EA FA 35 7F 77 35 BC 6E 98 CF DF F4 48 73 C8 4C 30 84 70 CD 3A CC FF 17 88 97 1D EC 0F 13 84 64 D6 98 53 BC AE 30 0F BF 0C 9B ED 63 96 6D 6C F1 64 96 10 04 46 8E 32 82 59 E1 3F 11 7C 5F 6E 00 5A 7A 1E D9 85 AC 91 62 F1 CE 3B E7 5B B4 99 AF 89 CF C7 F5 74 5F 27 88 31 5F 86 31 F2 CC B7 8D 6E 9B FD D3 F3 30 BC B6 AF 5D C9 03 A2 A3 FD 34 7A 37 4D DB 3E AD CB F1 86 F0 45 C0 D2 E1 04 60 5F 25 BB 66 B1 67 30 42 9C 10 86 BF 81 F8 E9 02 99 37 40 44 D8 18 C4 C0 A1 EF D3 E2 4B F1 9F 4F 0B 27 84 6B F9 EF B3 AD 6F 2C D0 C2 63 EF A3 BE 91 25 C2 13 16 7F C7 F0 F5 C2 69 E2 1C 7A 6D AD 86 30 12 8A 29 6D 8B 3B 62 28 EB EA 44 F4 1C 6E 0E DC E3 BF 1F 65 BB D4 9B F7 BA D9 BC 19 60 50 7E B3 67 78 56 FB 15 08 F9 5C 1F 47 E6 34 A6 CA 14 B5 6D 25 E4 91 38 EC 7E 5A 4A 0A 71 59 97 13 42 1A 1F 07 26 D3 8D 71 B8 A2 93 7B C7 06 91 53 DD 62 24 A2 47 63 32 D3 88 5E 63 FA 97 BD 9B 12 E4 13 D2 29 2E 39 77 1C 98 FA E6 C5 47 AC 50 96 A9 85 1C DD BA 54 8D 65 C6 D1 08 7B 5E B8 FD 8E 1C 01 1A D9 7F 08 7D 8F 11 A4 A9 BA 69 F7 85 D0 32 5D 19 7F EE FD E7 4B 1E 6E 4D CA 29 57 D8 34 A6 E4 F6 68 1B 66 FF 93 47 1C CA 34 79 AC C5 00 91 CD E4 EB 21 BB 40 49 0A D2 5C 2B 91 D4 EB 04 04 6E F5 FB B4 EF DB 56 81 A0 9B E6 F9 A7 02 BD 22 22 DF 02 23 7D 1D 5B E8 62 63 DB 0B D3 A0 33 C1 4F AE A7 1C B7 69 8B A3 06 58 00 01 00 28 AD 53 81 65 DB 7D 7B 1E F4 AC 69 28 90 35 23 F3 0F DF AF 48 66 D9 06 13 0F AE 57 3C 5D AF CB 96 6C 5C CD 95 3F 2F 50 C9 01 15 00 10 A8 5D 2B 4F 33 AF 5D 99 B1 EF 92 DA C6 E5 A9 FB
                
           
           """.trimIndent()
            .replace("\n", " ")
            .replace("UVarInt", "", ignoreCase = true)
            .replace("uint", "", ignoreCase = true)
            .replace("[", "")
            .replace("]", "")
            .replace("(", "")
            .replace(")", "")
            .replace("  ", " ")
            .replace("  ", " ")
            .replace("  ", " ")
            .replace("_", "")
            .replace(",", "")
            .hexToBytes()
    println(bytes.read { readProtoMap() })
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun <T : Any> KClass<T>.loadFrom(protoBuf: ByteArray): T = ProtoBuf.load(this.serializer(), protoBuf)