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
import net.mamoe.mirai.internal.MockBot
import net.mamoe.mirai.internal.message.DeepMessageRefiner.refineDeep
import net.mamoe.mirai.internal.message.LightMessageRefiner.refineLight
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.message.data.*
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals


open class TM(private val name: String = Random.nextInt().toString()) : SingleMessage {
    override fun toString(): String = name
    override fun contentToString(): String = name
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TM

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()
}

private val bot = MockBot()

private suspend fun testRefineAll(
    before: Message,
    after: MessageChain,
) {
    testRefineLight(before, after)
    testRefineDeep(before, after)
}

private suspend fun testRefineDeep(
    before: Message,
    after: MessageChain
) = assertEquals(after.toMessageChain(), before.toMessageChain().refineDeep(bot))

private fun testRefineLight(
    before: Message,
    after: MessageChain
) = assertEquals(after.toMessageChain(), before.toMessageChain().refineLight(bot))


@Suppress("TestFunctionName")
private fun RefinableMessage(
    refine: (bot: Bot, context: MessageChain) -> Message?
): RefinableMessage {
    return object : RefinableMessage, TM() {
        override fun tryRefine(bot: Bot, context: MessageChain): Message? {
            return refine(bot, context)
        }
    }
}

@Suppress("TestFunctionName")
private fun RefinableMessage0(
    refine: () -> Message?
): RefinableMessage {
    return object : RefinableMessage, TM() {
        override fun tryRefine(bot: Bot, context: MessageChain): Message? {
            return refine()
        }
    }
}

internal class MessageRefineTest {

    @Test
    fun `can remove self`() = runBlockingUnit {
        testRefineAll(
            RefinableMessage0 { null },
            messageChainOf()
        )
    }

    @Test
    fun `can replace`() = runBlockingUnit {
        testRefineAll(
            RefinableMessage0 { TM("1") },
            messageChainOf(TM("1"))
        )
    }

    @Test
    fun `can replace flatten`() = runBlockingUnit {
        testRefineAll(
            buildMessageChain {
                +RefinableMessage0 { TM("1") + TM("2") }
                +TM("3")
                +RefinableMessage0 { TM("4") + TM("5") }
            },
            messageChainOf(TM("1"), TM("2"), TM("3"), TM("4"), TM("5"))
        )
    }

    @Test
    fun `toMessageCainOffline`() = runBlockingUnit {

    }

    // TODO: 2021/4/7 tests using `toMessageChainOffline` etc.
}