@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS", "unused")

package net.mamoe.mirai.network.protocol.tim.packet.action

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.userAgent
import kotlinx.coroutines.withContext
import kotlinx.io.core.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.ImageId
import net.mamoe.mirai.network.protocol.tim.TIMProtocol
import net.mamoe.mirai.network.protocol.tim.packet.*
import net.mamoe.mirai.network.protocol.tim.packet.action.FriendImageIdRequestPacket.Response.State.*
import net.mamoe.mirai.network.qqAccount
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.withSession
import kotlin.coroutines.coroutineContext

/**
 * 图片文件过大
 */
class OverFileSizeMaxException : IllegalStateException()

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
    GroupImageIdRequestPacket(bot.qqAccount, internalId, image, sessionKey)
        .sendAndExpect<GroupImageIdRequestPacket.Response, Unit> {
            withContext(userContext) {
                when (it.state) {
                    GroupImageIdRequestPacket.Response.State.REQUIRE_UPLOAD -> httpClient.postImage(
                        htcmd = "0x6ff0071",
                        uin = bot.qqAccount,
                        groupId = GroupId(id),
                        imageInput = image.input,
                        inputSize = image.inputSize,
                        uKeyHex = it.uKey!!.toUHexString("")
                    )

                    GroupImageIdRequestPacket.Response.State.ALREADY_EXISTS -> {
                    }

                    GroupImageIdRequestPacket.Response.State.OVER_FILE_SIZE_MAX -> throw OverFileSizeMaxException()
                }
            }
        }.join()
    image.groupImageId
}

/**
 * 上传图片
 * 挂起直到上传完成或失败
 *
 * 在 JVM 下, `SendImageUtilsJvm.kt` 内有多个捷径函数
 *
 * @throws OverFileSizeMaxException 如果文件过大, 服务器拒绝接收时
 */
suspend fun QQ.uploadImage(image: ExternalImage): ImageId = bot.withSession {
    FriendImageIdRequestPacket(qqAccount, sessionKey, id, image)
        .sendAndExpect<FriendImageIdRequestPacket.Response, ImageId> {
            when (it.state) {
                REQUIRE_UPLOAD -> httpClient.postImage(
                    htcmd = "0x6ff0070",
                    uin = bot.qqAccount,
                    groupId = null,
                    uKeyHex = it.uKey!!.toUHexString(""),
                    imageInput = image.input,
                    inputSize = image.inputSize
                )

                ALREADY_EXISTS -> {
                }

                OVER_FILE_SIZE_MAX -> throw OverFileSizeMaxException()
            }

            it.imageId!!
        }.await()
}

@Suppress("SpellCheckingInspection")
internal suspend inline fun HttpClient.postImage(
    htcmd: String,
    uin: UInt,
    groupId: GroupId?,
    imageInput: Input,
    inputSize: Long,
    uKeyHex: String
): Boolean = try {
    post<HttpStatusCode> {
        url {
            protocol = URLProtocol.HTTP
            host = "htdata2.qq.com"
            path("cgi-bin/httpconn")

            parameters["htcmd"] = htcmd
            parameters["uin"] = uin.toLong().toString()

            if (groupId != null) parameters["groupcode"] = groupId.value.toLong().toString()

            parameters["term"] = "pc"
            parameters["ver"] = "5603"
            parameters["filesize"] = inputSize.toString()
            parameters["range"] = 0.toString()
            parameters["ukey"] = uKeyHex

            userAgent("QQClient")
        }

        configureBody(inputSize, imageInput)
    } == HttpStatusCode.OK
} finally {
    imageInput.close()
}

/**
 * 似乎没有必要. 服务器的返回永远都是 01 00 00 00 02 00 00
 */
@Deprecated("Useless packet")
@AnnotatedId(KnownPacketId.SUBMIT_IMAGE_FILE_NAME)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
object SubmitImageFilenamePacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        target: UInt,
        filename: String,
        sessionKey: ByteArray
    ): OutgoingPacket = buildOutgoingPacket {
        writeQQ(bot)
        writeHex(TIMProtocol.fixVer2)//?
        //writeHex("04 00 00 00 01 2E 01 00 00 69 35")

        encryptAndWrite(sessionKey) {
            writeByte(0x01)
            writeQQ(bot)
            writeQQ(target)
            writeZero(2)
            writeUByte(0x02u)
            writeRandom(1)
            writeHex("00 0A 00 01 00 01")
            val name = "UserDataImage:$filename"
            writeShort(name.length.toShort())
            writeStringUtf8(name)
            writeHex("00 00")
            writeRandom(2)//这个也与是哪个好友有关?
            writeHex("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2E 01")//35  02? 最后这个值是与是哪个好友有关

            //this.debugPrintThis("SubmitImageFilenamePacket")
        }

        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1A 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1B 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 28 5A 53 41 58 40 57 4B 52 4A 5A 31 7E 33 59 4F 53 53 4C 4D 32 4B 49 2E 6A 70 67 00 00 06 E2 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
        //解密body=01 3E 03 3F A2 7C BC D3 C1 00 00 27 1C 00 0A 00 01 00 01 00 30 55 73 65 72 44 61 74 61 43 75 73 74 6F 6D 46 61 63 65 3A 31 5C 29 37 42 53 4B 48 32 44 35 54 51 28 5A 35 7D 35 24 56 5D 32 35 49 4E 2E 6A 70 67 00 00 03 73 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 02 2F 02
    }

    @AnnotatedId(KnownPacketId.SUBMIT_IMAGE_FILE_NAME)
    @PacketVersion(date = "2019.10.19", timVersion = "2.3.2.21173")
    class Response(input: ByteReadPacket) : ResponsePacket(input) {
        override fun decode() = with(input) {
            require(readBytes().contentEquals(expecting))
        }

        companion object {
            private val expecting = byteArrayOf(0x01, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00)
        }
    }
}


/**
 * 请求上传图片. 将发送图片的 md5, size, width, height.
 * 服务器返回以下之一:
 * - 服务器已经存有这个图片
 * - 服务器未存有, 返回一个 key 用于客户端上传
 */
@AnnotatedId(KnownPacketId.FRIEND_IMAGE_ID)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
object FriendImageIdRequestPacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        sessionKey: ByteArray,
        target: UInt,
        image: ExternalImage
    ) = buildOutgoingPacket {
        writeQQ(bot)
        writeHex("04 00 00 00 01 2E 01 00 00 69 35 00 00 00 00 00 00 00 00")

        encryptAndWrite(sessionKey) {
            writeHex("00 00 00 07 00 00 00")

            writeUVarintLVPacket(lengthOffset = { it - 7 }) {
                writeUByte(0x08u)
                writeUShort(0x01_12u)
                writeUShort(0x03_98u)
                writeUShort(0x01_01u)
                writeUShort(0x08_01u)

                writeUVarintLVPacket(tag = 0x12u, lengthOffset = { it + 1 }) {
                    writeUByte(0x08u)
                    writeUVarInt(bot)

                    writeUByte(0x10u)
                    writeUVarInt(target)

                    writeUShort(0x18_00u)

                    writeUByte(0x22u)
                    writeUByte(0x10u)
                    writeFully(image.md5)

                    writeUByte(0x28u)
                    writeUVarInt(image.inputSize.toUInt())


                    writeUByte(0x32u)
                    //长度应为1A
                    writeUVarintLVPacket {
                        writeUShort(0x28_00u)
                        writeUShort(0x46_00u)
                        writeUShort(0x51_00u)
                        writeUShort(0x56_00u)
                        writeUShort(0x4B_00u)
                        writeUShort(0x41_00u)
                        writeUShort(0x49_00u)
                        writeUShort(0x25_00u)
                        writeUShort(0x4B_00u)
                        writeUShort(0x24_00u)
                        writeUShort(0x55_00u)
                        writeUShort(0x30_00u)
                        writeUShort(0x24_00u)
                    }

                    writeUShort(0x38_01u)
                    writeUShort(0x48_00u)

                    writeUByte(0x70u)
                    writeUVarInt(image.width.toUInt())
                    writeUByte(0x78u)
                    writeUVarInt(image.height.toUInt())
                }
            }
        }
    }

    @AnnotatedId(KnownPacketId.FRIEND_IMAGE_ID)
    @PacketVersion(date = "2019.11.1", timVersion = "2.3.2.21173")
    class Response(input: ByteReadPacket) : ResponsePacket(input) {
        /**
         * 访问 HTTP API 时需要使用的一个 key. 128 位
         */
        var uKey: ByteArray? = null

        /**
         * 发送消息时使用的 id
         */
        var imageId: ImageId? = null

        lateinit var state: State

        enum class State {
            /**
             * 需要上传. 此时 [uKey], [imageId] 均不为 `null`
             */
            REQUIRE_UPLOAD,
            /**
             * 服务器已有这个图片. 此时 [uKey] 为 `null`, [imageId] 不为 `null`
             */
            ALREADY_EXISTS,
            /**
             * 图片过大. 此时 [uKey], [imageId] 均为 `null`
             */
            OVER_FILE_SIZE_MAX,
        }

        override fun decode() = with(input) {
            discardExact(6)
            if (readUByte() != UByte.MIN_VALUE) {
                discardExact(60)

                @Suppress("ControlFlowWithEmptyBody")
                while (readUByte().toUInt() != 0x4Au);

                uKey = readBytes(readUnsignedVarInt().toInt())//128

                discardExact(1)//52, id
                imageId = ImageId(readString(readUnsignedVarInt().toInt()))//37
                state = REQUIRE_UPLOAD
            } else {
                val toDiscard = readUByte().toInt() - 37
                if (toDiscard < 0) {
                    state = OVER_FILE_SIZE_MAX
                } else {
                    discardExact(toDiscard)
                    imageId = ImageId(readString(37))
                    state = ALREADY_EXISTS
                }
            }
        }
    }
}


/**
 * 获取 Image Id 和上传用的一个 uKey
 */
@AnnotatedId(KnownPacketId.GROUP_IMAGE_ID)
@PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
object GroupImageIdRequestPacket : OutgoingPacketBuilder {
    operator fun invoke(
        bot: UInt,
        groupInternalId: GroupInternalId,
        image: ExternalImage,
        sessionKey: ByteArray
    ) = buildOutgoingPacket {
        writeQQ(bot)
        writeHex("04 00 00 00 01 01 01 00 00 68 20 00 00 00 00 00 00 00 00")

        encryptAndWrite(sessionKey) {
            writeHex("00 00 00 07 00 00 00")

            writeUVarintLVPacket(lengthOffset = { it - 7 }) {
                writeByte(0x08)
                writeHex("01 12 03 98 01 01 10 01 1A")

                writeUVarintLVPacket(lengthOffset = { it }) {
                    writeTUVarint(0x08u, groupInternalId.value)
                    writeTUVarint(0x10u, bot)
                    writeTV(0x1800u)

                    writeUByte(0x22u)
                    writeUByte(0x10u)
                    writeFully(image.md5)

                    writeTUVarint(0x28u, image.inputSize.toUInt())
                    writeUVarintLVPacket(tag = 0x32u) {
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
    }

    private val value0x6A: UByteArray = ubyteArrayOf(0x05u, 0x32u, 0x36u, 0x36u, 0x35u, 0x36u)

    @AnnotatedId(KnownPacketId.GROUP_IMAGE_ID)
    @PacketVersion(date = "2019.10.26", timVersion = "2.3.2.21173")
    class Response(input: ByteReadPacket) : ResponsePacket(input) {
        lateinit var state: State

        /**
         * 访问 HTTP API 时需要使用的一个 key. 128 位
         */
        var uKey: ByteArray? = null

        enum class State {
            /**
             * 需要上传. 此时 [uKey] 不为 `null`
             */
            REQUIRE_UPLOAD,
            /**
             * 服务器已有这个图片. 此时 [uKey] 为 `null`
             */
            ALREADY_EXISTS,
            /**
             * 图片过大. 此时 [uKey] 为 `null`
             */
            OVER_FILE_SIZE_MAX,
        }

        override fun decode(): Unit = with(input) {
            discardExact(6)//00 00 00 05 00 00

            val length = remaining - 128 - 14
            if (length < 0) {
                state = if (readUShort().toUInt() == 0x0025u) State.OVER_FILE_SIZE_MAX else State.ALREADY_EXISTS
                return@with
            }

            discardExact(length)
            uKey = readBytes(128)
            state = State.REQUIRE_UPLOAD
        }
    }
}