@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "RUNTIME_ANNOTATION_NOT_SUPPORTED")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.coroutines.withContext
import kotlinx.io.charsets.Charsets
import kotlinx.io.core.*
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.contact.withSession
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.message.requireLength
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.event.EventPacket
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.Http
import net.mamoe.mirai.utils.assertUnreachable
import net.mamoe.mirai.utils.io.*
import kotlin.coroutines.coroutineContext


/**
 * 上传群图片
 * 挂起直到上传完成或失败
 *
 * 在 JVM 下, `SendImageUtilsJvm.kt` 内有多个捷径函数
 *
 * @throws OverFileSizeMaxException 如果文件过大, 服务器拒绝接收时
 */
suspend fun Group.uploadImage(image: ExternalImage): ImageId = withSession {
    val userContext = coroutineContext
    val response = GroupImagePacket.RequestImageId(bot.qqAccount, internalId, image, sessionKey).sendAndExpect<GroupImageResponse>()

    withContext(userContext) {
        when (response) {
            is ImageUploadInfo -> response.uKey?.let {
                Http.postImage(
                    htcmd = "0x6ff0071",
                    uin = bot.qqAccount,
                    groupId = GroupId(id),
                    imageInput = image.input,
                    inputSize = image.inputSize,
                    uKeyHex = it.toUHexString("")
                )
            }

            // TODO: 2019/11/17 超过大小的情况
            //is Overfile -> throw OverFileSizeMaxException()
            else -> assertUnreachable()
        }
    }

    return image.groupImageId
}

interface GroupImageResponse : EventPacket

// endregion

@Serializable
data class ImageDownloadInfo(
    @SerialId(11) val host: String,

    @SerialId(12) val thumbnail: String,
    @SerialId(13) val original: String,
    @SerialId(14) val compressed: String
) : GroupImageResponse

@Serializable
class ImageUploadInfo(
    @SerialId(8) val uKey: ByteArray? = null
) : GroupImageResponse {
    override fun toString(): String = "ImageUploadInfo(uKey=${uKey?.toUHexString()})"
}

/**
 * 获取 Image Id 和上传用的一个 uKey
 */
@AnnotatedId(KnownPacketId.GROUP_IMAGE_ID)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2 (21173)")
object GroupImagePacket : SessionPacketFactory<GroupImageResponse>() {
    @Suppress("FunctionName")
    fun RequestImageId(
        bot: UInt,
        groupInternalId: GroupInternalId,
        image: ExternalImage,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionPacket(bot, sessionKey, version = TIMProtocol.version0x04) {
        writeHex("00 00 00 07 00 00")

        writeShortLVPacket(lengthOffset = { it - 7 }) {
            writeByte(0x08)
            writeHex("01 12 03 98 01 01 10 01 1A")
            //                             02 10 02 22

            writeUVarIntLVPacket(lengthOffset = { it }) {
                writeTUVarint(0x08u, groupInternalId.value)
                writeTUVarint(0x10u, bot)
                writeTV(0x1800u)

                writeUByte(0x22u)
                writeUByte(0x10u)
                writeFully(image.md5)

                writeTUVarint(0x28u, image.inputSize.toUInt())
                writeUVarIntLVPacket(tag = 0x32u) {
                    writeTV(0x5B_00u)
                    writeTV(0x40_00u)
                    writeTV(0x33_00u)
                    writeTV(0x48_00u)
                    writeTV(0x5F_00u)
                    writeTV(0x58_00u)
                    writeTV(0x46_00u)
                    writeTV(0x51_00u)
                    writeTV(0x45_00u)
                    writeTV(0x51_00u)
                    writeTV(0x40_00u)
                    writeTV(0x24_00u)
                    writeTV(0x4F_00u)
                }
                writeTV(0x38_01u)
                writeTV(0x48_01u)
                writeTUVarint(0x50u, image.width.toUInt())
                writeTUVarint(0x58u, image.height.toUInt())
                writeTV(0x60_04u)//这个似乎会变 有时候是02, 有时候是03
                writeTByteArray(0x6Au, value0x6A)

                writeTV(0x70_00u)
                writeTV(0x78_03u)
                writeTV(0x80_01u)
                writeUByte(0u)
            }
        }

    }

    @Suppress("FunctionName")
    fun RequestImageLink(
        bot: UInt,
        sessionKey: SessionKey,
        imageId: ImageId
    ): OutgoingPacket {
        imageId.requireLength()
        require(imageId.value.length == 37) { "ImageId.value.length must == 37" }

        // 00 00 00 07 00 00 00
        // [4B]
        // 08
        // 01 12
        // 03 98
        // 01 02
        // 08 02
        //
        // 1A [47]
        // 08 [A2 FF 8C F0 03] UVarInt
        // 10 [DD F1 92 B7 07] UVarInt
        // 1A [25] 2F 38 65 32 63 32 38 62 64 2D 35 38 61 31 2D 34 66 37 30 2D 38 39 61 31 2D 65 37 31 39 66 63 33 30 37 65 65 66
        // 20 02 30 04 38 20 40 FF 01 50 00 6A 05 32 36 39 33 33 78 01


        // 00 00 00 07 00 00 00
        // [4B]
        // 08 01
        // 12 03
        // 98 01 02
        // 08 02
        //
        // 1A
        // [47]
        // 08 [A2 FF 8C F0 03]
        // 10 [A6 A7 F1 EA 02]
        // 1A [25] 2F 39 61 31 66 37 31 36 32 2D 38 37 30 38 2D 34 39 30 38 2D 38 31 63 30 2D 66 34 63 64 66 33 35 63 38 64 37 65
        // 20 02 30 04 38 20 40 FF 01 50 00 6A 05 32 36 39 33 33 78 01

        return buildSessionPacket(bot, sessionKey, version = TIMProtocol.version0x04) {
            writeHex("00 00 00 07 00 00")

            writeUShort(0x004Bu)

            writeUByte(0x08u)
            writeTV(0x01_12u)
            writeTV(0x03_98u)
            writeTV(0x01_02u)
            writeTV(0x08_02u)

            writeUByte(0x1Au)
            writeUByte(0x47u)
            writeTUVarint(0x08u, bot)
            writeTUVarint(0x10u, bot)
            writeTLV(0x1Au, imageId.value.toByteArray(Charsets.ISO_8859_1))
            writeHex("20 02 30 04 38 20 40 FF 01 50 00 6A 05 32 36 39 33 33 78 01")
        }
    }

    private val value0x6A: UByteArray = ubyteArrayOf(0x05u, 0x32u, 0x36u, 0x36u, 0x35u, 0x36u)

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): GroupImageResponse {
        discardExact(6)//00 00 00 05 00 00

        discardExact(2) // 是 protobuf 的长度, 但是是错的
        val bytes = readBytes()
        // println(ByteReadPacket(bytes).readProtoMap())

        @Serializable
        data class GroupImageResponseProto(
            @SerialId(3) val imageUploadInfoPacket: ImageUploadInfo? = null,
            @SerialId(4) val imageDownloadInfo: ImageDownloadInfo? = null
        )

        val proto = ProtoBuf.load(GroupImageResponseProto.serializer(), bytes)
        return when {
            proto.imageUploadInfoPacket != null -> proto.imageUploadInfoPacket
            proto.imageDownloadInfo != null -> proto.imageDownloadInfo
            else -> assertUnreachable()
        }
    }
}