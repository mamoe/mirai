/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.EventChannel
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
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
import net.mamoe.mirai.mock.fsserver.TmpFsServer
import net.mamoe.mirai.mock.internal.components.MockEventDispatcherImpl
import net.mamoe.mirai.mock.internal.contact.MockFriendImpl
import net.mamoe.mirai.mock.internal.contact.MockGroupImpl
import net.mamoe.mirai.mock.internal.contact.MockStrangerImpl
import net.mamoe.mirai.mock.internal.contact.mockImplUploadAudioAsOnline
import net.mamoe.mirai.mock.internal.remotefile.FsServerImpl
import net.mamoe.mirai.mock.userprofile.UserProfileService
import net.mamoe.mirai.mock.utils.NameGenerator
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import net.mamoe.mirai.utils.*
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import net.mamoe.mirai.internal.utils.subLoggerImpl as subLog

internal class MockBotImpl(
    override val configuration: BotConfiguration,
    override val id: Long,
    nick: String,
    override val nameGenerator: NameGenerator,
    override val tmpFsServer: TmpFsServer,
    override val msgDatabase: MessageDatabase,
    override val userProfileService: UserProfileService,
) : MockBot, Bot, ContactOrBot {
    private val loginBefore = AtomicBoolean(false)
    override var nickNoEvent: String = nick
    override var nick: String
        get() = nickNoEvent
        set(value) {
            val ov = nickNoEvent
            if (value == ov) return
            nickNoEvent = value
            BotNickChangedEvent(this, ov, value).broadcastBlocking()
        }

    override var avatarUrl: String = ""
        get() {
            val f = field
            if (f.isEmpty()) {
                @Suppress("QUALIFIED_SUPERTYPE_EXTENDED_BY_OTHER_SUPERTYPE", "RemoveExplicitSuperQualifier")
                return super<ContactOrBot>.avatarUrl
            }
            return f
        }
        set(value) {
            field = value
            BotAvatarChangedEvent(this).broadcastBlocking()
        }

    override val logger: MiraiLogger by lazy {
        configuration.botLoggerSupplier(this)
    }

    init {
        if (tmpFsServer is FsServerImpl) {
            // Not using logger.subLogger caused by kotlin compile error
            tmpFsServer.logger = subLog(this.logger, "TmpFsServer").takeUnless { it == this.logger } ?: kotlin.run {
                MiraiLogger.Factory.create(TmpFsServer::class.java, "TFS $id")
            }
        }
        tmpFsServer.startup()
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
        tmpFsServer.close()
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

    override fun addGroup(id: Long, name: String): MockGroup =
        addGroup(id, Mirai.calculateGroupUinByGroupCode(id), name)

    override fun addGroup(id: Long, uin: Long, name: String): MockGroup {
        val group = MockGroupImpl(coroutineContext, this, id, uin, name)
        groups.delegate.add(group)
        group.addMember(simpleMemberInfo(this.id, this.nick, permission = MemberPermission.OWNER))
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
        MockFriendImpl(coroutineContext, this, id, nick, "")
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

    override fun toString(): String {
        return "MockBot<$nick, $id>"
    }
}