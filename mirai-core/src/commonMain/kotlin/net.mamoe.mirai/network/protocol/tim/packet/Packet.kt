@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.network.protocol.tim.packet.NullPacketId.value
import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.network.protocol.tim.packet.event.ServerEventPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility

/**
 * 数据包.
 */
interface Packet {
    /**
     * 包序列 ID. 唯一. 所有包共用一个原子自增序列 ID 生成
     */
    val sequenceId: UShort

    /**
     * 包识别 ID
     */
    val packetId: PacketId
}

/**
 * ID Hex. 格式为 `00 00 00 00`
 */
val Packet.idHexString: String get() = (packetId.value.toInt().shl(16) or sequenceId.toInt()).toUHexString()

// region Packet id

/**
 * 包 ID
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AnnotatedId(
    val id: KnownPacketId
)

inline val AnnotatedId.value: UShort get() = id.value

/**
 * 通过 [value] 匹配一个 [KnownPacketId], 无匹配则返回一个 [UnknownPacketId].
 */
@Suppress("FunctionName")
fun PacketId(value: UShort): PacketId =
    KnownPacketId.values().firstOrNull { it.value == value } ?: UnknownPacketId(value)

/**
 * 包 ID.
 */
interface PacketId {
    val value: UShort
}

/**
 * 用于代表 `null`. 调用属性 [value] 时将会得到一个 [error]
 */
object NullPacketId : PacketId {
    override val value: UShort get() = error("Packet id is not initialized")
}

/**
 * 未知的 [PacketId]
 */
inline class UnknownPacketId(override inline val value: UShort) : PacketId

/**
 * 已知的 [PacketId]. 所有在 Mirai 中实现过的包都会使用这些 Id
 */
enum class KnownPacketId(override inline val value: UShort, internal inline val builder: OutgoingPacketBuilder?) :
    PacketId {
    inline TOUCH(0x0825u, TouchPacket),
    inline SESSION_KEY(0x0828u, RequestSessionPacket),
    inline LOGIN(0x0836u, SubmitPasswordPacket),
    inline CAPTCHA(0x00BAu, SubmitCaptchaPacket),
    inline SERVER_EVENT_1(0x00CEu, ServerEventPacket.EventResponse),
    inline SERVER_EVENT_2(0x0017u, ServerEventPacket.EventResponse),
    inline FRIEND_ONLINE_STATUS_CHANGE(0x0081u, null),
    inline CHANGE_ONLINE_STATUS(0x00_ECu, null),

    inline HEARTBEAT(0x0058u, HeartbeatPacket),
    inline S_KEY(0x001Du, RequestSKeyPacket),
    inline ACCOUNT_INFO(0x005Cu, RequestAccountInfoPacket),
    inline SEND_GROUP_MESSAGE(0x0002u, SendGroupMessagePacket),
    inline SEND_FRIEND_MESSAGE(0x00CDu, SendFriendMessagePacket),
    inline CAN_ADD_FRIEND(0x00A7u, CanAddFriendPacket),
    inline GROUP_IMAGE_ID(0x0388u, GroupImageIdRequestPacket),
    inline FRIEND_IMAGE_ID(0x0352u, FriendImageIdRequestPacket),

    inline REQUEST_PROFILE(0x00_31u, RequestProfilePicturePacket),
    @Suppress("DEPRECATION")
    inline SUBMIT_IMAGE_FILE_NAME(0x01_BDu, SubmitImageFilenamePacket),

    ;

    override fun toString(): String = builder?.let { it::class.simpleName } ?: this.name
}

// endregion

// region Internal utils

/**
 * 版本信息
 */
@Suppress("unused")
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
internal annotation class PacketVersion(val date: String, val timVersion: String)

private object PacketNameFormatter {
    private var longestNameLength: Int = 43
    fun adjustName(name: String): String =
        if (name.length > longestNameLength) {
            longestNameLength = name.length
            name
        } else " ".repeat(longestNameLength - name.length) + name
}

private object IgnoreIdListEquals : List<String> by listOf(
    "idHex",
    "id",
    "eventIdentity",
    "packetId",
    "sequenceIdInternal",
    "sequenceId",
    "fixedId",
    "idByteArray",
    "encoded",
    "packet",
    "EMPTY_ID_HEX",
    "input",
    "sequenceId",
    "output",
    "bot",
    "UninitializedByteReadPacket",
    "sessionKey"
)

private object IgnoreIdListInclude : List<String> by listOf(
    "Companion",
    "EMPTY_ID_HEX",
    "input",
    "output",
    "this\$",
    "\$\$delegatedProperties",
    "UninitializedByteReadPacket",
    "\$FU",
    "RefVolatile"
)

/**
 * 带有这个注解的 [Packet], 将不会被记录在 log 中.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class NoLog

/**
 * 这个方法会翻倍内存占用, 考虑修改.
 */
@Suppress("UNCHECKED_CAST")
internal fun Packet.packetToString(name: String = this::class.simpleName.toString()): String =
    PacketNameFormatter.adjustName(name + "(${this.idHexString})") +
            this::class.members
                .filterIsInstance<KProperty<*>>()
                .filterNot { it.isConst || it.isSuspend || it.visibility != KVisibility.PUBLIC }
                .filterNot { prop -> prop.name in IgnoreIdListEquals || IgnoreIdListInclude.any { it in prop.name } }
                .joinToString(", ", "{", "}") { it.briefDescription(this@packetToString) }

@Suppress("UNCHECKED_CAST")
private fun KProperty<*>.briefDescription(thisRef: Packet): String =
    try {
        when (this) {
            is KProperty0<*> -> get()
            is KProperty1<*, *> -> (this as KProperty1<in Packet, Any>).get(thisRef)
            else -> null
        }
    } catch (e: Exception) {
        null
    }.let { value: Any? ->
        @Suppress("UNCHECKED_CAST")
        name.replace("\$delegate", "") + "=" + when (value) {
            null -> "_"
            is ByteArray -> value.toUHexString()
            is UByteArray -> value.toUHexString()
            is ByteReadPacket -> "[ByteReadPacket(${value.remaining})]"
            is IoBuffer -> "[IoBuffer(${value.readRemaining})]"
            is Lazy<*> -> "[Lazy]"
            is ReadWriteProperty<*, *> -> (value as? ReadWriteProperty<Packet, *>)?.getValue(
                thisRef,
                this
            ) ?: "[UnknownProperty]"
            else -> value.toString().replace("\n", """\n""")
        }
    }

// endregion