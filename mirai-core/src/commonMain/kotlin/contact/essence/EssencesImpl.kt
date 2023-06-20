/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.essence

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.contact.essence.EssenceMessageRecord
import net.mamoe.mirai.contact.essence.Essences
import net.mamoe.mirai.internal.contact.GroupImpl
import net.mamoe.mirai.internal.network.protocol.packet.chat.TroopEssenceMsgManager
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource

internal class EssencesImpl(
    internal val group: GroupImpl,
    internal val logger: MiraiLogger,
) : Essences {

    private suspend fun parse(content: JsonObject): Message {
        return when (content.getValue("msg_type").jsonPrimitive.intOrNull) {
            1 -> PlainText(content = content.getValue("text").jsonPrimitive.content)
            2 -> Face(id = content.getValue("face_index").jsonPrimitive.int)
            3 -> {
                val url = content.getValue("image_url").jsonPrimitive.content

                try {
                    // url -> bytes -> group.upload
                    val bytes = group.bot.downloadEssenceMessageImage(url)
                    bytes.toExternalResource().use { group.uploadImage(it) }
                } catch (cause: Exception) {
                    logger.debug({ "essence message image $url download fail." }, cause)
                    val match = IMAGE_MD5_REGEX.find(url) ?: return emptyMessageChain()
                    val (md5, ext) = match.destructured
                    val imageId = buildString {
                        append(md5)
                        insert(8,"-")
                        insert(13,"-")
                        insert(18,"-")
                        insert(23,"-")
                        insert(0, "{")
                        append("}.")
                        append(ext.replace("jpeg", "jpg"))
                    }
                    Image(imageId)
                }
            }
            else -> {
                // XXX: unknown message type
                logger.warning { "unknown digest message type for $content" }
                emptyMessageChain()
            }
        }
    }

    private fun plain(content: JsonObject): String {
        return when (content.getValue("msg_type").jsonPrimitive.intOrNull) {
            1 -> content.getValue("text").jsonPrimitive.content
            2 -> Face(id = content.getValue("face_index").jsonPrimitive.int).content
            3 -> "[图片]"
            else -> ""
        }
    }

    private suspend fun source(digests: DigestMessage, parse: Boolean): MessageSource {
        return group.bot.buildMessageSource(MessageSourceKind.GROUP) {
            ids = intArrayOf(digests.msgSeq.toInt())
            internalIds = intArrayOf(digests.msgRandom.toInt())
            time = digests.senderTime

            fromId = digests.senderUin
            targetId = group.id

            if (parse) {
                messages(digests.msgContent.map { content -> parse(content) })
            } else {
                messages(digests.msgContent.joinToString { content -> plain(content) }.toPlainText())
            }
        }
    }

    private fun record(digests: DigestMessage): EssenceMessageRecord {
        return EssenceMessageRecord(
            group = group,
            sender = group[digests.senderUin],
            senderId = digests.senderUin,
            senderNick = digests.senderNick,
            senderTime = digests.senderTime,
            operator = group[digests.addDigestUin],
            operatorId = digests.addDigestUin,
            operatorNick = digests.addDigestNick,
            operatorTime = digests.addDigestTime,
            loadMessageSource = { source(digests = digests, parse = false) }
        )
    }

    override suspend fun getPage(start: Int, limit: Int): List<EssenceMessageRecord> {
        val page = group.bot.getDigestList(
            groupCode = group.id,
            pageStart = start,
            pageLimit = limit
        )

        return page.messages.map(this::record)
    }

    override suspend fun share(source: MessageSource): String {
        val share = group.bot.shareDigest(
            groupCode = group.id,
            msgSeq = source.ids.first().toLong().and(0xFFFF_FFFF),
            msgRandom = source.internalIds.first().toLong().and(0xFFFF_FFFF),
            targetGroupCode = 0
        )
        return "https://qun.qq.com/essence/share?_wv=3&_wwv=128&_wvx=2&sharekey=${share.shareKey}"
    }

    override suspend fun remove(source: MessageSource) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        val result = group.bot.network.sendAndExpect(
            TroopEssenceMsgManager.RemoveEssence(
                group.bot.client,
                group.uin,
                source.internalIds.first(),
                source.ids.first()
            ), 5000, 2
        )
        if (result.success.not()) {
            try {
                group.bot.cancelDigest(
                    groupCode = group.id,
                    msgSeq = source.ids.first().toLong().and(0xFFFF_FFFF),
                    msgRandom = source.internalIds.first().toLong().and(0xFFFF_FFFF)
                )
            } catch (cause: IllegalStateException) {
                cause.addSuppressed(IllegalStateException(result.msg))
                throw cause
            }
        }
    }

    override fun asFlow(): Flow<EssenceMessageRecord> {
        return flow {
            var offset = 0
            while (currentCoroutineContext().isActive) {
                val page = group.bot.getDigestList(
                    groupCode = group.id,
                    pageStart = offset,
                    pageLimit = 30
                )
                for (message in page.messages) {
                    emit(record(message))
                }
                if (page.isEnd) break
                if (page.messages.isEmpty()) break
                offset += 30
            }
        }
    }
}