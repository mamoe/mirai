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
}

suspend fun deserializeTest() {
    val bytes =
        """
            
            
            00 0A 00 04 01 00 00 00 00 (32 DC FC C8) 01 (2D 5C 53 A6) 03 3E 03 3F A2 06 B4 B4 BD A8 D5 DF 00 30 32 33 32 63 32 39 36 65 36 35 64 62 64 64 64 64 65 35 62 33 34 64 36 62 34 33 32 61 30 64 61 65 32 30 37 35 38 34 37 34 32 65 32 39 63 35 63 64           
           
           
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