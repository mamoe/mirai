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
                    val (md5, ext) = IMAGE_MD5_REGEX.find(url)!!.destructured
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

    private suspend fun source(message: DigestMessage): MessageSource {
        return group.bot.buildMessageSource(MessageSourceKind.GROUP) {
            ids = intArrayOf(message.msgSeq)
            internalIds = intArrayOf(message.msgRandom)
            time = message.senderTime

            fromId = message.senderUin
            targetId = group.id

            messages(message.msgContent.map { content -> parse(content) })
        }
    }

    private fun record(message: DigestMessage): EssenceMessageRecord {
        return EssenceMessageRecord(
            group = group,
            sender = group[message.senderUin],
            senderId = message.senderUin,
            senderNick = message.senderNick,
            senderTime = message.senderTime,
            operator = group[message.addDigestUin],
            operatorId = message.addDigestUin,
            operatorNick = message.addDigestNick,
            operatorTime = message.addDigestTime,
            loadMessageSource = { source(message = message) }
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
            msgSeq = source.ids.first(),
            msgRandom = source.internalIds.first(),
            targetGroupCode = 0
        )
        return "https://qun.qq.com/essence/share?_wv=3&_wwv=128&_wvx=2&sharekey=${share.shareKey}"
    }

    override suspend fun remove(source: MessageSource) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        group.bot.cancelDigest(
            groupCode = group.id,
            msgSeq = source.ids.first(),
            msgRandom = source.internalIds.first()
        )
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