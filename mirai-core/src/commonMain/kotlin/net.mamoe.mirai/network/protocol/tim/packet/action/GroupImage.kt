@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "RUNTIME_ANNOTATION_NOT_SUPPORTED")

package net.mamoe.mirai.network.protocol.tim.packet.action

import kotlinx.coroutines.withContext
import kotlinx.io.core.ByteReadPacket
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.GroupId
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.contact.withSession
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.message.ImageId0x03
import net.mamoe.mirai.message.requireLength
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.event.EventPacket
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.Http
import net.mamoe.mirai.utils.assertUnreachable
import net.mamoe.mirai.utils.io.toUHexString
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

@Suppress("unused")
@Serializable
class ImageDownloadInfo(
    @SerialId(3) val errorCode: Int = 0, // 0 for success
    @SerialId(4) val errorMessage: String? = null, // 感动中国

    @SerialId(10) private val _port: List<Byte>? = null,
    @SerialId(11) private val _host: String? = null,

    @SerialId(12) private val _thumbnail: String? = null,
    @SerialId(13) private val _original: String? = null,
    @SerialId(14) private val _compressed: String? = null
) : GroupImageResponse, ImageLink {
    private inline val port: List<Byte> get() = _port!!
    private inline val host: String get() = "http://" + _host!!

    val thumbnail: String get() = host + ":" + port.first() + _thumbnail!!
    override val original: String get() = host + ":" + port.first() + _original!!
    val compressed: String get() = host + ":" + port.first() + _compressed!!
    override fun toString(): String = "ImageDownloadInfo(${_original?.let { original } ?: errorMessage ?: "unknown"})"
}

fun ImageDownloadInfo.requireSuccess(): ImageDownloadInfo {
    require(this.errorCode == 0) { this.errorMessage ?: "null" }
    return this
}

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
@PacketVersion(date = "2019.11.22", timVersion = "2.3.2 (21173)")
object GroupImagePacket : SessionPacketFactory<GroupImageResponse>() {

    private val constValue3 = byteArrayOf(
        0x28, 0x00, 0x5A, 0x00, 0x53, 0x00, 0x41, 0x00, 0x58, 0x00, 0x40, 0x00, 0x57,
        0x00, 0x4B, 0x00, 0x52, 0x00, 0x4A, 0x00, 0x5A, 0x00, 0x31, 0x00, 0x7E, 0x00
    )

    @Suppress("unused")
    @Serializable
    private class RequestIdProto(
        @SerialId(2) val unknown4: Byte = 1,
        @SerialId(3) var body: Body
    ) {
        @Serializable
        class Body(
            @SerialId(1) val group: Int,
            @SerialId(2) val bot: Int,
            @SerialId(3) val const1: Byte = 0,
            @SerialId(4) val md5: ByteArray,
            @SerialId(5) val const2: Short = 0x0E2D,
            @SerialId(6) val const3: ByteArray = constValue3,
            @SerialId(7) val const4: Byte = 1,
            // 8 is missing
            @SerialId(9) val const5: Byte = 1,
            @SerialId(10) val width: Int,
            @SerialId(11) val height: Int,
            @SerialId(12) val const6: Byte = 4,
            @SerialId(13) val const7: ByteArray = constValue7,
            @SerialId(14) val const8: Byte = 0,
            @SerialId(15) val const9: Byte = 3,
            @SerialId(16) val const10: Byte = 0
        )
    }

    @Suppress("unused")
    @Serializable
    private class RequestLinkProto(
        @SerialId(2) val unknown4: Byte = 2,
        @SerialId(4) var body: Body
    ) {
        @Serializable
        class Body(
            @SerialId(1) val group: Int,
            @SerialId(2) val bot: Int,
            @SerialId(3) val uniqueId: Int,
            @SerialId(4) val md5: ByteArray,
            @SerialId(5) val const2: Byte = 4,
            @SerialId(6) val const3: Byte = 2,
            @SerialId(7) val const4: Byte = 32,
            @SerialId(8) val const14: Int = 255,
            @SerialId(9) val const5: Byte = 0,
            @SerialId(10) val unknown5: Int = 1,
            @SerialId(11) val const7: ByteArray = constValue7,
            @SerialId(12) val unknown6: Byte = 0,
            @SerialId(13) val const6: Byte = 0,
            @SerialId(14) val const8: Byte = 0,
            @SerialId(15) val const9: Byte = 0,
            @SerialId(16) val height: Int,
            @SerialId(17) val width: Int,
            @SerialId(18) val const12: Int = 1003, //?? 有时候还是1000, 1004
            // 19 is missing
            @SerialId(20) val const13: Byte = 1
        )
    }

    private val constValue7: ByteArray = byteArrayOf(0x32, 0x36, 0x39, 0x33, 0x33)

    private val requestImageIdHead = ubyteArrayOf(0x12u, 0x03u, 0x98u, 0x01u, 0x01u)

    @Suppress("FunctionName")
    fun RequestImageId(
        bot: UInt,
        groupInternalId: GroupInternalId,
        image: ExternalImage,
        sessionKey: SessionKey
    ): OutgoingPacket = buildSessionProtoPacket(
        bot, sessionKey, name = "GroupImagePacket.RequestImageId",
        head = requestImageIdHead,
        serializer = RequestIdProto.serializer(),
        protoObj = RequestIdProto(
            body = RequestIdProto.Body(
                bot = bot.toInt(),
                group = groupInternalId.value.toInt(),
                md5 = image.md5,
                height = image.height,
                width = image.width
            )
        )
    )

    private val requestImageLinkHead = ubyteArrayOf(0x08u, 0x01u, 0x12u, 0x03u, 0x98u, 0x01u, 0x2u)
    @Suppress("FunctionName")
    fun RequestImageLink(
        bot: UInt,
        sessionKey: SessionKey,
        imageId: ImageId0x03
    ): OutgoingPacket {
        imageId.requireLength()
        //require(imageId.value.length == 37) { "ImageId.value.length must == 37" }
        //[00 00 00 07] [00 00 00 52] (08 01 12 03 98 01 02) 10 02 22 4E 08 A0 89 F7 B6 03 10 A2 FF 8C F0 03 18 BB 92 94 BF 08 22 10 64 CF BB 65 00 13 8D B5 58 E2 45 1E EA 65 88 E1 28 04 30 02 38 20 40 FF 01 48 00 50 01 5A 05 32 36 39 33 33 60 00 68 00 70 00 78 00 80 01 97 04 88 01 ED 03 90 01 04 A0 01 01
        //  head 长度      proto 长度        head               proto
        return buildSessionProtoPacket(
            bot,
            sessionKey,
            name = "GroupImagePacket.RequestImageLink",
            head = requestImageLinkHead,
            serializer = RequestLinkProto.serializer(),
            protoObj = RequestLinkProto(
                body = RequestLinkProto.Body(
                    bot = bot.toInt(), // same bin representation, so will be decoded correctly as a unsigned value in the server
                    group = bot.toInt(), // it's no need to pass a real group (internal) id
                    uniqueId = imageId.uniqueId.toInt(),
                    md5 = imageId.md5,
                    height = imageId.height,
                    width = imageId.width
                )
            )
        )
    }

    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): GroupImageResponse {

        @Serializable
        data class GroupImageResponseProto(
            @SerialId(3) val imageUploadInfoPacket: ImageUploadInfo? = null,
            @SerialId(4) val imageDownloadInfo: ImageDownloadInfo? = null
        )

        val proto = decodeProtoPacket(GroupImageResponseProto.serializer())
        return when {
            proto.imageUploadInfoPacket != null -> proto.imageUploadInfoPacket
            proto.imageDownloadInfo != null -> proto.imageDownloadInfo
            else -> assertUnreachable()
        }
    }
}