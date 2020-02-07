package net.mamoe.mirai.qqandroid.network.highway

import kotlinx.io.core.Input
import kotlinx.io.core.use
import net.mamoe.mirai.qqandroid.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.network.QQAndroidClient
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.withUse
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.io.PlatformSocket
import net.mamoe.mirai.utils.io.discardExact

@UseExperimental(MiraiInternalAPI::class)
internal object HighwayHelper {

    suspend fun uploadImage(
        client: QQAndroidClient,
        uin: Long,
        serverIp: String,
        serverPort: Int,
        uKey: ByteArray,
        imageInput: Input,
        inputSize: Int,
        md5: ByteArray,
        commandId: Int  // group=2, friend=1
    ) {
        require(md5.size == 16) { "bad md5. Required size=16, got ${md5.size}" }
        require(uKey.size == 128) { "bad uKey. Required size=128, got ${uKey.size}" }
        val socket = PlatformSocket()
        socket.connect(serverIp, serverPort)
        socket.use {
            socket.send(
                Highway.RequestDataTrans(
                    uin = uin,
                    command = "PicUp.DataUp",
                    sequenceId = client.nextHighwayDataTransSequenceId(),
                    uKey = uKey,
                    data = imageInput,
                    dataSize = inputSize,
                    md5 = md5,
                    commandId = commandId
                )
            )

            //0A 3C 08 01 12 0A 31 39 39 34 37 30 31 30 32 31 1A 0C 50 69 63 55 70 2E 44 61 74 61 55 70 20 E9 A7 05 28 00 30 BD DB 8B 80 02 38 80 20 40 02 4A 0A 38 2E 32 2E 30 2E 31 32 39 36 50 84 10 12 3D 08 00 10 FD 08 18 00 20 FD 08 28 C6 01 38 00 42 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 4A 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 50 89 92 A2 FB 06 58 00 60 00 18 53 20 01 28 00 30 04 3A 00 40 E6 B7 F7 D9 80 2E 48 00 50 00
            socket.read().withUse {
                discardExact(1)
                val headLength = readInt()
                discardExact(4)
                val proto = readProtoBuf(CSDataHighwayHead.RspDataHighwayHead.serializer(), length = headLength)
                check(proto.errorCode == 0) { "image upload failed: Transfer errno=${proto.errorCode}" }
            }
        }
    }
}