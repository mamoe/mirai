/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.message

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import net.mamoe.mirai.contact.BotIsBeingMutedException
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MessageTooLargeException
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent
import net.mamoe.mirai.event.events.GroupMessagePreSendEvent
import net.mamoe.mirai.event.events.MessagePostSendEvent
import net.mamoe.mirai.event.events.MessagePreSendEvent
import net.mamoe.mirai.internal.contact.*
import net.mamoe.mirai.internal.getMiraiImpl
import net.mamoe.mirai.internal.message.*
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_CAN_SEND_AS_FRAGMENTED
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_CAN_SEND_AS_LONG
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_CAN_SEND_AS_SIMPLE
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_FINAL_MESSAGE_CHAIN
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_MESSAGE_SOURCE_RESULT
import net.mamoe.mirai.internal.network.message.MessagePipelineContext.Companion.KEY_ORIGINAL_MESSAGE
import net.mamoe.mirai.internal.network.pipeline.*
import net.mamoe.mirai.internal.network.pipeline.PipelineContext.Companion.KEY_EXECUTION_RESULT
import net.mamoe.mirai.internal.network.protocol.packet.OutgoingPacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.FileManagement
import net.mamoe.mirai.internal.network.protocol.packet.chat.MusicSharePacket
import net.mamoe.mirai.internal.network.protocol.packet.chat.SendMessageResponse
import net.mamoe.mirai.internal.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.internal.network.protocol.packet.chat.receive.MessageSvcPbSendMsg
import net.mamoe.mirai.internal.network.protocol.packet.sendAndExpect
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.Either
import net.mamoe.mirai.utils.Either.Companion.fold
import net.mamoe.mirai.utils.cast


internal typealias MessagePipelineConfigurationBuilder<C> = PipelineConfigurationBuilder<MessagePipelineContext<C>, MessageChain, MessageReceipt<C>>

@Suppress("unused") // provides type C
internal inline fun <C : AbstractContact> OutgoingMessagePhases<C>.buildPhaseConfiguration(
    block: MessagePipelineConfigurationBuilder<C>.() -> Node.Finish<MessageReceipt<C>>,
): PipelineConfiguration<MessagePipelineContext<C>, MessageChain, MessageReceipt<C>> =
    MessagePipelineConfigurationBuilder<C>().apply { block() }.build()

internal fun main() {
    OutgoingMessagePhasesGroup.run {
        buildPhaseConfiguration {
            Begin then
                    Preconditions then
                    MessageToMessageChain then
                    OutgoingMessagePhasesCommon.BroadcastPreSendEvent(::GroupMessagePreSendEvent) then
                    CheckLength then
                    EnsureSequenceIdAvailable then
                    UploadForwardMessages then
                    FixGroupImages then

                    Savepoint(1) then

                    ConvertToLongMessage onFailureJumpTo 1 then
                    StartCreatePackets then
                    OutgoingMessagePhasesCommon.CreatePacketsForMusicShare(specialMessageSourceStrategy) then
                    OutgoingMessagePhasesCommon.CreatePacketsForFileMessage(specialMessageSourceStrategy) then
                    // TODO: 2021/8/15 fallback here
                    LogMessageSent() then
                    SendPacketsAndCreateReceipt() onFailureJumpTo 1 then

                    Finish finally

                    OutgoingMessagePhasesCommon.BroadcastPostSendEvent(::GroupMessagePostSendEvent) finally
                    CloseContext() finally
                    ThrowExceptions()
        }
    }
}

@DslMarker
internal annotation class PhaseMarker

@DslMarker
internal annotation class FinallyMarker

internal interface OutgoingMessagePhases<in C : AbstractContact>

@Suppress("PropertyName", "FunctionName")
internal abstract class OutgoingMessagePhasesCommon {
    companion object : OutgoingMessagePhasesCommon()

    @PhaseMarker
    val Begin = object : Phase<MessagePipelineContext<AbstractContact>, Message, Message>("Begin") {
        override suspend fun MessagePipelineContextRaw.doPhase(input: Message): Message {
            return input
        }
    }

    @PhaseMarker
    val Preconditions = object : Phase<MessagePipelineContext<AbstractContact>, Message, Message>("Preconditions") {
        override suspend fun MessagePipelineContextRaw.doPhase(input: Message): Message {
            require(!input.isContentEmpty()) { "message is empty" }

            return input
        }
    }

    @PhaseMarker
    val MessageToMessageChain =
        object : Phase<MessagePipelineContext<AbstractContact>, Message, MessageChain>("MessageToMessageChain") {
            override suspend fun MessagePipelineContextRaw.doPhase(input: Message): MessageChain {
                return input.toMessageChain()
            }
        }

    @PhaseMarker
    val CheckLength =
        object : Phase<MessagePipelineContext<AbstractContact>, MessageChain, MessageChain>("CheckLength") {
            override suspend fun MessagePipelineContextRaw.doPhase(input: MessageChain): MessageChain {
                if (input.contains(IgnoreLengthCheck)) return input

                input.takeSingleContent<ForwardMessage>()?.let { forward ->
                    checkForwardLength(forward)
                }

                input.checkLength(attributes[KEY_ORIGINAL_MESSAGE], contact)

                return input
            }

            fun MessagePipelineContextRaw.checkForwardLength(forward: ForwardMessage) {
                check(forward.nodeList.size <= 200) {
                    throw MessageTooLargeException(
                        contact, forward, forward,
                        "ForwardMessage allows up to 200 nodes, but found ${forward.nodeList.size}"
                    )
                }
            }
        }

    @PhaseMarker
    class BroadcastPreSendEvent<C : AbstractContact> @PhaseMarker constructor(
        private val constructor: (C, Message) -> MessagePreSendEvent
    ) : Phase<MessagePipelineContext<C>, MessageChain, MessageChain>("BroadcastPreSendEvent") {
        override suspend fun MessagePipelineContext<C>.doPhase(input: MessageChain): MessageChain {
            constructor(contact, input).broadcast()
            return input
        }
    }

    class BroadcastPostSendEvent<C : AbstractContact> @PhaseMarker constructor(
        private val constructor: (C, MessageChain, Throwable?, MessageReceipt<C>?) -> MessagePostSendEvent<in C>
    ) : Node.Finally<MessagePipelineContext<C>>("BroadcastPreSendEvent") {
        override suspend fun MessagePipelineContext<C>.doFinally() {
            val result = attributes[KEY_EXECUTION_RESULT]
            if (result.isFailure) return
            val chain = attributes[KEY_FINAL_MESSAGE_CHAIN]
            constructor(contact, chain, result.exceptionOrNull(), result.getOrNull()?.cast()).broadcast()
        }
    }

    @PhaseMarker
    fun <T> LogMessageSent() = object : Phase<MessagePipelineContext<AbstractContact>, T, T>("LogMessageSent") {
        override suspend fun MessagePipelineContext<AbstractContact>.doPhase(input: T): T {
            contact.logMessageSent(attributes[KEY_ORIGINAL_MESSAGE])
            return input
        }
    }

    @PhaseMarker
    val UploadForwardMessages =
        object : Phase<MessagePipelineContext<AbstractContact>, MessageChain, MessageChain>("UploadForwardMessages") {
            override suspend fun MessagePipelineContextRaw.doPhase(input: MessageChain): MessageChain {
                return input.replaced<ForwardMessage> { uploadForward(it) }
            }

            private suspend fun MessagePipelineContextRaw.uploadForward(forward: ForwardMessage): ForwardMessageInternal {
                val resId = getMiraiImpl().uploadMessageHighway(
                    contact = contact,
                    nodes = forward.nodeList,
                    isLong = false,
                )
                return RichMessage.forwardMessage(
                    resId = resId,
                    timeSeconds = time.currentTimeSeconds(),
                    forwardMessage = forward,
                )
            }
        }

    @PhaseMarker
    val FixGroupImages =
        object : Phase<MessagePipelineContext<GroupImpl>, MessageChain, MessageChain>("FixGroupImages") {
            override suspend fun MessagePipelineContext<GroupImpl>.doPhase(input: MessageChain): MessageChain {
                input.forEach {
                    if (it is OfflineGroupImage) contact.fixImageFileId(it)
                }
                input.replaced<FriendImage> {
                    contact.updateFriendImageForGroupMessage(it)
                }
                return input
            }

            suspend fun GroupImpl.fixImageFileId(image: OfflineGroupImage) {
                if (image.fileId == null) {
                    val response: ImgStore.GroupPicUp.Response = ImgStore.GroupPicUp(
                        bot.client,
                        uin = bot.id,
                        groupCode = this.id,
                        md5 = image.md5,
                        size = 1,
                    ).sendAndExpect(bot)

                    when (response) {
                        is ImgStore.GroupPicUp.Response.Failed -> {
                            image.fileId = 0 // Failed
                        }
                        is ImgStore.GroupPicUp.Response.FileExists -> {
                            image.fileId = response.fileId.toInt()
                        }
                        is ImgStore.GroupPicUp.Response.RequireUpload -> {
                            image.fileId = response.fileId.toInt()
                        }
                    }
                }
            }

            /**
             * Ensures server holds the cache
             */
            suspend fun GroupImpl.updateFriendImageForGroupMessage(image: FriendImage): OfflineGroupImage {
                val response = ImgStore.GroupPicUp(
                    bot.client,
                    uin = bot.id,
                    groupCode = id,
                    md5 = image.md5,
                    size = if (image is OnlineFriendImageImpl) image.delegate.fileLen else 0
                ).sendAndExpect(bot.network)
                return OfflineGroupImage(image.imageId).also { img ->
                    when (response) {
                        is ImgStore.GroupPicUp.Response.FileExists -> {
                            img.fileId = response.fileId.toInt()
                        }
                        is ImgStore.GroupPicUp.Response.RequireUpload -> {
                            img.fileId = response.fileId.toInt()
                        }
                        is ImgStore.GroupPicUp.Response.Failed -> {
                            img.fileId = 0
                        }
                    }
                }
            }
        }

    @PhaseMarker
    val EnsureSequenceIdAvailable =
        object :
            Phase<MessagePipelineContext<AbstractContact>, MessageChain, MessageChain>("EnsureSequenceIdAvailable") {
            override suspend fun MessagePipelineContextRaw.doPhase(input: MessageChain): MessageChain {
                input.findIsInstance<QuoteReply>()?.source?.ensureSequenceIdAvailable()
                return input
            }
        }

    @PhaseMarker
    val ConvertToLongMessage =
        object : Phase<MessagePipelineContext<AbstractContact>, MessageChain, MessageChain>("ConvertToLongMessage") {
            override suspend fun MessagePipelineContextRaw.doPhase(input: MessageChain): MessageChain {
                if (ForceAsLongMessage in input) {
                    return convertToLongMessageImpl(input)
                }

                when {
                    attributes[KEY_CAN_SEND_AS_SIMPLE] -> { // fastest
                        attributes[KEY_CAN_SEND_AS_SIMPLE] = false
                        return input
                    }
                    attributes[KEY_CAN_SEND_AS_LONG] && DontAsLongMessage !in input -> {
                        attributes[KEY_CAN_SEND_AS_LONG] = false
                        return convertToLongMessageImpl(input)
                    }
                    attributes[KEY_CAN_SEND_AS_FRAGMENTED] -> { // slowest
                        attributes[KEY_CAN_SEND_AS_FRAGMENTED] = false
                        return input
                    }
                    else -> {
                        error("Failed to send message: all strategies tried out.")
                    }
                }
            }

            suspend fun MessagePipelineContextRaw.convertToLongMessageImpl(chain: MessageChain): MessageChain {
                val resId = uploadLongMessageHighway(chain)
                return chain + RichMessage.longMessage(
                    brief = chain.takeContent(27),
                    resId = resId,
                    timeSeconds = time.currentTimeSeconds()
                ) // LongMessageInternal replaces all contents and preserves metadata
            }

            suspend fun MessagePipelineContextRaw.uploadLongMessageHighway(
                chain: MessageChain,
            ): String {
                return getMiraiImpl().uploadMessageHighway(
                    contact,
                    listOf(
                        ForwardMessage.Node(
                            senderId = bot.id,
                            time = time.currentTimeSeconds().toInt(),
                            messageChain = chain,
                            senderName = bot.nick
                        )
                    ),
                    true
                )
            }
        }

    @PhaseMarker
    val StartCreatePackets =
        object :
            Phase<MessagePipelineContext<AbstractContact>, MessageChain, List<OutgoingPacket>?>("CreateMessagePackets") {
            override suspend fun MessagePipelineContext<AbstractContact>.doPhase(input: MessageChain): List<OutgoingPacket>? {
                attributes[KEY_FINAL_MESSAGE_CHAIN] = input
                return null
            }
        }

    abstract class CreatePacketsPhase<in C : AbstractContact>(
        name: String
    ) : Phase<MessagePipelineContext<C>, List<OutgoingPacket>?, List<OutgoingPacket>?>(name) {
        override suspend fun MessagePipelineContext<C>.doPhase(input: List<OutgoingPacket>?): List<OutgoingPacket>? {
            return doPhase(attributes[KEY_FINAL_MESSAGE_CHAIN])
        }

        protected abstract suspend fun MessagePipelineContext<C>.doPhase(chain: MessageChain): List<OutgoingPacket>?
    }

    @PhaseMarker
    class CreatePacketsForMusicShare<in C : AbstractContact> @PhaseMarker constructor(
        private val specialMessageSourceStrategy: SpecialMessageSourceStrategy<C>
    ) : CreatePacketsPhase<C>("CreatePacketsForMusicShare") {
        override suspend fun MessagePipelineContext<C>.doPhase(chain: MessageChain): List<OutgoingPacket>? {
            val musicShare = chain[MusicShare] ?: return null
            attributes[KEY_MESSAGE_SOURCE_RESULT] =
                CompletableDeferred(specialMessageSourceStrategy.constructSourceForSpecialMessage(context, chain, 3116))
            return listOf(
                MusicSharePacket(
                    bot.client, musicShare, contact.id,
                    targetKind = if (contact is GroupImpl) MessageSourceKind.GROUP else MessageSourceKind.FRIEND // always FRIEND
                )
            )
        }
    }

    interface SpecialMessageSourceStrategy<in C : AbstractContact> {
        suspend fun constructSourceForSpecialMessage(
            context: MessagePipelineContext<C>,
            finalMessage: MessageChain,
            fromAppId: Int,
        ): OnlineMessageSource.Outgoing
    }

    @PhaseMarker
    class CreatePacketsForFileMessage<in C : AbstractContact> @PhaseMarker constructor(
        private val specialMessageSourceStrategy: SpecialMessageSourceStrategy<C>
    ) : CreatePacketsPhase<C>("CreatePacketsForFileMessage") {
        override suspend fun MessagePipelineContext<C>.doPhase(chain: MessageChain): List<OutgoingPacket>? {
            val file = chain[FileMessage] ?: return null
            file.checkIsImpl()
            attributes[KEY_MESSAGE_SOURCE_RESULT] =
                contact.async { specialMessageSourceStrategy.constructSourceForSpecialMessage(context, chain, 2021) }
            return listOf(FileManagement.Feed(bot.client, contact.id, file.busId, file.id))
        }
    }

    @PhaseMarker
    abstract class CreatePacketsFallback<C : AbstractContact> : CreatePacketsPhase<C>("CreatePacketsFallback")

    @PhaseMarker
    fun <C : AbstractContact> SendPacketsAndCreateReceipt() =
        object :
            Phase<MessagePipelineContext<C>, List<OutgoingPacket>?, MessageReceipt<C>>("EnsureSequenceIdAvailable") {
            override suspend fun MessagePipelineContext<C>.doPhase(input: List<OutgoingPacket>?): MessageReceipt<C> {
                checkNotNull(input) { "Internal error: packets are null." }
                val finalMessage = attributes[KEY_FINAL_MESSAGE_CHAIN]

                for (packet in input) {
                    val resp = packet.sendAndExpect<SendMessageResponse>(bot)
                    when (resp) {
                        is MusicSharePacket.Response -> {
                            resp.pkg.checkSuccess("send music share")
                        }
                        is MessageSvcPbSendMsg.Response.MessageTooLarge -> {
                            throw MessageTooLargeException(
                                contact, attributes[KEY_ORIGINAL_MESSAGE], finalMessage,
                                "Message '${finalMessage.content.take(10)}' is too large."
                            )
                        }
                        is MessageSvcPbSendMsg.Response.Failed -> {
                            val contact = contact
                            when (resp.errorCode) {
                                120 -> if (contact is Group) throw BotIsBeingMutedException(contact)
                            }
                            error("Send message failed: $resp")
                        }
                        is MessageSvcPbSendMsg.Response.SUCCESS -> {
                        }
                    }
                }

                val sourceAwait = attributes[KEY_MESSAGE_SOURCE_RESULT].await()

                try {
                    sourceAwait.ensureSequenceIdAvailable()
                } catch (e: Exception) {
                    logger.warning(
                        "Timeout awaiting sequenceId for message(${finalMessage.content.take(10)}). Some features may not work properly",
                        e
                    )
                }

                return sourceAwait.createMessageReceipt(contact, true)
            }

        }

    @PhaseMarker
    fun <Ctx : MessagePipelineContext<AbstractContact>> CloseContext() = object : Node.Finally<Ctx>("CloseContext") {
        override suspend fun Ctx.doFinally() {
            cancel() // coroutine scope
        }
    }

    @PhaseMarker
    fun <Ctx : MessagePipelineContext<AbstractContact>> ThrowExceptions() =
        object : Node.Finally<Ctx>("ThrowExceptions") {
            override suspend fun Ctx.doFinally() {
                attributes[KEY_EXECUTION_RESULT].onFailure {
                    exceptionCollector.collectThrow(it)
                }
            }
        }
}

@OverloadResolutionByLambdaReturnType
internal inline fun <reified T : SingleMessage> MessageChain.replaced(
    replacer: (message: T) -> Either<SingleMessage, Iterable<SingleMessage>>
): MessageChain {
    if (!this.anyIsInstance<T>()) return this

    return buildMessageChain(this.size) {
        for (singleMessage in this@replaced) {
            if (singleMessage is T) {
                replacer(singleMessage).fold(
                    onLeft = { add(it) },
                    onRight = { addAll(it) }
                )
            }
        }
    }
}

@OverloadResolutionByLambdaReturnType
@JvmName("replaced1")
internal inline fun <reified T : SingleMessage> MessageChain.replaced(
    replacer: (message: T) -> SingleMessage
): MessageChain = replaced<T> { Either(replacer(it)) }