@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.network.protocol.tim.packet

import kotlinx.atomicfu.atomic
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.IoBuffer
import net.mamoe.mirai.event.events.FriendConversationInitializedEvent
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MemberPermissionChangedEvent
import net.mamoe.mirai.message.MessageChain
import net.mamoe.mirai.message.NullMessageChain
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.network.protocol.tim.packet.action.*
import net.mamoe.mirai.network.protocol.tim.packet.event.EventPacketFactory
import net.mamoe.mirai.network.protocol.tim.packet.event.SenderPermission
import net.mamoe.mirai.network.protocol.tim.packet.login.*
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

/**
 * 一种数据包的处理工厂. 它可以解密解码服务器发来的这个包, 也可以编码加密要发送给服务器的这个包
 * 应由一个 `object` 实现, 且实现 `operator fun invoke`
 *
 * @param TPacket 服务器回复包解析结果
 * @param TDecrypter 服务器回复包解密器
 */
abstract class PacketFactory<out TPacket : Packet, TDecrypter : Decrypter>(internal val decrypterType: DecrypterType<TDecrypter>) {

    /**
     * 2 Ubyte.
     * 读取注解 [AnnotatedId]
     */
    private val annotatedId: AnnotatedId
        get() = (this::class.annotations.firstOrNull { it is AnnotatedId } as? AnnotatedId)
            ?: error("Annotation AnnotatedId not found")

    /**
     * 包 ID.
     */
    open val id: PacketId by lazy { annotatedId.id }

    init {
        @Suppress("LeakingThis")
        PacketFactoryList.add(this)
    }

    /**
     * **解码**服务器的回复数据包
     */
    abstract suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): TPacket

    /**
     * **解密**服务器的回复数据包. 这个函数将会被 [BotNetworkHandler]
     */
    open fun decrypt(input: ByteReadPacket, decrypter: TDecrypter): ByteReadPacket = decrypter.decrypt(input)

    companion object {
        private val sequenceIdInternal = atomic(1)

        @PublishedApi
        internal fun atomicNextSequenceId(): UShort = sequenceIdInternal.getAndIncrement().toUShort()
    }
}

object PacketFactoryList : MutableList<PacketFactory<*, *>> by mutableListOf()


object UnknownPacketFactory : SessionPacketFactory<UnknownPacket>() {
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): UnknownPacket {

        return UnknownPacket
    }
}

object IgnoredPacketFactory : SessionPacketFactory<IgnoredPacket>() {
    override suspend fun ByteReadPacket.decode(id: PacketId, sequenceId: UShort, handler: BotNetworkHandler<*>): IgnoredPacket {
        return IgnoredPacket
    }
}

// region Packet id

/**
 * 通过 [value] 匹配一个 [IgnoredPacketId] 或 [KnownPacketId], 无匹配则返回一个 [UnknownPacketId].
 */
@Suppress("FunctionName")
fun PacketId(value: UShort): PacketId =
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
object NullPacketId : PacketId {
    override val factory: PacketFactory<*, *> get() = error("uninitialized")
    override val value: UShort get() = error("uninitialized")
}

/**
 * 未知的 [PacketId]
 */
inline class UnknownPacketId(override inline val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = UnknownPacketFactory
}

object IgnoredPacketIds : List<IgnoredPacketId> by {
    listOf<UShort>(
    ).map { IgnoredPacketId(it.toUShort()) }
}()

inline class IgnoredPacketId constructor(override val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = IgnoredPacketFactory
}

/**
 * 已知的 [PacketId]. 所有在 Mirai 中实现过的包都会使用这些 Id
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

    inline REQUEST_PROFILE_AVATAR(0x0031u, RequestProfilePicturePacket),
    inline REQUEST_PROFILE_DETAILS(0x003Cu, RequestProfilePicturePacket),
    // @Suppress("DEPRECATION")
    // inline SUBMIT_IMAGE_FILE_NAME(0x01BDu, SubmitImageFilenamePacket),

    ;

    override fun toString(): String = factory.let { it::class.simpleName } ?: this.name
}

// endregion

object IgnoredPacket : Packet

sealed class EventPacket {
    class GroupFileUpload(inline val xmlMessage: String) : Packet

    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
    class AndroidDeviceStatusChange(inline val kind: Kind) : Packet {
        enum class Kind {
            ONLINE,
            OFFLINE
        }
    }

    @CorrespondingEvent(MemberPermissionChangedEvent::class)
    class MemberPermissionChange : Packet {
        var groupId: UInt = 0u
        var qq: UInt = 0u
        lateinit var kind: MemberPermissionChangedEvent.Kind
    }

    @CorrespondingEvent(FriendConversationInitializedEvent::class)
    @PacketVersion(date = "2019.11.2", timVersion = "2.3.2.21173")
    class FriendConversationInitialize : Packet {
        var qq: UInt = 0u
    }

    @CorrespondingEvent(FriendMessageEvent::class)
    data class FriendMessage(
        val qq: UInt,
        /**
         * 是否是在这次登录之前的消息, 即消息记录
         */
        val isPrevious: Boolean,
        val message: MessageChain
    ) : Packet

    @CorrespondingEvent(GroupMessageEvent::class)
    class GroupMessage : Packet { // TODO: 2019/11/6 改为 data class
        var groupNumber: UInt = 0u
            internal set
        var qq: UInt = 0u
            internal set
        lateinit var senderName: String
            internal set
        /**
         * 发送方权限.
         */
        lateinit var senderPermission: SenderPermission
            internal set
        var message: MessageChain = NullMessageChain
            internal set
    }
}

/**
 * 一个包的数据 (body)
 */
@Suppress("unused")
interface Packet
//// TODO: 2019/11/5 Packet.toString

/**
 * 未知的包.
 */
object UnknownPacket : Packet {
    // TODO: 2019/11/5 添加包数据用于调试
}

/**
 * 仅用于替换类型应为 [Unit] 的情况
 */
object NoPacket : Packet


// region Internal utils

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
 * 这个方法会翻倍内存占用, 考虑修改.
 */
@Suppress("UNCHECKED_CAST")
internal fun Packet.packetToString(id: UShort, sequenceId: UShort, name: String = this::class.simpleName.toString()): String =
    id.toString()
/*PacketNameFormatter.adjustName(name + "(${(id.toInt().shl(16) or sequenceId.toInt()).toUHexString()})") +
        this::class.members
            .filterIsInstance<KProperty<*>>()
            .filterNot { it.isConst || it.isSuspend || it.visibility != KVisibility.PUBLIC }
            .filterNot { prop -> prop.name in IgnoreIdListEquals || IgnoreIdListInclude.any { it in prop.name } }
            .joinToString(", ", "{", "}") { it.briefDescription(this@packetToString) }*/

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