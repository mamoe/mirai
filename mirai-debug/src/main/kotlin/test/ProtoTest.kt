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
import net.mamoe.mirai.utils.cryptor.readProtoMap
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.read
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
            08 02 1A 55 08 A2 FF 8C F0 03 10 DD F1 92 B7 07 1A 25 2F 34 35 35 38 66 39 30 38 2D 37 62 39 61 2D 34 65 32 66 2D 38 63 36 39 2D 34 61 35 32 61 66 62 33 36 35 61 37 20 01 30 04 38 05 40 09 48 01 58 00 60 01 6A 0A 38 2E 32 2E 30 2E 31 32 39 36 70 E0 8C B2 F0 05 78 01 50 03
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