import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import net.mamoe.mirai.qqandroid.io.ProtoBuf

@Serializable
class Common : ProtoBuf {
    @Serializable
    class BindInfo(
        @SerialId(1) val friUin: Long = 0L,
        @SerialId(2) val friNick: String = "",
        @SerialId(3) val time: Long = 0L,
        @SerialId(4) val bindStatus: Int = 0
    ) : ProtoBuf

    @Serializable
    class MedalInfo(
        @SerialId(1) val id: Int = 0,
        @SerialId(2) val type: Int = 0,
        @SerialId(4) val seq: Long = 0,
        @SerialId(5) val name: String = "",
        @SerialId(6) val newflag: Int = 0,
        @SerialId(7) val time: Long = 0L,
        @SerialId(8) val msgBindFri: Common.BindInfo? = null,
        @SerialId(11) val desc: String = "",
        @SerialId(31) val level: Int = 0,
        @SerialId(36) val taskinfos: List<Common.MedalTaskInfo>? = null,
        @SerialId(40) val point: Int = 0,
        @SerialId(41) val pointLevel2: Int = 0,
        @SerialId(42) val pointLevel3: Int = 0,
        @SerialId(43) val seqLevel2: Long = 0,
        @SerialId(44) val seqLevel3: Long = 0,
        @SerialId(45) val timeLevel2: Long = 0L,
        @SerialId(46) val timeLevel3: Long = 0L,
        @SerialId(47) val descLevel2: String = "",
        @SerialId(48) val descLevel3: String = "",
        @SerialId(49) val endtime: Int = 0,
        @SerialId(50) val detailUrl: String = "",
        @SerialId(51) val detailUrl2: String = "",
        @SerialId(52) val detailUrl3: String = "",
        @SerialId(53) val taskDesc: String = "",
        @SerialId(54) val taskDesc2: String = "",
        @SerialId(55) val taskDesc3: String = "",
        @SerialId(56) val levelCount: Int = 0,
        @SerialId(57) val noProgress: Int = 0,
        @SerialId(58) val resource: String = "",
        @SerialId(59) val fromuinLevel: Int = 0,
        @SerialId(60) val unread: Int = 0,
        @SerialId(61) val unread2: Int = 0,
        @SerialId(62) val unread3: Int = 0
    ) : ProtoBuf

    @Serializable
    class MedalTaskInfo(
        @SerialId(1) val taskid: Int = 0,
        @SerialId(32) val int32TaskValue: Int = 0,
        @SerialId(33) val tarValue: Int = 0,
        @SerialId(34) val tarValueLevel2: Int = 0,
        @SerialId(35) val tarValueLevel3: Int = 0
    ) : ProtoBuf
}
