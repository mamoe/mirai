/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import net.mamoe.mirai.utils.currentTimeSeconds
import kotlin.jvm.JvmField

internal interface ImgReq : ProtoBuf

// cmd0x352$ReqBody

@Serializable
internal class GetImgUrlReq(
    @ProtoId(1) @JvmField val srcUni: Int,
    @ProtoId(2) @JvmField val dstUni: Int,
    @ProtoId(3) @JvmField val fileResID: String,//UUID
    /**
     * UUID例子: 没有找到
     */
    @ProtoId(4) @JvmField val urlFlag: Int = 1,
    //5 unknown, 好像没用
    @ProtoId(6) @JvmField val urlType: Int = 4,
    @ProtoId(7) @JvmField val requestTerm: Int = 5,//确定
    @ProtoId(8) @JvmField val requestPlatformType: Int = 9,//确定
    @ProtoId(9) @JvmField val srcFileType: Int = 1,//2=ftn，1=picplatform，255
    @ProtoId(10) @JvmField val innerIP: Int = 0,//确定
    @ProtoId(11) @JvmField val addressBook: Int = 0,//[ChatType.internalID]== 1006为1[为CONTACT时] 我觉得发0没问题
    @ProtoId(12) @JvmField val buType: Int = 1,//确定
    @ProtoId(13) @JvmField val buildVer: String = "8.2.7.4410",//版本号
    @ProtoId(14) @JvmField val timestamp: Int = currentTimeSeconds.toInt(),//(pic_up_timestamp)
    @ProtoId(15) @JvmField val requestTransferType: Int = 1
) : ImgReq