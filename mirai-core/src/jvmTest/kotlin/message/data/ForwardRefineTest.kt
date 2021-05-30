/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.AbstractTestWithMiraiImpl
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.ForwardMessageInternal
import net.mamoe.mirai.internal.message.SimpleRefineContext
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.message.data.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ForwardRefineTest : AbstractTestWithMiraiImpl() {
    private val bot = MockBot()

    companion object {
        private const val content = """
            <?xml version="1.0" encoding="utf-8"?>
    <msg brief="[聊天记录]" m_fileName="C85BCFB5-3143-41E2-860B-172D5E3FAFAC" action="viewMultiMsg" tSum="2" flag="3" m_resid="ZnAsFPOuieB0CJt7HJ6zC1Rq0MRqfso44OAvY99wqePeYYcGr03NI6UXB24QqefR" serviceID="35" m_fileSize="171"  > <item layout="1"> <title color="#000000" size="34" > 群聊的聊天记录 </title> <title color="#000000" size="26" > A:@B ‘ </title> <title color="#000000" size="26" > A:@B ’ </title>  <hr></hr> <summary color="#808080" size="26" > 查看转发消息  </summary> </item><source name="聊天记录"></source> </msg>
        """
        private const val resId = "ZnAsFPOuieB0CJt7HJ6zC1Rq0MRqfso44OAvY99wqePeYYcGr03NI6UXB24QqefR"
        private val nodes = listOf(ForwardMessage.Node(1, 1, "sender", AtAll))
    }

    override suspend fun downloadForwardMessage(bot: Bot, resourceId: String): List<ForwardMessage.Node> {
        assertEquals(resId, resourceId)
        return nodes
    }

    @Test
    fun `can refine ForwardMessageInternal deep`() = runBlockingUnit {
        val internal = ForwardMessageInternal(content, resId, null)
        val expectedOrigin =
            SimpleServiceMessage(internal.serviceId, content) // we do not expose ForwardMessageInternal
        val refine = internal.toMessageChain().refineDeep(bot, SimpleRefineContext().apply {
            set(ForwardMessageInternal.MsgTransmits, mapOf())
        })
        println(refine.size)
        println(refine.first()::class)
        println(refine._miraiContentToString())
        assertTrue { refine.first() is MessageOrigin }
        assertTrue { refine.drop(1).first() is ForwardMessage }
        assertEquals(
            ForwardMessage(
                preview = listOf("A:@B ‘", "A:@B ’"),
                title = "群聊的聊天记录",
                brief = "[聊天记录]",
                source = "聊天记录",
                summary = "查看转发消息",
                nodeList = nodes
            ),
            refine[ForwardMessage]
        )
        assertEquals(
            expectedOrigin,
            refine[MessageOrigin]!!.origin
        )
        assertEquals(
            MessageOrigin(expectedOrigin, resId, MessageOriginKind.FORWARD),
            refine[MessageOrigin]
        )
    }
}