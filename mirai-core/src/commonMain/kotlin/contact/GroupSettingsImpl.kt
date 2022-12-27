/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact

import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent
import net.mamoe.mirai.event.events.GroupMuteAllEvent
import net.mamoe.mirai.event.events.GroupNameChangeEvent
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement.GroupOperation
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement.SwitchAnonymousChat

@Suppress("SetterBackingFieldAssignment")
internal class GroupSettingsImpl(
    private val group: GroupImpl,
    groupInfo: GroupInfo,
) : GroupSettings {

    private inline fun <T> GroupImpl.setImpl(
        newValue: T,
        getter: () -> T,
        setter: (T) -> Unit,
        crossinline packetConstructor: (client: QQAndroidClient, groupCode: Long, newValue: T) -> OutgoingPacket,
        crossinline eventConstructor: (old: T) -> Event?,
    ) {
        checkBotPermission(MemberPermission.ADMINISTRATOR)
        val oldValue = getter()
        setter(newValue)
        launch {
            bot.network.sendWithoutExpect(packetConstructor(bot.client, id, newValue))
            eventConstructor(oldValue)?.broadcast()
        }
    }


    internal var nameField: String = groupInfo.name
    var name: String
        get() = nameField
        set(newValue) {
            group.setImpl(newValue, { nameField }, { nameField = it }, GroupOperation::name) {
                GroupNameChangeEvent(it, newValue, group, null)
            }
        }


    private var _entranceAnnouncement: String = groupInfo.memo

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var entranceAnnouncement: String
        get() = _entranceAnnouncement
        set(newValue) {
            group.setImpl(newValue, { _entranceAnnouncement }, { _entranceAnnouncement = it }, GroupOperation::memo) {
                null
            }
        }

    private var isAllowMemberInviteField: Boolean = groupInfo.allowMemberInvite

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var isAllowMemberInvite: Boolean
        get() = isAllowMemberInviteField
        set(newValue) {
            group.setImpl(
                newValue,
                { isAllowMemberInviteField },
                { isAllowMemberInviteField = it },
                GroupOperation::allowMemberInvite
            ) {
                GroupAllowMemberInviteEvent(it, newValue, group, null)
            }
        }

    internal var isAnonymousChatEnabledField: Boolean = groupInfo.allowAnonymousChat

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var isAnonymousChatEnabled: Boolean
        get() = isAnonymousChatEnabledField
        set(newValue) {
            group.run {
                checkBotPermission(MemberPermission.ADMINISTRATOR)
                launch {
                    //Handle it in NoticePipelineContext#processAllowAnonymousChat
                    bot.network.sendAndExpect(SwitchAnonymousChat(bot.client, id, newValue))
                }
            }
        }

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var isAutoApproveEnabled: Boolean = groupInfo.autoApprove
        @Suppress("UNUSED_PARAMETER")
        set(newValue) {
            throw UnsupportedOperationException()
        }


    internal var isMuteAllField: Boolean = groupInfo.muteAll

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var isMuteAll: Boolean
        get() = isMuteAllField
        set(newValue) {
            group.setImpl(newValue, { isMuteAllField }, { isMuteAllField = it }, GroupOperation::muteAll) {
                GroupMuteAllEvent(it, newValue, group, null)
            }
        }
}