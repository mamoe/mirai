/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact

import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent
import net.mamoe.mirai.event.events.GroupEntranceAnnouncementChangeEvent
import net.mamoe.mirai.event.events.GroupMuteAllEvent
import net.mamoe.mirai.event.events.GroupNameChangeEvent
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopManagement

@Suppress("SetterBackingFieldAssignment")
internal class GroupSettingsImpl(
    private val group: GroupImpl,
    groupInfo: GroupInfo,
) : GroupSettings {

    internal var nameField: String = groupInfo.name
    var name: String
        get() = nameField
        set(newValue) = with(group) {
            checkBotPermission(MemberPermission.ADMINISTRATOR)
            if (nameField != newValue) {
                val oldValue = nameField
                nameField = newValue
                launch {
                    bot.network.run {
                        TroopManagement.GroupOperation.name(
                            client = bot.client,
                            groupCode = id,
                            newName = newValue
                        ).sendWithoutExpect()
                    }
                    GroupNameChangeEvent(oldValue, newValue, group, null).broadcast()
                }
            }
        }


    private var _entranceAnnouncement: String = groupInfo.memo

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var entranceAnnouncement: String
        get() = _entranceAnnouncement
        set(newValue) = with(group) {
            checkBotPermission(MemberPermission.ADMINISTRATOR)
            //if (_announcement != newValue) {
            val oldValue = _entranceAnnouncement
            _entranceAnnouncement = newValue
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.memo(
                        client = bot.client,
                        groupCode = id,
                        newMemo = newValue
                    ).sendWithoutExpect()
                }
                GroupEntranceAnnouncementChangeEvent(oldValue, newValue, group, null).broadcast()
            }
            //}
        }

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var isAllowMemberInvite: Boolean = groupInfo.allowMemberInvite
        set(newValue) = with(group) {
            checkBotPermission(MemberPermission.ADMINISTRATOR)
            //if (_allowMemberInvite != newValue) {
            val oldValue = field
            field = newValue
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.allowMemberInvite(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
                GroupAllowMemberInviteEvent(oldValue, newValue, group, null).broadcast()
            }
            //}
        }

    internal var isAnonymousChatEnabledField: Boolean = groupInfo.allowAnonymousChat

    @Deprecated("Don't use public var internally", level = DeprecationLevel.HIDDEN)
    override var isAnonymousChatEnabled: Boolean
        get() = isAnonymousChatEnabledField
        @Suppress("UNUSED_PARAMETER")
        set(newValue) {
            throw UnsupportedOperationException()
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
        set(newValue) = with(group) {
            checkBotPermission(MemberPermission.ADMINISTRATOR)
            //if (_muteAll != newValue) {
            val oldValue = isMuteAllField
            isMuteAllField = newValue
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.muteAll(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
                GroupMuteAllEvent(oldValue, newValue, group, null).broadcast()
            }
            //}
        }
}