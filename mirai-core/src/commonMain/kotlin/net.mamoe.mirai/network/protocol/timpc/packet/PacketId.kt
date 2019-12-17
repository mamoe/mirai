@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.timpc.packet

import net.mamoe.mirai.network.protocol.timpc.packet.action.*
import net.mamoe.mirai.network.protocol.timpc.packet.event.EventPacketFactory
import net.mamoe.mirai.network.protocol.timpc.packet.event.FriendOnlineStatusChangedPacket
import net.mamoe.mirai.network.protocol.timpc.packet.login.*
import net.mamoe.mirai.utils.io.toUHexString


/**
 * 通过 [value] 匹配一个 [IgnoredPacketId] 或 [KnownPacketId], 无匹配则返回一个 [UnknownPacketId].
 */
internal fun matchPacketId(value: UShort): PacketId =
    IgnoredPacketIds.firstOrNull { it.value == value } ?: KnownPacketId.values().firstOrNull { it.value == value } ?: UnknownPacketId(value)

/**
 * 包 ID.
 */
internal interface PacketId {
    val value: UShort
    val factory: PacketFactory<*, *>
}

/**
 * 用于代表 `null`. 调用任何属性时都将会得到一个 [error]
 */
@Suppress("unused")
internal object NullPacketId : PacketId {
    override val factory: PacketFactory<*, *> get() = error("uninitialized")
    override val value: UShort get() = error("uninitialized")
    override fun toString(): String = "NullPacketId"
}

/**
 * 未知的 [PacketId]
 */
internal inline class UnknownPacketId(override inline val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = UnknownPacketFactory
    override fun toString(): String = "UnknownPacketId(${value.toUHexString()})"
}

internal object IgnoredPacketIds : List<IgnoredPacketId> by {
    listOf<UShort>(
    ).map { IgnoredPacketId(it.toUShort()) }
}()

internal inline class IgnoredPacketId constructor(override val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = IgnoredPacketFactory
    override fun toString(): String = "IgnoredPacketId(${value.toUHexString()})"
}

/**
 * 已知的 [matchPacketId]. 所有在 Mirai 中实现过的包都会使用这些 Id
 */
@Suppress("unused")
internal enum class KnownPacketId(override val value: UShort, override val factory: PacketFactory<*, *>) :
    PacketId {
    TOUCH(0x0825u, TouchPacket),
    SESSION_KEY(0x0828u, RequestSessionPacket),
    LOGIN(0x0836u, SubmitPasswordPacket),
    CAPTCHA(0x00BAu, CaptchaPacket),
    SERVER_EVENT_1(0x00CEu, EventPacketFactory),
    SERVER_EVENT_2(0x0017u, EventPacketFactory),
    FRIEND_ONLINE_STATUS_CHANGE(0x0081u, FriendOnlineStatusChangedPacket),
    CHANGE_ONLINE_STATUS(0x00ECu, ChangeOnlineStatusPacket),

    HEARTBEAT(0x0058u, HeartbeatPacket),
    S_KEY(0x001Du, RequestSKeyPacket),
    ACCOUNT_INFO(0x005Cu, RequestAccountInfoPacket),
    GROUP_PACKET(0x0002u, GroupPacket),
    SEND_FRIEND_MESSAGE(0x00CDu, SendFriendMessagePacket),
    CAN_ADD_FRIEND(0x00A7u, CanAddFriendPacket),
    ADD_FRIEND(0x00A8u, AddFriendPacket),
    REQUEST_FRIEND_ADDITION_KEY(0x00AEu, RequestFriendAdditionKeyPacket),
    GROUP_IMAGE_ID(0x0388u, GroupImagePacket),
    FRIEND_IMAGE_ID(0x0352u, FriendImagePacket),

    REQUEST_PROFILE_AVATAR(0x0031u, RequestProfileAvatarPacket),
    REQUEST_PROFILE_DETAILS(0x003Cu, RequestProfileDetailsPacket),
    QUERY_NICKNAME(0x0126u, QueryNicknamePacket),

    QUERY_PREVIOUS_NAME(0x01BCu, QueryPreviousNamePacket),

    QUERY_FRIEND_REMARK(0x003Eu, QueryFriendRemarkPacket)
    // 031F  查询 "新朋友" 记录


    // @Suppress("DEPRECATION")
    // inline SUBMIT_IMAGE_FILE_NAME(0x01BDu, SubmitImageFilenamePacket),

    ;

    init {
        factory._id = this
    }

    override fun toString(): String = (factory::class.simpleName ?: this.name) + "(${value.toUHexString()})"
}
