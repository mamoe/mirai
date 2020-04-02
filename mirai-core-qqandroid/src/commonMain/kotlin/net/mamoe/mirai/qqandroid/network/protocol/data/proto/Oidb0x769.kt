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

class Oidb0x769 {
    @Serializable
    class RequestBody(
        @ProtoId(1) val rpt_config_list: List<ConfigSeq>
       // @SerialId(2) val msg_device_info: DeviceInfo,
       // @SerialId(3) val str_info: String = "",
       // @SerialId(4) val province: String,
       // @SerialId(5) val city: String,
       // @SerialId(6) val req_debug_msg: Int = 0,
       // @SerialId(101) val query_uin_package_usage_req: QueryUinPackageUsageReq
    ) : ProtoBuf

    @Serializable
    class QueryUinPackageUsageReq(
        @ProtoId(1) val type: Int,
        @ProtoId(2) val uinFileSize: Long = 0
    ): ProtoBuf

    @Serializable
    class ConfigSeq(
        @ProtoId(1) val type: Int, // uint
        @ProtoId(2) val version: Int // uint
    ): ProtoBuf

    @Serializable
    class DeviceInfo(
        @ProtoId(1) val brand: String,
        @ProtoId(2) val model: String
        //@SerialId(3) val os: OS,
        //@SerialId(4) val cpu: CPU,
        //@SerialId(5) val memory: Memory,
        //@SerialId(6) val storage: Storage,
        //@SerialId(7) val screen: Screen,
        //@SerialId(8) val camera: Camera
    ): ProtoBuf

    @Serializable
    class OS(
        @ProtoId(1) val type: Int = 1,
        @ProtoId(2) val version: String,
        @ProtoId(3) val sdk: String,
        @ProtoId(4) val kernel: String,
        @ProtoId(5) val rom: String
    ): ProtoBuf

    @Serializable
    class Camera(
        @ProtoId(1) val primary: Long,
        @ProtoId(2) val secondary: Long,
        @ProtoId(3) val flag: Boolean
    ): ProtoBuf

    @Serializable
    class CPU(
        @ProtoId(1) val model: String,
        @ProtoId(2) val frequency: Int,
        @ProtoId(3) val cores: Int
    ): ProtoBuf

    @Serializable
    class Memory(
        @ProtoId(1) val total: Int,
        @ProtoId(2) val process: Int
    ): ProtoBuf

    @Serializable
    class Screen(
        @ProtoId(1) val model: String,
        @ProtoId(2) val width: Int,
        @ProtoId(3) val height: Int,
        @ProtoId(4) val dpi: Int,
        @ProtoId(5) val multiTouch: Boolean
    ): ProtoBuf

    @Serializable
    class Storage(
        @ProtoId(1) val builtin: Int,
        @ProtoId(2) val external: Int
    ): ProtoBuf
}