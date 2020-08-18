package net.mamoe.mirai.qqandroid.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.qqandroid.utils.io.ProtoBuf
import kotlin.jvm.JvmField

internal class Oidb0x769 {
    @Serializable
    internal class RequestBody(
        @ProtoNumber(1) @JvmField val rpt_config_list: List<ConfigSeq>
        // @SerialId(2) @JvmField val msg_device_info: DeviceInfo,
        // @SerialId(3) @JvmField val str_info: String = "",
        // @SerialId(4) @JvmField val province: String,
        // @SerialId(5) @JvmField val city: String,
        // @SerialId(6) @JvmField val req_debug_msg: Int = 0,
        // @SerialId(101) @JvmField val query_uin_package_usage_req: QueryUinPackageUsageReq
    ) : ProtoBuf

    @Serializable
    internal class QueryUinPackageUsageReq(
        @ProtoNumber(1) @JvmField val type: Int,
        @ProtoNumber(2) @JvmField val uinFileSize: Long = 0
    ) : ProtoBuf

    @Serializable
    internal class ConfigSeq(
        @ProtoNumber(1) @JvmField val type: Int, // uint
        @ProtoNumber(2) @JvmField val version: Int // uint
    ) : ProtoBuf

    @Serializable
    internal class DeviceInfo(
        @ProtoNumber(1) @JvmField val brand: String,
        @ProtoNumber(2) @JvmField val model: String
        //@SerialId(3) @JvmField val os: OS,
        //@SerialId(4) @JvmField val cpu: CPU,
        //@SerialId(5) @JvmField val memory: Memory,
        //@SerialId(6) @JvmField val storage: Storage,
        //@SerialId(7) @JvmField val screen: Screen,
        //@SerialId(8) @JvmField val camera: Camera
    ) : ProtoBuf

    @Serializable
    internal class OS(
        @ProtoNumber(1) @JvmField val type: Int = 1,
        @ProtoNumber(2) @JvmField val version: String,
        @ProtoNumber(3) @JvmField val sdk: String,
        @ProtoNumber(4) @JvmField val kernel: String,
        @ProtoNumber(5) @JvmField val rom: String
    ) : ProtoBuf

    @Serializable
    internal class Camera(
        @ProtoNumber(1) @JvmField val primary: Long,
        @ProtoNumber(2) @JvmField val secondary: Long,
        @ProtoNumber(3) @JvmField val flag: Boolean
    ) : ProtoBuf

    @Serializable
    internal class CPU(
        @ProtoNumber(1) @JvmField val model: String,
        @ProtoNumber(2) @JvmField val frequency: Int,
        @ProtoNumber(3) @JvmField val cores: Int
    ) : ProtoBuf

    @Serializable
    internal class Memory(
        @ProtoNumber(1) @JvmField val total: Int,
        @ProtoNumber(2) @JvmField val process: Int
    ) : ProtoBuf

    @Serializable
    internal class Screen(
        @ProtoNumber(1) @JvmField val model: String,
        @ProtoNumber(2) @JvmField val width: Int,
        @ProtoNumber(3) @JvmField val height: Int,
        @ProtoNumber(4) @JvmField val dpi: Int,
        @ProtoNumber(5) @JvmField val multiTouch: Boolean
    ) : ProtoBuf

    @Serializable
    internal class Storage(
        @ProtoNumber(1) @JvmField val builtin: Int,
        @ProtoNumber(2) @JvmField val external: Int
    ) : ProtoBuf
}