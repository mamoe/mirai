/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.event.events.GroupMessagePostSendEvent
import net.mamoe.mirai.event.events.GroupMessagePreSendEvent
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.message.MessagePipelineConfiguration
import net.mamoe.mirai.internal.network.message.OutgoingMessagePhasesGroup
import net.mamoe.mirai.internal.network.message.buildPhaseConfiguration

/**
 * @since 2.8.0-M1
 */
internal interface OutgoingMessagePipelineFactory {
    fun createForGroup(group: GroupImpl): MessagePipelineConfiguration<GroupImpl>

    companion object : ComponentKey<OutgoingMessagePipelineFactory>
}

internal open class OutgoingMessagePipelineFactoryImpl : OutgoingMessagePipelineFactory {
    override fun createForGroup(group: GroupImpl): MessagePipelineConfiguration<GroupImpl> {
        return OutgoingMessagePhasesGroup.run {
            buildPhaseConfiguration {
                Begin then
                        Preconditions then
                        MessageToMessageChain then
                        BroadcastPreSendEvent(::GroupMessagePreSendEvent) then
                        CheckLength then
                        EnsureSequenceIdAvailable then
                        UploadForwardMessages then
                        FixGroupImages then

                        Savepoint(1) then

                        ConvertToLongMessage onFailureJumpTo 1 then
                        StartCreatePackets then
                        CreatePacketsForMusicShare(specialMessageSourceStrategy) then
                        CreatePacketsForFileMessage(specialMessageSourceStrategy) then
                        CreatePacketsFallback() then
                        LogMessageSent() then
                        SendPacketsAndCreateReceipt() onFailureJumpTo 1 then

                        Finish finally

                        BroadcastPostSendEvent(::GroupMessagePostSendEvent) finally
                        CloseContext() finally
                        ThrowExceptions()
            }
        }
    }

}