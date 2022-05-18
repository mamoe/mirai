/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.protocol.packet.chat.CheckableStruct
import net.mamoe.mirai.internal.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class Oidb0x6d7 : ProtoBuf {
    @Serializable
    internal class CreateFolderReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val parentFolderId: String = "",
        @JvmField @ProtoNumber(4) val folderName: String = "",
    ) : ProtoBuf

    @Serializable
    internal class CreateFolderRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val folderInfo: GroupFileCommon.FolderInfo? = null,
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class DeleteFolderReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val folderId: String = "",
    ) : ProtoBuf

    @Serializable
    internal class DeleteFolderRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class MoveFolderReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val folderId: String = "",
        @JvmField @ProtoNumber(4) val parentFolderId: String = "",
        @JvmField @ProtoNumber(5) val destFolderId: String = "",
    ) : ProtoBuf

    @Serializable
    internal class MoveFolderRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val folderInfo: GroupFileCommon.FolderInfo? = null,
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class RenameFolderReqBody(
        @JvmField @ProtoNumber(1) val groupCode: Long = 0L,
        @JvmField @ProtoNumber(2) val appId: Int = 0,
        @JvmField @ProtoNumber(3) val folderId: String = "",
        @JvmField @ProtoNumber(4) val newFolderName: String = "",
    ) : ProtoBuf

    @Serializable
    internal class RenameFolderRspBody(
        @ProtoNumber(1) override val int32RetCode: Int = 0,
        @ProtoNumber(2) override val retMsg: String = "",
        @JvmField @ProtoNumber(3) val clientWording: String = "",
        @JvmField @ProtoNumber(4) val folderInfo: GroupFileCommon.FolderInfo? = null,
    ) : ProtoBuf, CheckableStruct

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val createFolderReq: CreateFolderReqBody? = null,
        @JvmField @ProtoNumber(2) val deleteFolderReq: DeleteFolderReqBody? = null,
        @JvmField @ProtoNumber(3) val renameFolderReq: RenameFolderReqBody? = null,
        @JvmField @ProtoNumber(4) val moveFolderReq: MoveFolderReqBody? = null,
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val createFolderRsp: CreateFolderRspBody? = null,
        @JvmField @ProtoNumber(2) val deleteFolderRsp: DeleteFolderRspBody? = null,
        @JvmField @ProtoNumber(3) val renameFolderRsp: RenameFolderRspBody? = null,
        @JvmField @ProtoNumber(4) val moveFolderRsp: MoveFolderRspBody? = null,
    ) : ProtoBuf
}
        