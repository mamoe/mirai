@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.network.protocol.tim.packet.event.EventPacketFactory
import net.mamoe.mirai.network.protocol.tim.packet.event.FriendOnlineStatusChangedPacket
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.io.toUHexString


/**
 * 通过 [value] 匹配一个 [IgnoredPacketId] 或 [KnownPacketId], 无匹配则返回一个 [UnknownPacketId].
 */
fun matchPacketId(value: UShort): PacketId =
    IgnoredPacketIds.firstOrNull { it.value == value } ?: KnownPacketId.values().firstOrNull { it.value == value } ?: UnknownPacketId(value)

/**
 * 包 ID.
 */
interface PacketId {
    val value: UShort
    val factory: PacketFactory<*, *>
}

/**
 * 用于代表 `null`. 调用任何属性时都将会得到一个 [error]
 */
@Suppress("unused")
object NullPacketId : PacketId {
    override val factory: PacketFactory<*, *> get() = error("uninitialized")
    override val value: UShort get() = error("uninitialized")
    override fun toString(): String = "NullPacketId"
}

/**
 * 未知的 [PacketId]
 */
inline class UnknownPacketId(override inline val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = UnknownPacketFactory
    override fun toString(): String = "UnknownPacketId(${value.toUHexString()})"
}

object IgnoredPacketIds : List<IgnoredPacketId> by {
    listOf<UShort>(
    ).map { IgnoredPacketId(it.toUShort()) }
}()

inline class IgnoredPacketId constructor(override val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = IgnoredPacketFactory
    override fun toString(): String = "IgnoredPacketId(${value.toUHexString()})"
}

/**
 * 已知的 [matchPacketId]. 所有在 Mirai 中实现过的包都会使用这些 Id
 */
@Suppress("unused")
enum class KnownPacketId(override inline val value: UShort, override inline val factory: PacketFactory<*, *>) :
    PacketId {
    inline TOUCH(0x0825u, TouchPacket),
    inline SESSION_KEY(0x0828u, RequestSessionPacket),
    inline LOGIN(0x0836u, SubmitPasswordPacket),
    inline CAPTCHA(0x00BAu, CaptchaPacket),
    inline SERVER_EVENT_1(0x00CEu, EventPacketFactory),
    inline SERVER_EVENT_2(0x0017u, EventPacketFactory),
    inline FRIEND_ONLINE_STATUS_CHANGE(0x0081u, FriendOnlineStatusChangedPacket),
    inline CHANGE_ONLINE_STATUS(0x00ECu, ChangeOnlineStatusPacket),

    inline HEARTBEAT(0x0058u, HeartbeatPacket),
    inline S_KEY(0x001Du, RequestSKeyPacket),
    inline ACCOUNT_INFO(0x005Cu, RequestAccountInfoPacket),
    inline SEND_GROUP_MESSAGE(0x0002u, SendGroupMessagePacket),
    inline SEND_FRIEND_MESSAGE(0x00CDu, SendFriendMessagePacket),
    inline CAN_ADD_FRIEND(0x00A7u, CanAddFriendPacket),
    inline GROUP_IMAGE_ID(0x0388u, GroupImageIdRequestPacket),
    inline FRIEND_IMAGE_ID(0x0352u, FriendImageIdRequestPacket),

    inline REQUEST_PROFILE_AVATAR(0x0031u, RequestProfileAvatarPacket),
    inline REQUEST_PROFILE_DETAILS(0x003Cu, RequestProfileDetailsPacket),
    // @Suppress("DEPRECATION")
    // inline SUBMIT_IMAGE_FILE_NAME(0x01BDu, SubmitImageFilenamePacket),

    ;

    override fun toString(): String = (factory::class.simpleName ?: this.name) + "(${value.toUHexString()})"
}
