@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package test

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import kotlinx.serialization.serializer
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.ProtoFieldId
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
import net.mamoe.mirai.utils.io.toUHexString
import net.mamoe.mirai.utils.readProtoMap
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
    //println(ImageId0x03("{CE20C11D-FAAB-6E9B-A685-93072427F942}.png", 0, 0).md5.toUHexString())
    //return

    /*
    {
    02=7B 33 45 46 32 42 37 36 42 2D 43 44 46 38 2D 39 45 35 37 2D 44 39 44 46 2D 33 33 35 44 46 31 42 30 46 43 30 32 7D 2E 70 6E 67,
    04=87 E5 09 3B,
    05=D2 C4 C0 B7,
    06=00 00 00 50,
    07=43,
    08=,
     09=01,
     B=,
     14=00 00 00 00,
     15=00 00 01 ED,
     16=00 00 02 17,
     18=00 00 EB 34
     }
     */
    deserializeTest()
    return

    intArrayOf(
    ).forEach {
        println(it.toUShort().toUHexString() + " => " + ProtoFieldId(it.toUInt()))
    }

    println(ProtoBuf.dump(ProtoTest.serializer(), ProtoTest(1)).toUHexString())
}

suspend fun deserializeTest() {
    //println(Http.getURL("http://gchat.qpic.cn/gchatpic_new/1994701021/1994701021-2868483628-39F76532E1AB5CA786D7A51389225385/0?vuin=1994701021&term=255&srvver=26933").remaining)

    val bytes =
        """
            
  08 01 10 00 1A 89 02 10 01 18 03 3A 4D 08 A6 A7 F1 EA 02 10 DD F1 92 B7 07 18 01 20 D3 81 D5 EE 05 2A 00 32 11 E6 9D A5 E8 87 AA 51 51 E5 8F B7 E6 9F A5 E6 89 BE 38 01 40 01 48 00 50 00 58 00 60 01 6A 00 70 00 78 00 80 01 03 A0 01 00 A8 01 00 B0 01 00 C0 01 01 E8 01 00 3A 4A 08 A6 A7 F1 EA 02 10 DD F1 92 B7 07 18 03 20 DC 80 D5 EE 05 2A 00 32 11 E6 9D A5 E8 87 AA 51 51 E5 8F B7 E6 9F A5 E6 89 BE 38 01 40 01 48 00 50 00 58 00 60 01 6A 00 70 00 78 00 80 01 00 A0 01 00 A8 01 00 B0 01 00 C0 01 00 3A 4A 08 A6 A7 F1 EA 02 10 DD F1 92 B7 07 18 03 20 D7 F8 D4 EE 05 2A 00 32 11 E6 9D A5 E8 87 AA 51 51 E5 8F B7 E6 9F A5 E6 89 BE 38 01 40 01 48 00 50 00 58 00 60 01 6A 00 70 00 78 00 80 01 00 A0 01 00 A8 01 00 B0 01 00 C0 01 00 40 D3 81 D5 EE 05 48 01 50 01 58 01 60 DD F1 92 B7 07 72 08 0A 06 08 DD F1 92 B7 07 78 00
           
           
           """.trimIndent().replace("\n", " ").replace("[", "").replace("]", "")
            .hexToBytes()
    println(bytes.read { readProtoMap() })
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun <T : Any> KClass<T>.loadFrom(protoBuf: ByteArray): T = ProtoBuf.load(this.serializer(), protoBuf)