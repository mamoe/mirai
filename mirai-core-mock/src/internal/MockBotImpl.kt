/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package net.mamoe.mirai.mock.internal

import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.AvatarSpec
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.friendgroup.FriendGroups
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.events.BotOnlineEvent
import net.mamoe.mirai.event.events.BotReloginEvent
import net.mamoe.mirai.internal.network.component.ComponentStorage
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.components.EventDispatcher
import net.mamoe.mirai.message.data.OnlineAudio
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockFriend
import net.mamoe.mirai.mock.contact.MockGroup
import net.mamoe.mirai.mock.contact.MockOtherClient
import net.mamoe.mirai.mock.contact.MockStranger
import net.mamoe.mirai.mock.database.MessageDatabase
import net.mamoe.mirai.mock.internal.components.MockEventDispatcherImpl
import net.mamoe.mirai.mock.internal.contact.*
import net.mamoe.mirai.mock.internal.contact.friendfroup.MockFriendGroups
import net.mamoe.mirai.mock.internal.contactbase.ContactDatabase
import net.mamoe.mirai.mock.internal.serverfs.TmpResourceServerImpl
import net.mamoe.mirai.mock.resserver.TmpResourceServer
import net.mamoe.mirai.mock.userprofile.UserProfileService
import net.mamoe.mirai.mock.utils.AvatarGenerator
import net.mamoe.mirai.mock.utils.NameGenerator
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import net.mamoe.mirai.utils.*
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import net.mamoe.mirai.internal.utils.subLoggerImpl as subLog

internal class MockBotImpl(
    override val configuration: BotConfiguration,
    override val id: Long,
    nick: String,
    override val nameGenerator: NameGenerator,
    override val tmpResourceServer: TmpResourceServer,
    override val msgDatabase: MessageDatabase,
    override val userProfileService: UserProfileService,
    override val avatarGenerator: AvatarGenerator,
) : MockBot, Bot, ContactOrBot {
    @JvmField
    internal val contactDatabase = ContactDatabase(this)
    private val botccinfo = contactDatabase.acquireCI(id, nick)

    private val loginBefore = AtomicBoolean(false)
    override var nickNoEvent: String by botccinfo::nick
    override var nick: String
        get() = botccinfo.nick
        set(value) {
            botccinfo.changeNick(value)
        }

    override var avatarUrl: String
        get() = asFriend.avatarUrl
        set(value) {
            asFriend.changeAvatarUrl(value)
        }

    override fun avatarUrl(spec: AvatarSpec): String {
        return avatarUrl
    }

    override val logger: MiraiLogger by lazy {
        configuration.botLoggerSupplier(this)
    }

    init {
        if (tmpResourceServer is TmpResourceServerImpl) {
            // Not using logger.subLogger caused by kotlin compile error
            tmpResourceServer.logger =
                subLog(this.logger, "TmpFsServer").takeUnless { it == this.logger } ?: kotlin.run {
                    MiraiLogger.Factory.create(TmpResourceServerImpl::class.java, "TFS $id")
                }
        }
        tmpResourceServer.startupServer()
        msgDatabase.connect()
    }

    val components: ComponentStorage by lazy {
        ConcurrentComponentStorage {
            set(EventDispatcher, MockEventDispatcherImpl(coroutineContext, logger))
        }
    }

    @TestOnly
    internal suspend fun joinEventBroadcast() {
        components[EventDispatcher].joinBroadcast()
    }

    override suspend fun login() {
        BotOnlineEvent(this).broadcast()
        if (!loginBefore.compareAndSet(false, true)) {
            BotReloginEvent(this, null).broadcast()
        }
    }


    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    override fun close(cause: Throwable?) {
        tmpResourceServer.close()
        Bot._instances.remove(id, this)
        cancel(when (cause) {
            null -> CancellationException("Bot cancelled")
            else -> CancellationException(cause.message).also { it.initCause(cause) }
        })
    }

    override val groups: ContactList<MockGroup> = ContactList()
    override val friends: ContactList<MockFriend> = ContactList()
    override val strangers: ContactList<MockStranger> = ContactList()
    override val otherClients: ContactList<MockOtherClient> = ContactList()
    override val friendGroups: FriendGroups = MockFriendGroups(this)

    @Suppress("DEPRECATION")
    override fun addGroup(id: Long, name: String): MockGroup =
        addGroup(id, Mirai.calculateGroupUinByGroupCode(id), name)

    override fun addGroup(id: Long, uin: Long, name: String): MockGroup {
        val group = MockGroupImpl(coroutineContext, this, id, uin, name)
        groups.delegate.add(group)
        group.appendMember(simpleMemberInfo(this.id, this.nick, permission = MemberPermission.OWNER))
        return group
    }

    override fun addFriend(id: Long, name: String): MockFriend {
        val friend = MockFriendImpl(coroutineContext, this, id, name, "")
        friends.delegate.add(friend)
        return friend
    }

    override fun addStranger(id: Long, name: String): MockStranger {
        val stranger = MockStrangerImpl(coroutineContext, this, id, "", name)
        strangers.delegate.add(stranger)
        return stranger
    }

    override val isOnline: Boolean get() = isActive
    override val eventChannel: EventChannel<BotEvent> =
        GlobalEventChannel.filterIsInstance<BotEvent>().filter { it.bot === this@MockBotImpl }

    override val asFriend: MockFriend by lazy {
        MockFriendImpl(coroutineContext, this, id, nick, "").also { basm ->
            @Suppress("QUALIFIED_SUPERTYPE_EXTENDED_BY_OTHER_SUPERTYPE", "RemoveExplicitSuperQualifier")
            basm.initAvatarUrl(super<ContactOrBot>.avatarUrl(spec = AvatarSpec.LARGEST))
        }
    }
    override val asStranger: MockStranger by lazy {
        MockStrangerImpl(coroutineContext, this, id, "", nick)
    }

    override val coroutineContext: CoroutineContext by lazy {
        configuration.parentCoroutineContext.childScopeContext()
    }

    override suspend fun uploadOnlineAudio(resource: ExternalResource): OnlineAudio {
        return resource.mockImplUploadAudioAsOnline(this)
    }

    override suspend fun uploadMockImage(resource: ExternalResource): MockImage {
        val md5 = resource.md5
        val format = resource.formatName

        // todo width, height ?
        return MockImage(
            imageId = generateImageId(md5, format),
            urlPath = bot.tmpResourceServer.uploadResourceAsImage(resource).toString(),
            size = resource.size
        )
    }

    override fun toString(): String {
        return "MockBot($id)"
    }
}

internal fun MockBot.impl(): MockBotImpl {
    contract { returns() implies (this@impl is MockBotImpl) }
    return cast()
}
