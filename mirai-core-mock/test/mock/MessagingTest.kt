/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import kotlinx.coroutines.flow.toList
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.mock.MockActions.mockFireRecalled
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.mock.userprofile.MockMemberInfoBuilder
import net.mamoe.mirai.mock.utils.broadcastMockEvents
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import kotlin.test.*

internal class MessagingTest : MockBotTestBase() {

    @Test
    internal fun testMessageEventBroadcast() = runTest {
        runAndReceiveEventBroadcast {
            bot.addGroup(5597122, "testing!")
                .addMember(simpleMemberInfo(5971, "test", permission = MemberPermission.OWNER))
                .says("Hello World")

            bot.addFriend(9815, "tester").says("Msg By TestFriend")

            bot.addStranger(987166, "sudo").says("How are you")

            bot.getGroupOrFail(5597122).sendMessage("Testing message")
            bot.getFriendOrFail(9815).sendMessage("Hi my friend")
            bot.getStrangerOrFail(987166).sendMessage("How are you")
        }.let { events ->
            assertEquals(9, events.size)

            assertIsInstance<GroupMessageEvent>(events[0]) {
                assertEquals("Hello World", message.contentToString())
                assertEquals("test", senderName)
                assertEquals(5971, sender.id)
                assertEquals(5597122, group.id)
                assertIsInstance<OnlineMessageSource.Incoming.FromGroup>(message.source)
            }
            assertIsInstance<FriendMessageEvent>(events[1]) {
                assertEquals("Msg By TestFriend", message.contentToString())
                assertEquals("tester", senderName)
                assertEquals(9815, sender.id)
                assertIsInstance<OnlineMessageSource.Incoming.FromFriend>(message.source)

            }
            assertIsInstance<StrangerMessageEvent>(events[2]) {
                assertEquals("How are you", message.contentToString())
                assertEquals("sudo", senderName)
                assertEquals(987166, sender.id)
                assertIsInstance<OnlineMessageSource.Incoming.FromStranger>(message.source)
            }

            assertIsInstance<GroupMessagePreSendEvent>(events[3])
            assertIsInstance<GroupMessagePostSendEvent>(events[4]) {
                assertIsInstance<OnlineMessageSource.Outgoing.ToGroup>(receipt!!.source)
            }
            assertIsInstance<FriendMessagePreSendEvent>(events[5])
            assertIsInstance<FriendMessagePostSendEvent>(events[6]) {
                assertIsInstance<OnlineMessageSource.Outgoing.ToFriend>(receipt!!.source)
            }
            assertIsInstance<StrangerMessagePreSendEvent>(events[7])
            assertIsInstance<StrangerMessagePostSendEvent>(events[8]) {
                assertIsInstance<OnlineMessageSource.Outgoing.ToStranger>(receipt!!.source)
            }
        }
    }

    @Test
    internal fun testNudge() = runTest {
        val group = bot.addGroup(1, "1")
        val nudgeSender = group.addMember(simpleMemberInfo(3, "3", permission = MemberPermission.MEMBER))
        val nudged = group.addMember(simpleMemberInfo(4, "4", permission = MemberPermission.MEMBER))

        val myFriend = bot.addFriend(1, "514")
        val myStranger = bot.addStranger(2, "awef")

        runAndReceiveEventBroadcast {
            nudged.nudgedBy(nudgeSender)
            nudged.nudge().sendTo(group)
            myFriend.nudges(bot)
            myStranger.nudges(bot)
            myFriend.nudgedBy(bot)
            myStranger.nudgedBy(bot)
        }.let { events ->
            assertEquals(6, events.size)
            assertIsInstance<NudgeEvent>(events[0]) {
                assertSame(nudgeSender, this.from)
                assertSame(nudged, this.target)
                assertSame(group, this.subject)
            }
            assertIsInstance<NudgeEvent>(events[1]) {
                assertSame(bot, this.from)
                assertSame(nudged, this.target)
                assertSame(group, this.subject)
            }
            assertIsInstance<NudgeEvent>(events[2]) {
                assertSame(myFriend, this.from)
                assertSame(bot, this.target)
                assertSame(myFriend, this.subject)
            }
            assertIsInstance<NudgeEvent>(events[3]) {
                assertSame(myStranger, this.from)
                assertSame(bot, this.target)
                assertSame(myStranger, this.subject)
            }
            assertIsInstance<NudgeEvent>(events[4]) {
                assertSame(bot, this.from)
                assertSame(myFriend, this.target)
                assertSame(myFriend, this.subject)
            }
            assertIsInstance<NudgeEvent>(events[5]) {
                assertSame(bot, this.from)
                assertSame(myStranger, this.target)
                assertSame(myStranger, this.subject)
            }
        }
    }

    @Test
    internal fun testRoamingMessages() = runTest {
        val mockFriend = bot.addFriend(1, "1")

        val allSent = mutableListOf<MessageSource>()
        fun MutableList<MessageSource>.add(msg: MessageChain) {
            add(msg.source)
        }

        fun MutableList<MessageSource>.convertToOffline() {
            replaceAll { src ->
                bot.buildMessageSource(src.kind) { allFrom(src) }
            }
        }

        broadcastMockEvents {
            allSent.add(mockFriend says { append("Testing!") })
            allSent.add(mockFriend says { append("Test2!") })
        }
        allSent.add(mockFriend.sendMessage("Pong!").source)
        allSent.convertToOffline()

        mockFriend.roamingMessages.getAllMessages().toList().let { messages ->
            assertEquals(3, messages.size)
            assertEquals(messageChainOf(allSent[0] + PlainText("Testing!")), messages[0])
            assertEquals(messageChainOf(allSent[1] + PlainText("Test2!")), messages[1])
            assertEquals(messageChainOf(allSent[2] + PlainText("Pong!")), messages[2])
        }

        allSent.clear()

        val mockGroup = bot.addGroup(2, "2")
        val mockGroupMember1 = mockGroup.addMember(123, "123")
        val mockGroupMember2 = mockGroup.addMember(124, "124")
        val mockGroupMember3 = mockGroup.addMember(125, "125")

        broadcastMockEvents {
            allSent.add(mockGroupMember1 says { append("msg1") })
            allSent.add(mockGroupMember2 says { append("msg2") })
            allSent.add(mockGroupMember3 says { append("msg3") })
        }
        allSent.convertToOffline()

        with(mockGroup.roamingMessages.getAllMessages().toList()) {
            assertEquals(3, size)
            assertEquals(messageChainOf(allSent[0] + PlainText("msg1")), get(0))
            assertEquals(messageChainOf(allSent[1] + PlainText("msg2")), get(1))
            assertEquals(messageChainOf(allSent[2] + PlainText("msg3")), get(2))
        }
    }

    @Test
    internal fun testMessageRecallEventBroadcast() = runTest {
        val group = bot.addGroup(8484846, "g")
        val admin = group.addMember(simpleMemberInfo(945474, "admin", permission = MemberPermission.ADMINISTRATOR))
        val sender = group.addMember(simpleMemberInfo(178711, "usr", permission = MemberPermission.MEMBER))

        runAndReceiveEventBroadcast {
            sender.says("Test").recalledBySender()
            sender.says("Admin recall").recalledBy(admin)
            mockFireRecalled(group.sendMessage("Hello world"), admin)
            sender.says("Hi").recall()
            admin.says("I'm admin").let { resp ->
                resp.recall()
                assertFails { resp.recall() }.let(::println)
            }
        }.dropMsgChat().let { events ->
            assertEquals(5, events.size)
            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                assertSame(sender, operator)
                assertSame(sender, author)
            }
            assertIsInstance<MessageRecallEvent.GroupRecall>(events[1]) {
                assertSame(admin, operator)
                assertSame(sender, author)
            }
            assertIsInstance<MessageRecallEvent.GroupRecall>(events[2]) {
                assertSame(admin, operator)
                assertSame(group.botAsMember, author)
            }
            assertIsInstance<MessageRecallEvent.GroupRecall>(events[3]) {
                assertSame(null, operator)
                assertSame(sender, author)
            }
            assertIsInstance<MessageRecallEvent.GroupRecall>(events[4]) {
                assertSame(null, operator)
                assertSame(admin, author)
            }
        }

        val root = group.addMember(simpleMemberInfo(54986565, "root", permission = MemberPermission.OWNER))

        runAndReceiveEventBroadcast {
            sender.says("0").runAndAssertFails { recall() }
            admin.says("0").runAndAssertFails { recall() }
            root.says("0").runAndAssertFails { recall() }
            group.sendMessage("Hi").recall()
        }.dropMsgChat().let { events ->
            assertEquals(1, events.size)
            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                assertEquals(group.botAsMember, author)
                assertEquals(null, operator)
            }
        }
    }


    @Suppress("ComplexRedundantLet")
    @Nested
    internal inner class MessageRecalling {
        @TestFactory
        fun `friend messaging`(): Iterable<DynamicNode> {
            val myFriend = bot.addFriend(1, "f")

            return listOf<DynamicNode>(
                dynamicTest("bot recalling") {
                    val msgBot = myFriend.sendMessage("2")
                    runAndReceiveEventBroadcast {
                        msgBot.recall()
                    }.let { events ->
                        assertEquals(0, events.size)
                    }
                    assertMessageNotAvailable(msgBot.source)
                },
                dynamicTest("friend recalling") {
                    val msgFriend = myFriend.says("1")
                    runAndReceiveEventBroadcast {
                        msgFriend.recalledBySender()
                    }.let { events ->
                        assertEquals(1, events.size)
                        assertIsInstance<MessageRecallEvent.FriendRecall>(events[0]) {
                            assertEquals(myFriend, this.operator)
                            assertContentEquals(msgFriend.source.ids, this.messageIds)
                            assertContentEquals(msgFriend.source.internalIds, this.messageInternalIds)
                        }
                        assertMessageNotAvailable(msgFriend.source)
                    }
                },
                dynamicTest("bot recall friend msg failed") {
                    val msg = myFriend.says("1")
                    assertFails { msg.recall() }
                    assertMessageAvailable(msg.source)
                },
            )
        }

        @TestFactory
        fun `stranger messaging`(): Iterable<DynamicNode> {
            val myStranger = bot.addStranger(2, "s")
            return listOf<DynamicNode>(
                dynamicTest("stranger recalling") {
                    val msg = myStranger.says("1")
                    runAndReceiveEventBroadcast {
                        msg.recalledBySender()
                    }.let { events ->
                        assertEquals(0, events.size)
                    }
                    assertMessageNotAvailable(msg.source)
                },
                dynamicTest("bot recalling") {
                    val msg = myStranger.sendMessage("1")
                    runAndReceiveEventBroadcast {
                        msg.recall()
                    }.let { events ->
                        assertEquals(0, events.size)
                    }
                    assertMessageNotAvailable(msg.source)
                },
                dynamicTest("bot recall stranger failed") {
                    val msg = myStranger.says("1")
                    assertFails { msg.recall() }
                    assertMessageAvailable(msg.source)
                },
            )
        }

        @TestFactory
        fun `group messaging`(): Iterable<DynamicNode> = listOf(
            dynamicContainer("Normal messaging test") {

                val group = bot.addGroup(18451444229, "owner group")
                group.addMember(MockMemberInfoBuilder.create {
                    uin(184554).permission(MemberPermission.OWNER)
                })
                val administrator = group.addMember(MockMemberInfoBuilder.create {
                    uin(184).permission(MemberPermission.ADMINISTRATOR)
                })
                val member = group.addMember(7777, "wapeog")

                group.botAsMember.mockApi.permission = MemberPermission.MEMBER


                return@dynamicContainer listOf<DynamicNode>(
                    dynamicTest("member self recalling") {
                        val msg = member.says("1")
                        runAndReceiveEventBroadcast { msg.recalledBySender() }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(member, this.author)
                                assertSame(member, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },

                    dynamicTest("member msg recalled by others") {
                        val msg = member.says("1")
                        runAndReceiveEventBroadcast { msg.recalledBy(administrator) }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(member, this.author)
                                assertSame(administrator, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },

                    dynamicTest("member msg forced recalled by bot") {
                        val msg = member.says("1")
                        runAndReceiveEventBroadcast { msg.recalledBy(group.botAsMember) }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(member, this.author)
                                assertSame(null, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },
                )
            },

            dynamicContainer("bot is owner") {
                val group = bot.addGroup(8412, "owner group")
                val administrator = group.addMember(MockMemberInfoBuilder.create {
                    uin(184).permission(MemberPermission.ADMINISTRATOR)
                })
                val member = group.addMember(7777, "wapeog")

                assertEquals(group.botPermission, MemberPermission.OWNER)


                return@dynamicContainer listOf<DynamicNode>(
                    dynamicTest("Bot can recall itself message") {
                        val msg = group.sendMessage("1")
                        runAndReceiveEventBroadcast { msg.recall() }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(group.botAsMember, this.author)
                                assertSame(null, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },
                    dynamicTest("Bot can recall administrator message") {
                        val msg = administrator.says("1")
                        runAndReceiveEventBroadcast { msg.recall() }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(administrator, this.author)
                                assertSame(null, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },
                    dynamicTest("Bot can recall member message") {
                        val msg = member.says("1")
                        runAndReceiveEventBroadcast { msg.recall() }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(member, this.author)
                                assertSame(null, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },
                )
            },
            dynamicContainer("bot is administrator") {

                val group = bot.addGroup(7517, "owner group")
                val owner = group.addMember(MockMemberInfoBuilder.create {
                    uin(96451).permission(MemberPermission.OWNER)
                })
                val administrator = group.addMember(MockMemberInfoBuilder.create {
                    uin(184).permission(MemberPermission.ADMINISTRATOR)
                })
                val member = group.addMember(7777, "wapeog")

                group.botAsMember.mockApi.permission = MemberPermission.ADMINISTRATOR



                return@dynamicContainer listOf<DynamicNode>(
                    dynamicTest("Bot can recall itself message") {
                        val msg = group.sendMessage("1")
                        runAndReceiveEventBroadcast { msg.recall() }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(group.botAsMember, this.author)
                                assertSame(null, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },
                    dynamicTest("Bot cannot recall owner message") {
                        val msg = owner.says("1")
                        assertFails { msg.recall() }
                        assertMessageAvailable(msg.source)
                    },
                    dynamicTest("Bot cannot recall administrator message") {
                        val msg = administrator.says("1")
                        assertFails { msg.recall() }
                        assertMessageAvailable(msg.source)
                    },
                    dynamicTest("Bot can recall member message") {
                        val msg = member.says("1")
                        runAndReceiveEventBroadcast { msg.recall() }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(member, this.author)
                                assertSame(null, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },
                )
            },
            dynamicContainer("bot is member") {

                val group = bot.addGroup(8441117, "owner group")
                val owner = group.addMember(MockMemberInfoBuilder.create {
                    uin(98171).permission(MemberPermission.OWNER)
                })
                val administrator = group.addMember(MockMemberInfoBuilder.create {
                    uin(184).permission(MemberPermission.ADMINISTRATOR)
                })
                val member = group.addMember(7777, "wapeog")

                group.botAsMember.mockApi.permission = MemberPermission.MEMBER


                return@dynamicContainer listOf<DynamicNode>(
                    dynamicTest("Bot can recall itself message") {
                        val msg = group.sendMessage("1")
                        runAndReceiveEventBroadcast { msg.recall() }.let { events ->
                            assertEquals(1, events.size)
                            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                                assertSame(group.botAsMember, this.author)
                                assertSame(null, operator)
                                assertContentEquals(msg.source.ids, this.messageIds)
                                assertContentEquals(msg.source.internalIds, this.messageInternalIds)
                            }
                        }
                        assertMessageNotAvailable(msg.source)
                    },
                    dynamicTest("Bot cannot recall owner message") {
                        val msg = owner.says("1")
                        assertFails { msg.recall() }
                        assertMessageAvailable(msg.source)
                    },
                    dynamicTest("Bot cannot recall administrator message") {
                        val msg = administrator.says("1")
                        assertFails { msg.recall() }
                        assertMessageAvailable(msg.source)
                    },
                    dynamicTest("Bot cannot recall member message") {
                        val msg = member.says("1")
                        assertFails { msg.recall() }
                        assertMessageAvailable(msg.source)
                    },
                )
            },
        )

    }
}