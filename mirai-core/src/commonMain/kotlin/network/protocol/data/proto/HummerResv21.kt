/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.internal.utils.io.ProtoBuf

@Serializable
internal class HummerResv21 : ProtoBuf {
    @Serializable
    internal class FileImgInfo(
        @JvmField @ProtoNumber(1) val fileWidth: Int = 0,
        @JvmField @ProtoNumber(2) val fileHeight: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ForwardExtFileInfo(
        @JvmField @ProtoNumber(1) val fileType: Int = 0,
        @JvmField @ProtoNumber(2) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(3) val receiverUin: Long = 0L,
        @JvmField @ProtoNumber(4) val fileUuid: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(5) val fileName: String = "",
        @JvmField @ProtoNumber(6) val fileSize: Long = 0L,
        @JvmField @ProtoNumber(7) val fileSha1: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(8) val fileMd5: ByteArray = EMPTY_BYTE_ARRAY,
        @JvmField @ProtoNumber(9) val int64DeadTime: Long = 0L,
        @JvmField @ProtoNumber(10) val imgWidth: Int = 0,
        @JvmField @ProtoNumber(11) val imgHeight: Int = 0,
        @JvmField @ProtoNumber(12) val videoDuration: Long = 0L,
        @JvmField @ProtoNumber(13) val busId: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class ResvAttr(
        @JvmField @ProtoNumber(1) val fileImageInfo: FileImgInfo? = null,
        @JvmField @ProtoNumber(2) val forwardExtFileInfo: ForwardExtFileInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class XtfSenderInfo(
        @JvmField @ProtoNumber(1) val lanIp: Int = 0,
        @JvmField @ProtoNumber(2) val lanPort: Int = 0,
        @JvmField @ProtoNumber(3) val lanSrkey: Long = 0L
    ) : ProtoBuf
}
        