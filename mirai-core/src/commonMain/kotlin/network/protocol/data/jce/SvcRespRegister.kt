package net.mamoe.mirai.internal.network.protocol.data.jce

import kotlinx.serialization.Serializable
import net.mamoe.mirai.internal.utils.io.JceStruct
import net.mamoe.mirai.internal.utils.io.serialization.tars.TarsId


@Serializable
internal class SvcRespRegister(
    @JvmField @TarsId(0) val uin: Long = 0L,
    @JvmField @TarsId(1) val bid: Long = 0L,
    @JvmField @TarsId(2) val replyCode: Byte = 0,
    @JvmField @TarsId(3) val result: String = "",
    @JvmField @TarsId(4) val serverTime: Long = 0L,
    @JvmField @TarsId(5) val logQQ: Byte = 0,
    @JvmField @TarsId(6) val needKik: Byte = 0,
    @JvmField @TarsId(7) val updateFlag: Byte = 0,
    @JvmField @TarsId(8) val timeStamp: Long = 0L,
    @JvmField @TarsId(9) val crashFlag: Byte? = 0,
    @JvmField @TarsId(10) val clientIP: String = "",
    @JvmField @TarsId(11) val iClientPort: Int = 0,
    @JvmField @TarsId(12) val iHelloInterval: Int = 300,
    @JvmField @TarsId(13) val iLargeSeq: Long = 0L,
    @JvmField @TarsId(14) val largeSeqUpdate: Byte = 0,
    @JvmField @TarsId(15) val bytes_0x769_rspBody: ByteArray? = null,
    @JvmField @TarsId(16) val iStatus: Int? = 0
) : JceStruct
