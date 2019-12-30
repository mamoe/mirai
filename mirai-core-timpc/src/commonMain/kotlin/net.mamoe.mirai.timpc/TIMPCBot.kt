package net.mamoe.mirai.timpc

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.BotImpl
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.data.ImageLink
import net.mamoe.mirai.data.Packet
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.ImageId0x03
import net.mamoe.mirai.message.data.ImageId0x06
import net.mamoe.mirai.qqAccount
import net.mamoe.mirai.timpc.internal.RawGroupInfo
import net.mamoe.mirai.timpc.network.GroupImpl
import net.mamoe.mirai.timpc.network.MemberImpl
import net.mamoe.mirai.timpc.network.QQImpl
import net.mamoe.mirai.timpc.network.TIMPCBotNetworkHandler
import net.mamoe.mirai.timpc.network.handler.TemporaryPacketHandler
import net.mamoe.mirai.timpc.network.packet.KnownPacketId
import net.mamoe.mirai.timpc.network.packet.OutgoingPacket
import net.mamoe.mirai.timpc.network.packet.SessionKey
import net.mamoe.mirai.timpc.network.packet.action.*
import net.mamoe.mirai.timpc.network.packet.event.EventPacketFactory
import net.mamoe.mirai.timpc.network.packet.event.FriendOnlineStatusChangedPacket
import net.mamoe.mirai.timpc.network.packet.login.*
import net.mamoe.mirai.timpc.utils.assertUnreachable
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.logStacktrace
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmSynthetic

internal expect class TIMPCBot constructor(
    account: BotAccount,
    configuration: BotConfiguration
) : TIMPCBotBase

@UseExperimental(MiraiInternalAPI::class)
internal abstract class TIMPCBotBase constructor(
    account: BotAccount,
    configuration: BotConfiguration
) : BotImpl<TIMPCBotNetworkHandler>(account, configuration) {

    @UseExperimental(ExperimentalUnsignedTypes::class)
    companion object {
        init {
            KnownPacketId[0x0825u] = TouchPacket
            KnownPacketId[0x0828u] = RequestSessionPacket
            KnownPacketId[0x0836u] = SubmitPasswordPacket
            KnownPacketId[0x00BAu] = CaptchaPacket
            KnownPacketId[0x00CEu] = EventPacketFactory
            KnownPacketId[0x0017u] = EventPacketFactory
            KnownPacketId[0x0081u] = FriendOnlineStatusChangedPacket
            KnownPacketId[0x00ECu] = ChangeOnlineStatusPacket

            KnownPacketId[0x0058u] = HeartbeatPacket
            KnownPacketId[0x001Du] = RequestSKeyPacket
            KnownPacketId[0x005Cu] = RequestAccountInfoPacket
            KnownPacketId[0x0002u] = GroupPacket
            KnownPacketId[0x00CDu] = SendFriendMessagePacket
            KnownPacketId[0x00A7u] = CanAddFriendPacket
            KnownPacketId[0x00A8u] = AddFriendPacket
            KnownPacketId[0x00AEu] = RequestFriendAdditionKeyPacket
            KnownPacketId[0x0388u] = GroupImagePacket
            KnownPacketId[0x0352u] = FriendImagePacket

            KnownPacketId[0x0031u] = RequestProfileAvatarPacket
            KnownPacketId[0x003Cu] = RequestProfileDetailsPacket
            KnownPacketId[0x0126u] = QueryNicknamePacket

            KnownPacketId[0x01BCu] = QueryPreviousNamePacket

            KnownPacketId[0x003Eu] = QueryFriendRemarkPacket
            // 031F  查询 "新朋友" 记录

            // @Suppress("DEPRECATION")
            // inline SUBMIT_IMAGE_FILE_NAME(0x01BDu, SubmitImageFilenamePacket),
        }
    }

    inline val sessionKey: SessionKey get()= network.sessionKey

    override fun createNetworkHandler(coroutineContext: CoroutineContext): TIMPCBotNetworkHandler {
        return TIMPCBotNetworkHandler(coroutineContext, this as TIMPCBot)
    }

    final override suspend fun addFriend(id: Long, message: String?, remark: String?): AddFriendResult {
        return when (CanAddFriendPacket(qqAccount, id, sessionKey).sendAndExpect<CanAddFriendResponse>()) {
            is CanAddFriendResponse.AlreadyAdded -> AddFriendResult.ALREADY_ADDED
            is CanAddFriendResponse.Rejected -> AddFriendResult.REJECTED

            is CanAddFriendResponse.ReadyToAdd,
            is CanAddFriendResponse.RequireVerification -> {
                val key = RequestFriendAdditionKeyPacket(qqAccount, id, sessionKey).sendAndExpect<RequestFriendAdditionKeyPacket.Response>().key
                AddFriendPacket.RequestAdd(qqAccount, id, sessionKey, message, remark, key).sendAndExpect<AddFriendPacket.Response>()
                AddFriendResult.WAITING_FOR_APPROVAL
            } //这个做的是需要验证消息的情况, 不确定 ReadyToAdd 的是啥

            // 似乎 RequireVerification 和 ReadyToAdd 判断错了. 需要重新检查一下

            // TODO: 2019/11/11 需要验证问题的情况

            /*is CanAddFriendResponse.ReadyToAdd -> {
            // TODO: 2019/11/11 这不需要验证信息的情况

            //AddFriendPacket(qqAccount, id, sessionKey, ).sendAndExpectAsync<AddFriendPacket.Response>().await()
            TODO()
        }*/
        }
    }

    final override suspend fun approveFriendAddRequest(id: Long, remark: String?) {
        AddFriendPacket.Approve(qqAccount, sessionKey, 0, id, remark).sendAndExpect<AddFriendPacket.Response>()
    }


    // region contacts

    final override val groups: ContactList<Group> = ContactList(LockFreeLinkedList())
    final override val qqs: ContactList<QQ> = ContactList(LockFreeLinkedList())

    /**
     * 线程安全地获取缓存的 QQ 对象. 若没有对应的缓存, 则会创建一个.
     */
    @UseExperimental(MiraiInternalAPI::class)
    @JvmSynthetic
    final override fun getQQ(id: Long): QQ = qqs.delegate.getOrAdd(id) { QQ(id) }

    @UseExperimental(MiraiInternalAPI::class, ExperimentalUnsignedTypes::class)
    final override suspend fun getGroup(id: GroupId): Group = groups.delegate.getOrNull(id.value) ?: inline {
        val info: RawGroupInfo = try {
            when (val response =
                GroupPacket.QueryGroupInfo(qqAccount, id.toInternalId(), sessionKey).sendAndExpect<GroupPacket.InfoResponse>()) {
                is RawGroupInfo -> response
                is GroupNotFound -> throw GroupNotFoundException("id=${id.value}")
                else -> assertUnreachable()
            }
        } catch (e: Exception) {
            throw IllegalStateException("Cannot obtain group info for id ${id.value}", e)
        }

        return groups.delegate.getOrAdd(id.value) { Group(id, info) }
    }

    final override suspend fun getGroup(internalId: GroupInternalId): Group =
        getGroup0(internalId.toId().value)

    private suspend inline fun getGroup0(id: Long): Group =
        groups.delegate.getOrNull(id) ?: inline {
            val info: RawGroupInfo = try {
                GroupPacket.QueryGroupInfo(qqAccount, GroupId(id).toInternalId(), sessionKey).sendAndExpect()
            } catch (e: Exception) {
                e.logStacktrace()
                error("Cannot obtain group info for id $id")
            }

            return groups.delegate.getOrAdd(id) { Group(GroupId(id), info) }
        }

    @UseExperimental(MiraiInternalAPI::class)
    final override suspend fun getGroup(id: Long): Group = getGroup0(id.coerceAtLeastOrFail(0))


    internal suspend inline fun <reified P : Packet, R> OutgoingPacket.sendAndExpectAsync(
        checkSequence: Boolean = true,
        noinline handler: suspend (P) -> R
    ): Deferred<R> {
        val deferred: CompletableDeferred<R> = CompletableDeferred(coroutineContext[Job])
        network.temporaryPacketHandlers.addLast(
            TemporaryPacketHandler(
                expectationClass = P::class,
                deferred = deferred,
                checkSequence = if (checkSequence) this.sequenceId else null,
                callerContext = coroutineContext + deferred,
                handler = handler
            )
        )
        network.socket.sendPacket(this)
        return deferred
    }

    internal suspend inline fun <reified P : Packet> OutgoingPacket.sendAndExpectAsync(checkSequence: Boolean = true): Deferred<P> =
        sendAndExpectAsync<P, P>(checkSequence) { it }

    internal suspend inline fun <reified P : Packet, R> OutgoingPacket.sendAndExpect(
        checkSequence: Boolean = true,
        timeoutMillis: Long = 5.secondsToMillis,
        crossinline mapper: (P) -> R
    ): R = withTimeout(timeoutMillis) { sendAndExpectAsync<P, R>(checkSequence) { mapper(it) }.await() }

    internal suspend inline fun <reified P : Packet> OutgoingPacket.sendAndExpect(
        checkSequence: Boolean = true,
        timeoutMillis: Long = 5.secondsToMillis
    ): P = withTimeout(timeoutMillis) { sendAndExpectAsync<P, P>(checkSequence) { it }.await() }

    internal suspend inline fun OutgoingPacket.send() = network.socket.sendPacket(this)


    final override suspend fun Image.getLink(): ImageLink = when (val id = this.id) {
        is ImageId0x03 -> GroupImagePacket.RequestImageLink(qqAccount, sessionKey, id).sendAndExpect<GroupImageLink>().requireSuccess()
        is ImageId0x06 -> FriendImagePacket.RequestImageLink(qqAccount, sessionKey, id).sendAndExpect<FriendImageLink>()
        else -> assertUnreachable()
    }

    @Suppress("FunctionName")
    @PublishedApi
    internal fun CoroutineScope.Group(groupId: GroupId, info: RawGroupInfo): Group {
        return GroupImpl(this@TIMPCBotBase as TIMPCBot, groupId, coroutineContext).apply { this.info = info.parseBy(this); launch { startUpdater() } }
    }

    @Suppress("FunctionName")
    @PublishedApi
    internal fun Group.Member(qq: QQ, permission: MemberPermission): Member {
        return MemberImpl(qq, this, permission, coroutineContext).apply { launch { startUpdater() } }
    }

    @Suppress("FunctionName")
    internal fun CoroutineScope.QQ(id: Long): QQ =
        QQImpl(this@TIMPCBotBase as TIMPCBot, id, coroutineContext).apply { launch { startUpdater() } }
}

internal inline fun <R> inline(block: () -> R): R = block()

internal suspend inline fun TIMPCBot.sendPacket(toSend: OutgoingPacket) = this.network.socket.sendPacket(toSend)

/**
 * 以 [Bot] 作为接收器 (receiver) 并调用 [block], 返回 [block] 的返回值.
 * 这个方法将能帮助使用在 [Bot] 中定义的一些扩展方法
 */
@UseExperimental(ExperimentalContracts::class)
internal inline fun <R> Contact.withTIMPCBot(block: TIMPCBot.() -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return (bot as TIMPCBot).run(block)
}
