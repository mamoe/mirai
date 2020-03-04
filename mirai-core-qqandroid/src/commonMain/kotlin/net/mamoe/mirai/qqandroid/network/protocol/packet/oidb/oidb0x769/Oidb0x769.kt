/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.qqandroid.network.protocol.packet.oidb.oidb0x769

import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf

class Oidb0x769 {
    @Serializable
    class RequestBody(
        @SerialId(1) val rpt_config_list: List<ConfigSeq>
       // @SerialId(2) val msg_device_info: DeviceInfo,
       // @SerialId(3) val str_info: String = "",
       // @SerialId(4) val province: String,
       // @SerialId(5) val city: String,
       // @SerialId(6) val req_debug_msg: Int = 0,
       // @SerialId(101) val query_uin_package_usage_req: QueryUinPackageUsageReq
    ) : ProtoBuf

    @Serializable
    class QueryUinPackageUsageReq(
        @SerialId(1) val type: Int,
        @SerialId(2) val uinFileSize: Long = 0
    ): ProtoBuf

    @Serializable
    class ConfigSeq(
        @SerialId(1) val type: Int, // uint
        @SerialId(2) val version: Int // uint
    ): ProtoBuf

    @Serializable
    class DeviceInfo(
        @SerialId(1) val brand: String,
        @SerialId(2) val model: String
        //@SerialId(3) val os: OS,
        //@SerialId(4) val cpu: CPU,
        //@SerialId(5) val memory: Memory,
        //@SerialId(6) val storage: Storage,
        //@SerialId(7) val screen: Screen,
        //@SerialId(8) val camera: Camera
    ): ProtoBuf

    @Serializable
    class OS(
        @SerialId(1) val type: Int = 1,
        @SerialId(2) val version: String,
        @SerialId(3) val sdk: String,
        @SerialId(4) val kernel: String,
        @SerialId(5) val rom: String
    ): ProtoBuf

    @Serializable
    class Camera(
        @SerialId(1) val primary: Long,
        @SerialId(2) val secondary: Long,
        @SerialId(3) val flag: Boolean
    ): ProtoBuf

    @Serializable
    class CPU(
        @SerialId(1) val model: String,
        @SerialId(2) val frequency: Int,
        @SerialId(3) val cores: Int
    ): ProtoBuf

    @Serializable
    class Memory(
        @SerialId(1) val total: Int,
        @SerialId(2) val process: Int
    ): ProtoBuf

    @Serializable
    class Screen(
        @SerialId(1) val model: String,
        @SerialId(2) val width: Int,
        @SerialId(3) val height: Int,
        @SerialId(4) val dpi: Int,
        @SerialId(5) val multiTouch: Boolean
    ): ProtoBuf

    @Serializable
    class Storage(
        @SerialId(1) val builtin: Int,
        @SerialId(2) val external: Int
    ): ProtoBuf
}