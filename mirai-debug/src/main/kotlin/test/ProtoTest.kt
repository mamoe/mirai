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