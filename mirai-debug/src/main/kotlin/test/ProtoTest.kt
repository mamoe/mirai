@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package test

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import kotlinx.serialization.serializer
import net.mamoe.mirai.utils.*
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
    println(Http.getURL("http://gchat.qpic.cn/gchatpic_new/1994701021/1994701021-2868483628-39F76532E1AB5CA786D7A51389225385/0?vuin=1994701021&term=255&srvver=26933").remaining)

    val bytes =
        """
            
            
   10 02 22 4E 08 A0 89 F7 B6 03 10 A2 FF 8C F0 03 18 BB 92 94 BF 08 22 10 63 B1 86 6F 41 3E D9 78 CB CF 53 3E 92 28 5C 58 28 04 30 02 38 20 40 FF 01 48 00 50 01 5A 05 32 36 39 33 33 60 00 68 00 70 00 78 00 80 01 97 04 88 01 ED 03 90 01 04 A0 01 01
   
   
   
    """.trimIndent().replace("\n", " ").replace("[", "").replace("]", "")
            .hexToBytes()

    /*
    00 00 00 07 00 00 00 52 08 01 12 03 98 01 02
    10 02
    22 [4E] 08 A0 89 F7 B6 03 10 A2 FF 8C F0 03 18 BB 92 94 BF 08 22 10 64 CF BB 65 00 13 8D B5 58 E2 45 1E EA 65 88 E1 28 04 30 02 38 20 40 FF 01 48 00 50 01 5A 05 32 36 39 33 33 60 00 68 00 70 00 78 00 80 01 97 04 88 01 ED 03 90 01 04 A0 01 01
     */

    /*
    for (i in 0..bytes.size) {
        try {
            println(bytes.copyOfRange(i, bytes.size).read { readProtoMap() })
        } catch (e: Exception) {

        }
    }*/
    println(bytes.read { readProtoMap() })
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun <T : Any> KClass<T>.loadFrom(protoBuf: ByteArray): T = ProtoBuf.load(this.serializer(), protoBuf)