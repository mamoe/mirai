@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package test

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumberType
import kotlinx.serialization.protobuf.ProtoType
import kotlinx.serialization.serializer
import net.mamoe.mirai.network.protocol.tim.packet.action.ImageUploadInfo
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.ProtoFieldId
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.reflect.KClass

@Serializable
data class ProtoTest(
    @SerialId(1) val string: String,
    @SerialId(1) val int: Int,
    @SerialId(1) val boolean: Boolean,
    @SerialId(1) val short: Short,
    @SerialId(1) val byte: Byte,
    @SerialId(1) @ProtoType(ProtoNumberType.FIXED) val fixedByte: Byte
)

@UseExperimental(MiraiInternalAPI::class)
fun main() {
    deserializeTest()
    return

    println(ProtoFieldId(0x12u))

    intArrayOf(
        0x5A,
        0X62,
        0X6A,
        0X72
    ).forEach {
        println(it.toUShort().toUHexString() + " => " + ProtoFieldId(it.toUInt()))
    }

    println(ProtoBuf.dump(ProtoTest.serializer(), ProtoTest("ss", 1, false, 1, 1, 1)).toUHexString())
}

fun deserializeTest() {
    val bytes =
        ("08 00 10 00 20 01 2A 1E 0A 10 BF 83 4C 2B 67 47 41 8C 9F DD 6D 8C 8E 95 53 D6 10 04 18 E4 E0 54 20 B0 09 28 9E 0D 30 FB AE A6 F4 07 38 50 48 D8 92 9E CD 0A")
            .hexToBytes()

    println(ImageUploadInfo::class.loadFrom(bytes))
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun <T : Any> KClass<T>.loadFrom(protoBuf: ByteArray): T = ProtoBuf.load(this.serializer(), protoBuf)