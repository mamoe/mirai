/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE", "CANNOT_OVERRIDE_INVISIBLE_MEMBER")

package net.mamoe.mirai.mock.internal.contact

import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.announcement.OfflineAnnouncement
import net.mamoe.mirai.contact.announcement.buildAnnouncementParameters
import net.mamoe.mirai.contact.file.AbsoluteFile
import net.mamoe.mirai.contact.file.RemoteFiles
import net.mamoe.mirai.contact.roaming.RoamingMessages
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.internal.contact.uin
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.mock.MockBot
import net.mamoe.mirai.mock.contact.MockAnonymousMember
import net.mamoe.mirai.mock.contact.MockGroup
import net.mamoe.mirai.mock.contact.MockGroupControlPane
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.contact.active.MockGroupActive
import net.mamoe.mirai.mock.contact.essence.MockEssences
import net.mamoe.mirai.mock.internal.contact.active.MockGroupActiveImpl
import net.mamoe.mirai.mock.internal.contact.essence.MockEssencesImpl
import net.mamoe.mirai.mock.internal.contact.roaming.MockRoamingMessages
import net.mamoe.mirai.mock.internal.msgsrc.OnlineMsgSrcToGroup
import net.mamoe.mirai.mock.internal.msgsrc.newMsgSrc
import net.mamoe.mirai.mock.utils.broadcastBlocking
import net.mamoe.mirai.mock.utils.mock
import net.mamoe.mirai.utils.*
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext

internal class MockGroupImpl(
    parentCoroutineContext: CoroutineContext,
    bot: MockBot,
    id: Long,
    override val uin: Long,
    name: String,
) : AbstractMockContact(
    parentCoroutineContext, bot, id
), MockGroup {
    @Deprecated(
        "use active.changeHonorMember",
        replaceWith = ReplaceWith(".active.changeHonorMember(member, honorType)"),
        level = DeprecationLevel.ERROR
    )
    override val honorMembers: MutableMap<GroupHonorType, MockNormalMember> = ConcurrentHashMap()
    private val txFileSystem by lazy { bot.mock().tmpResourceServer.mockServerFileDisk.newFsSystem() }

    override fun avatarUrl(spec: AvatarSpec): String {
        return avatarUrl
    }

    override val active: MockGroupActive by lazy { MockGroupActiveImpl(this) }

    override fun appendMember(mockMember: MemberInfo): MockGroup {
        addMember(mockMember)
        return this
    }

    override fun addMember(mockMember: MemberInfo): MockNormalMember {
        val nMember = MockNormalMemberImpl(
            this.coroutineContext,
            bot,
            mockMember.uin,
            this,
            mockMember.permission,
            mockMember.remark,
            mockMember.nick,
            mockMember.muteTimestamp,
            mockMember.joinTimestamp,
            mockMember.lastSpeakTimestamp,
            mockMember.specialTitle,
            mockMember.nameCard
        )

        if (nMember.id == bot.id) {
            botAsMember = nMember
        } else {
            members.delegate.removeAll { it.uin == nMember.id }
            members.delegate.add(nMember)
        }

        if (nMember.permission == MemberPermission.OWNER) {
            if (::owner.isInitialized) {
                owner.mock().mockApi.permission = MemberPermission.MEMBER
            }
            owner = nMember
        }
        return nMember
    }

    override suspend fun changeOwner(member: NormalMember) {
        if (member === owner) return
        val oldOwner = owner
        val oldPerm = member.permission
        oldOwner.mock().mockApi.permission = MemberPermission.MEMBER
        member.mock().mockApi.permission = MemberPermission.OWNER
        owner = member

        if (member === botAsMember) {
            BotGroupPermissionChangeEvent(this, oldPerm, MemberPermission.OWNER)
        } else {
            MemberPermissionChangeEvent(member, oldPerm, MemberPermission.OWNER)
        }.broadcast()

        if (oldOwner === botAsMember) {
            BotGroupPermissionChangeEvent(this, MemberPermission.OWNER, MemberPermission.MEMBER)
        } else {
            MemberPermissionChangeEvent(oldOwner, MemberPermission.OWNER, MemberPermission.MEMBER)
        }.broadcast()
    }

    override fun changeOwnerNoEventBroadcast(member: NormalMember) {
        val oldOwner = owner
        member.mock().mockApi.permission = MemberPermission.OWNER
        oldOwner.mockApi.permission = MemberPermission.MEMBER
        owner = member
    }

    override fun newAnonymous(nick: String, id: String): MockAnonymousMember {
        return MockAnonymousMemberImpl(
            coroutineContext, bot, 80000000, id, this, nick
        )
    }


    private val rawGroupControlPane = object : MockGroupControlPane {
        override val group: MockGroup get() = this@MockGroupImpl
        override val currentActor: MockNormalMember get() = group.botAsMember
        override var isAllowMemberInvite: Boolean = false
        override var isMuteAll: Boolean = false
        override var isAllowMemberFileUploading: Boolean = false
        override var isAnonymousChatAllowed: Boolean = false
        override var isAllowConfessTalk: Boolean = false
        override var groupName: String = name

        override fun withActor(actor: MockNormalMember): MockGroupControlPane {
            return GroupControlPaneImpl(actor)
        }
    }

    internal inner class GroupControlPaneImpl(
        override val currentActor: MockNormalMember
    ) : MockGroupControlPane {
        override val group: MockGroup get() = this@MockGroupImpl
        private val actorNullIfBot: MockNormalMember?
            get() = currentActor.takeIf { it.id != bot.id }

        override var groupName: String
            get() = rawGroupControlPane.groupName
            set(value) {
                val ov = rawGroupControlPane.groupName
                if (ov == value) return
                rawGroupControlPane.groupName = value
                GroupNameChangeEvent(ov, value, group, actorNullIfBot).broadcastBlocking()
            }

        override var isMuteAll: Boolean
            get() = rawGroupControlPane.isMuteAll
            set(value) {
                val ov = rawGroupControlPane.isMuteAll
                if (ov == value) return
                rawGroupControlPane.isMuteAll = value
                GroupMuteAllEvent(ov, value, group, actorNullIfBot).broadcastBlocking()
            }

        override var isAllowMemberFileUploading: Boolean
            get() = rawGroupControlPane.isAllowMemberFileUploading
            set(value) {
                // TODO: core-api no event
                rawGroupControlPane.isAllowMemberFileUploading = value
            }

        override var isAllowMemberInvite: Boolean
            get() = rawGroupControlPane.isAllowMemberInvite
            set(value) {
                val ov = rawGroupControlPane.isAllowMemberInvite
                if (ov == value) return
                rawGroupControlPane.isAllowMemberInvite = value
                GroupAllowMemberInviteEvent(ov, value, group, actorNullIfBot).broadcastBlocking()
            }

        override var isAnonymousChatAllowed: Boolean
            get() = rawGroupControlPane.isAnonymousChatAllowed
            set(value) {
                val ov = rawGroupControlPane.isAnonymousChatAllowed
                if (ov == value) return
                rawGroupControlPane.isAnonymousChatAllowed = value
                GroupAllowAnonymousChatEvent(ov, value, group, actorNullIfBot).broadcastBlocking()
            }

        override var isAllowConfessTalk: Boolean
            get() = rawGroupControlPane.isAllowConfessTalk
            set(value) {
                val ov = rawGroupControlPane.isAllowConfessTalk
                if (ov == value) return
                rawGroupControlPane.isAllowConfessTalk = value
                GroupAllowConfessTalkEvent(ov, value, group, currentActor.id == bot.id).broadcastBlocking()
            }

        override fun withActor(actor: MockNormalMember): MockGroupControlPane {
            return GroupControlPaneImpl(actor)
        }
    }

    override val controlPane: MockGroupControlPane get() = rawGroupControlPane

    override var name: String
        get() = controlPane.groupName
        set(value) {
            checkBotPermission(MemberPermission.ADMINISTRATOR)
            controlPane.withActor(botAsMember).groupName = value
        }

    override val mockApi: MockGroup.MockApi = object : MockGroup.MockApi {
        override var avatarUrl: String by lateinitMutableProperty {
            runBlocking { MockImage.randomForGroup(bot, id).getUrl(bot) }
        }
    }

    override fun changeAvatarUrl(newAvatar: String) {
        mockApi.avatarUrl = newAvatar
    }

    override val avatarUrl: String by mockApi::avatarUrl

    override lateinit var owner: MockNormalMember
    override lateinit var botAsMember: MockNormalMember
    override val members: ContactList<MockNormalMember> = ContactList()
    override fun get(id: Long): MockNormalMember? {
        if (id == bot.id) return botAsMember
        return members[id]
    }

    override fun contains(id: Long): Boolean = members.any { it.id == id }


    override suspend fun quit(): Boolean {
        return if (bot.groups.delegate.remove(this)) {
            BotLeaveEvent.Active(this).broadcast()
            cancel(CancellationException("Bot quited group $id"))
            true
        } else {
            false
        }
    }

    override val announcements = MockAnnouncementsImpl(this)

    @Suppress("OverridingDeprecatedMember", "OVERRIDE_DEPRECATION")
    override val settings: GroupSettings = object : GroupSettings {
        override var entranceAnnouncement: String
            get() = announcements.announcements.values.asSequence()
                .filter { it.parameters.sendToNewMember }
                .firstOrNull()?.content ?: ""
            set(value) {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                announcements.mockPublish(OfflineAnnouncement.create(value, buildAnnouncementParameters {
                    sendToNewMember = true
                }), this@MockGroupImpl.botAsMember)
            }

        override var isMuteAll: Boolean
            get() = rawGroupControlPane.isMuteAll
            set(value) {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                rawGroupControlPane.withActor(botAsMember).isMuteAll = value
            }

        override var isAllowMemberInvite: Boolean
            get() = rawGroupControlPane.isAllowMemberInvite
            set(value) {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                rawGroupControlPane.withActor(botAsMember).isAllowMemberInvite = value
            }

        @MiraiExperimentalApi
        override val isAutoApproveEnabled: Boolean
            get() = false // TODO

        override var isAnonymousChatEnabled: Boolean
            get() = rawGroupControlPane.isAnonymousChatAllowed
            set(value) {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                rawGroupControlPane.withActor(botAsMember).isAnonymousChatAllowed = value
            }
    }


    override fun newMessagePreSend(message: Message): MessagePreSendEvent =
        GroupMessagePreSendEvent(this, message)


    override suspend fun postMessagePreSend(message: MessageChain, receipt: MessageReceipt<*>) {
        GroupMessagePostSendEvent(this, message, null, receipt = receipt.cast())
            .broadcast()
    }

    override fun newMessageSource(message: MessageChain): OnlineMessageSource.Outgoing {
        return newMsgSrc(false, message) { ids, internalIds, time ->
            OnlineMsgSrcToGroup(ids, internalIds, time, message, bot, bot, this)
        }
    }

    override suspend fun broadcastMsgSyncEvent(client: OtherClient, message: MessageChain, time: Int) {
        val src = newMsgSrc(true, message, time.toLong()) { ids, internalIds, time0 ->
            OnlineMsgSrcToGroup(ids, internalIds, time0, message, bot, bot, this)
        }
        val msg = src.withMessage(message)
        GroupMessageSyncEvent(client, this, msg, botAsMember, bot.nick, time).broadcast()
    }

    override suspend fun sendMessage(message: Message): MessageReceipt<Group> {
        return super<AbstractMockContact>.sendMessage(message).cast()
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION_ERROR", "OVERRIDE_DEPRECATION")
    override suspend fun uploadVoice(resource: ExternalResource): net.mamoe.mirai.message.data.Voice =
        resource.mockUploadVoice(bot)

    override suspend fun setEssenceMessage(source: MessageSource): Boolean {
        checkBotPermission(MemberPermission.ADMINISTRATOR)
        essences.mockSetEssences(source, this.botAsMember)
        return true
    }

    override val essences: MockEssences = MockEssencesImpl(this)

    @Deprecated("Please use files instead.", replaceWith = ReplaceWith("files.root"))
    @Suppress("OverridingDeprecatedMember", "DEPRECATION", "DEPRECATION_ERROR")
    override val filesRoot: RemoteFile by lazy {
        net.mamoe.mirai.mock.internal.remotefile.remotefile.RootRemoteFile(txFileSystem, this)
    }

    override val files: RemoteFiles by lazy {
        net.mamoe.mirai.mock.internal.remotefile.absolutefile.MockRemoteFiles(this, txFileSystem)
    }

    override suspend fun uploadFile(
        filename: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>?
    ): FileMessage {
        TODO("uploadFile of MockGroupImpl")
    }

    override suspend fun uploadAudio(resource: ExternalResource): OfflineAudio =
        resource.mockUploadAudio(bot)

    override fun toString(): String {
        return "Group($id)"
    }

    override val roamingMessages: RoamingMessages = MockRoamingMessages(this)
}