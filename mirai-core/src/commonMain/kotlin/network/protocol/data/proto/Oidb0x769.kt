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
internal class Oidb0x769 : ProtoBuf {
    @Serializable
    internal class Camera(
        @JvmField @ProtoNumber(1) val primary: Long = 0L,
        @JvmField @ProtoNumber(2) val secondary: Long = 0L,
        @JvmField @ProtoNumber(3) val flash: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class Config(
        @JvmField @ProtoNumber(1) val type: Int = 0,
        @JvmField @ProtoNumber(2) val version: Int = 0,
        @JvmField @ProtoNumber(3) val contentList: List<String> = emptyList(),
        @JvmField @ProtoNumber(4) val debugMsg: String = "",
        @JvmField @ProtoNumber(5) val msgContentList: List<Content> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class ConfigSeq(
        @JvmField @ProtoNumber(1) val type: Int = 0,
        @JvmField @ProtoNumber(2) val version: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class Content(
        @JvmField @ProtoNumber(1) val taskId: Int = 0,
        @JvmField @ProtoNumber(2) val compress: Int = 0,
        @JvmField @ProtoNumber(10) val content: ByteArray = EMPTY_BYTE_ARRAY
    ) : ProtoBuf

    @Serializable
    internal class CPU(
        @JvmField @ProtoNumber(1) val model: String = "",
        @JvmField @ProtoNumber(2) val cores: Int = 0,
        @JvmField @ProtoNumber(3) val frequency: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class DeviceInfo(
        @JvmField @ProtoNumber(1) val brand: String = "",
        @JvmField @ProtoNumber(2) val model: String = "",
        @JvmField @ProtoNumber(3) val os: OS? = null,
        @JvmField @ProtoNumber(4) val cpu: CPU? = null,
        @JvmField @ProtoNumber(5) val memory: Memory? = null,
        @JvmField @ProtoNumber(6) val storage: Storage? = null,
        @JvmField @ProtoNumber(7) val screen: Screen? = null,
        @JvmField @ProtoNumber(8) val camera: Camera? = null
    ) : ProtoBuf

    @Serializable
    internal class Memory(
        @JvmField @ProtoNumber(1) val total: Long = 0L,
        @JvmField @ProtoNumber(2) val process: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class OS(
        //1 IOS | 2 ANDROID | 3 OTHER
        @JvmField @ProtoNumber(1) val type: Int /* enum */ = 1,
        @JvmField @ProtoNumber(2) val version: String = "",
        @JvmField @ProtoNumber(3) val sdk: String = "",
        @JvmField @ProtoNumber(4) val kernel: String = "",
        @JvmField @ProtoNumber(5) val rom: String = ""
    ) : ProtoBuf

    @Serializable
    internal class QueryUinPackageUsageReq(
        @JvmField @ProtoNumber(1) val type: Int = 0,
        @JvmField @ProtoNumber(2) val uinFileSize: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class QueryUinPackageUsageRsp(
        @JvmField @ProtoNumber(1) val status: Int = 0,
        @JvmField @ProtoNumber(2) val leftUinNum: Long = 0L,
        @JvmField @ProtoNumber(3) val maxUinNum: Long = 0L,
        @JvmField @ProtoNumber(4) val proportion: Int = 0,
        @JvmField @ProtoNumber(10) val uinPackageUsedList: List<UinPackageUsedInfo> = emptyList()
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val configList: List<ConfigSeq> = emptyList(),
        @JvmField @ProtoNumber(2) val msgDeviceInfo: DeviceInfo? = null,
        @JvmField @ProtoNumber(3) val info: String = "",
        @JvmField @ProtoNumber(4) val province: String = "",
        @JvmField @ProtoNumber(5) val city: String = "",
        @JvmField @ProtoNumber(6) val reqDebugMsg: Int = 0,
        @JvmField @ProtoNumber(101) val queryUinPackageUsageReq: QueryUinPackageUsageReq? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val result: Int = 0,
        @JvmField @ProtoNumber(2) val configList: List<Config> = emptyList(),
        @JvmField @ProtoNumber(101) val queryUinPackageUsageRsp: QueryUinPackageUsageRsp? = null
    ) : ProtoBuf

    @Serializable
    internal class Screen(
        @JvmField @ProtoNumber(1) val model: String = "",
        @JvmField @ProtoNumber(2) val width: Int = 0,
        @JvmField @ProtoNumber(3) val height: Int = 0,
        @JvmField @ProtoNumber(4) val dpi: Int = 0,
        @JvmField @ProtoNumber(5) val multiTouch: Boolean = false
    ) : ProtoBuf

    @Serializable
    internal class Storage(
        @JvmField @ProtoNumber(1) val builtin: Long = 0L,
        @JvmField @ProtoNumber(2) val external: Long = 0L
    ) : ProtoBuf

    @Serializable
    internal class UinPackageUsedInfo(
        @JvmField @ProtoNumber(1) val ruleId: Int = 0,
        @JvmField @ProtoNumber(2) val author: String = "",
        @JvmField @ProtoNumber(3) val url: String = "",
        @JvmField @ProtoNumber(4) val uinNum: Long = 0L
    ) : ProtoBuf
}
