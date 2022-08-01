/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.packet.chat.image

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.proto.Cmd0x388
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacketFactory
import net.mamoe.mirai.internal.network.protocol.packet.buildOutgoingUniPacket
import net.mamoe.mirai.internal.utils.io.serialization.readProtoBuf
import net.mamoe.mirai.internal.utils.io.serialization.writeProtoBuf
import kotlin.random.Random
import kotlin.random.nextInt

internal fun getRandomString(length: Int): String =
    getRandomString(length, *defaultRanges)

private val defaultRanges: Array<CharRange> = arrayOf('a'..'z', 'A'..'Z', '0'..'9')

internal fun getRandomString(length: Int, vararg charRanges: CharRange): String =
    CharArray(length) { charRanges[Random.Default.nextInt(0..charRanges.lastIndex)].random() }.concatToString()

internal class ImgStore {
    object GroupPicUp : OutgoingPacketFactory<GroupPicUp.Response>("ImgStore.GroupPicUp") {

        operator fun invoke(
            client: QQAndroidClient,
            uin: Long,
            groupCode: Long,
            md5: ByteArray,
            size: Long,
            picWidth: Int = 0, // not orthodox
            picHeight: Int = 0, // not orthodox
            picType: Int = 2001,
            fileId: Long = 0,
            filename: String = getRandomString(16) + ".gif", // make server happier
            srcTerm: Int = 5,
            platformType: Int = 9,
            buType: Int = 1, // group 1, other 2
            appPicType: Int = 1006,
        ) = buildOutgoingUniPacket(client) {
            writeProtoBuf(
                Cmd0x388.ReqBody.serializer(),
                Cmd0x388.ReqBody(
                    netType = 3, // wifi
                    subcmd = 1,
                    msgTryupImgReq = listOf(
                        Cmd0x388.TryUpImgReq(
                            groupCode = groupCode,
                            srcUin = uin,
                            fileMd5 = md5,
                            fileSize = size,
                            fileId = fileId,
                            fileName = filename,
                            picWidth = picWidth,
                            picHeight = picHeight,
                            picType = picType,
                            appPicType = appPicType,
                            buildVer = client.buildVer,
                            srcTerm = srcTerm,
                            platformType = platformType,
                            //For gif, not original there
                            originalPic = if (picType == 2000) {
                                0
                            } else {
                                1
                            },
                            buType = buType
                        )
                    )
                )
            )
        }

        sealed class Response : Packet {
            class FileExists(
                val fileId: Long,
                val fileInfo: Cmd0x388.ImgInfo
            ) : Response() {
                override fun toString(): String {
                    return "FileExists(fileId=$fileId, fileInfo=$fileInfo)"
                }
            }

            class RequireUpload(
                val fileId: Long,
                val uKey: ByteArray,
                val uploadIpList: List<Int>,
                val uploadPortList: List<Int>
            ) : Response() {
                override fun toString(): String {
                    return "RequireUpload(fileId=$fileId, uKey=${uKey.contentToString()})"
                }
            }

            data class Failed(
                val resultCode: Int,
                val message: String
            ) : Response()
        }

        override suspend fun ByteReadPacket.decode(bot: QQAndroidBot): Response {
            val resp0 = readProtoBuf(Cmd0x388.RspBody.serializer())
            val resp =
                resp0.msgTryupImgRsp.firstOrNull() ?: error("cannot find `msgTryupImgRsp` from `Cmd0x388.RspBody`")
            return when {
                resp.result != 0 -> Response.Failed(resultCode = resp.result, message = resp.failMsg)
                resp.boolFileExit -> Response.FileExists(fileId = resp.fileid, fileInfo = resp.msgImgInfo!!)
                else -> Response.RequireUpload(
                    fileId = resp.fileid,
                    uKey = resp.upUkey,
                    uploadIpList = resp.uint32UpIp,
                    uploadPortList = resp.uint32UpPort
                )
            }
        }
    }
}