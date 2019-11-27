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
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
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
    deserializeTest()

    /*
    intArrayOf(
    ).forEach {
        println(it.toUShort().toUHexString() + " => " + ProtoFieldId(it.toUInt()))
    }

    println(ProtoBuf.dump(ProtoTest.serializer(), ProtoTest(1)).toUHexString())*/
}

suspend fun deserializeTest() {
    val bytes =
        """
            
            
            
 00 36 DD C4 A0 01 2D 5C 53 A6 03 3E 03 3F A2 06 B9 DC C0 ED D4 B1 00 30 31 63 35 35 31 34 63 62 36 64 37 39 61 65 61 66 35 66 33 34 35 64 39 63 32 34 64 65 37 32 36 64 39 64 36 39 36 64 66 66 32 38 64 63 38 32 37 36         
            
            
           """.trimIndent().replace("\n", " ").replace("[", "").replace("]", "")
            .hexToBytes()
    println(bytes.read { readProtoMap() })
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun <T : Any> KClass<T>.loadFrom(protoBuf: ByteArray): T = ProtoBuf.load(this.serializer(), protoBuf)