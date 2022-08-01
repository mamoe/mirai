/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import kotlin.jvm.JvmField

@Serializable
internal class GroupFileUploadExt(
    @JvmField @ProtoNumber(1) val u1: Int,
    @JvmField @ProtoNumber(2) val u2: Int,
    @JvmField @ProtoNumber(100) val entry: GroupFileUploadEntry,
    @JvmField @ProtoNumber(3) val u3: Int,
) : ProtoBuf

@Serializable
internal class GroupFileUploadEntry(
    @JvmField @ProtoNumber(100) val business: ExcitingBusiInfo,
    @JvmField @ProtoNumber(200) val fileEntry: ExcitingFileEntry,
    @JvmField @ProtoNumber(300) val clientInfo: ExcitingClientInfo,
    @JvmField @ProtoNumber(400) val fileNameInfo: ExcitingFileNameInfo,
    @JvmField @ProtoNumber(500) val host: ExcitingHostConfig,
) : ProtoBuf

@Serializable
internal class ExcitingBusiInfo(
    @JvmField @ProtoNumber(1) val busId: Int,
    @JvmField @ProtoNumber(100) val senderUin: Long,
    @JvmField @ProtoNumber(200) val receiverUin: Long, // maybe
    @JvmField @ProtoNumber(400) val groupCode: Long, // maybe
) : ProtoBuf

@Serializable
internal class ExcitingFileEntry(
    @JvmField @ProtoNumber(100) val fileSize: Long,
    @JvmField @ProtoNumber(200) val md5: ByteArray,
    @JvmField @ProtoNumber(300) val sha1: ByteArray,
    @JvmField @ProtoNumber(600) val fileId: ByteArray,
    @JvmField @ProtoNumber(700) val uploadKey: ByteArray,
) : ProtoBuf


@Serializable
internal class ExcitingClientInfo(
    @JvmField @ProtoNumber(100) val clientType: Int, // maybe
    @JvmField @ProtoNumber(200) val appId: String,
    @JvmField @ProtoNumber(300) val terminalType: Int,
    @JvmField @ProtoNumber(400) val clientVer: String,
    @JvmField @ProtoNumber(600) val unknown: Int,
) : ProtoBuf

@Serializable
internal class ExcitingFileNameInfo(
    @JvmField @ProtoNumber(100) val filename: String,
) : ProtoBuf

@Serializable
internal class ExcitingHostConfig(
    @JvmField @ProtoNumber(200) val hosts: List<ExcitingHostInfo>,
) : ProtoBuf

@Serializable
internal class ExcitingHostInfo(
    @JvmField @ProtoNumber(1) val url: ExcitingUrlInfo,
    @JvmField @ProtoNumber(2) val port: Int,
) : ProtoBuf

@Serializable
internal class ExcitingUrlInfo(
    @JvmField @ProtoNumber(1) val unknown: Int,
    @JvmField @ProtoNumber(2) val host: String,
) : ProtoBuf