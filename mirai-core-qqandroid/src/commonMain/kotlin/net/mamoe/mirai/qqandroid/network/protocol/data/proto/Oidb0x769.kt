package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoId
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

class Oidb0x769 {
    @Serializable
internal class RequestBody(
        @ProtoId(1) @JvmField val rpt_config_list: List<ConfigSeq>
        // @SerialId(2) @JvmField val msg_device_info: DeviceInfo,
        // @SerialId(3) @JvmField val str_info: String = "",
        // @SerialId(4) @JvmField val province: String,
        // @SerialId(5) @JvmField val city: String,
        // @SerialId(6) @JvmField val req_debug_msg: Int = 0,
        // @SerialId(101) @JvmField val query_uin_package_usage_req: QueryUinPackageUsageReq
    ) : ProtoBuf

    @Serializable
internal class QueryUinPackageUsageReq(
        @ProtoId(1) @JvmField val type: Int,
        @ProtoId(2) @JvmField val uinFileSize: Long = 0
    ): ProtoBuf

    @Serializable
internal class ConfigSeq(
        @ProtoId(1) @JvmField val type: Int, // uint
        @ProtoId(2) @JvmField val version: Int // uint
    ): ProtoBuf

    @Serializable
internal class DeviceInfo(
        @ProtoId(1) @JvmField val brand: String,
        @ProtoId(2) @JvmField val model: String
        //@SerialId(3) @JvmField val os: OS,
        //@SerialId(4) @JvmField val cpu: CPU,
        //@SerialId(5) @JvmField val memory: Memory,
        //@SerialId(6) @JvmField val storage: Storage,
        //@SerialId(7) @JvmField val screen: Screen,
        //@SerialId(8) @JvmField val camera: Camera
    ): ProtoBuf

    @Serializable
internal class OS(
        @ProtoId(1) @JvmField val type: Int = 1,
        @ProtoId(2) @JvmField val version: String,
        @ProtoId(3) @JvmField val sdk: String,
        @ProtoId(4) @JvmField val kernel: String,
        @ProtoId(5) @JvmField val rom: String
    ): ProtoBuf

    @Serializable
internal class Camera(
        @ProtoId(1) @JvmField val primary: Long,
        @ProtoId(2) @JvmField val secondary: Long,
        @ProtoId(3) @JvmField val flag: Boolean
    ): ProtoBuf

    @Serializable
internal class CPU(
        @ProtoId(1) @JvmField val model: String,
        @ProtoId(2) @JvmField val frequency: Int,
        @ProtoId(3) @JvmField val cores: Int
    ): ProtoBuf

    @Serializable
internal class Memory(
        @ProtoId(1) @JvmField val total: Int,
        @ProtoId(2) @JvmField val process: Int
    ): ProtoBuf

    @Serializable
internal class Screen(
        @ProtoId(1) @JvmField val model: String,
        @ProtoId(2) @JvmField val width: Int,
        @ProtoId(3) @JvmField val height: Int,
        @ProtoId(4) @JvmField val dpi: Int,
        @ProtoId(5) @JvmField val multiTouch: Boolean
    ): ProtoBuf

    @Serializable
internal class Storage(
        @ProtoId(1) @JvmField val builtin: Int,
        @ProtoId(2) @JvmField val external: Int
    ): ProtoBuf
}