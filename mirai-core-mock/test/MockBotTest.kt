/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER", "NOTHING_TO_INLINE")

package net.mamoe.mirai.mock.test

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.announcement.AnnouncementParameters
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.messageChainOf
import net.mamoe.mirai.message.data.source
import net.mamoe.mirai.mock.MockBotFactory
import net.mamoe.mirai.mock.contact.MockNormalMember
import net.mamoe.mirai.mock.contact.announcement.MockOnlineAnnouncement
import net.mamoe.mirai.mock.internal.MockBotImpl
import net.mamoe.mirai.mock.userprofile.buildUserProfile
import net.mamoe.mirai.mock.utils.*
import net.mamoe.mirai.mock.utils.MockActions.mockFireRecalled
import net.mamoe.mirai.mock.utils.MockActions.nudged
import net.mamoe.mirai.mock.utils.MockActions.nudgedBy
import net.mamoe.mirai.mock.utils.MockActions.says
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.fail
import java.net.URL
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.jvm.jvmName
import kotlin.test.*

@Suppress("RemoveExplicitTypeArguments")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class MockBotTest {
    internal val bot = MockBotFactory.newMockBotBuilder()
        .id(984651187)
        .nick("Sakura")
        .create()

    @Test
    internal fun testMockBotMocking() = runBlocking<Unit> {
        repeat(50) { i ->
            bot.addFriend(20000L + i, "usr$i")
            bot.addStranger(10000L + i, "stranger$i")
            bot.addGroup(798100000L + i, "group$i")
        }
        assertEquals(50, bot.friends.size)
        assertEquals(50, bot.strangers.size)
        assertEquals(50, bot.groups.size)

        repeat(50) { i ->
            assertEquals("usr$i", bot.getFriendOrFail(20000L + i).nick)
            assertEquals("stranger$i", bot.getStrangerOrFail(10000L + i).nick)

            val group = bot.getGroupOrFail(798100000L + i)
            assertEquals("group$i", group.name)
            assertSame(group.botAsMember, group.owner)
            assertSame(MemberPermission.OWNER, group.botPermission)
            assertEquals(0, group.members.size)
        }

        val mockGroup = bot.group(798100000L)
        repeat(50) { i ->
            mockGroup.addMember(simpleMemberInfo(3700000L + i, "member$i", permission = MemberPermission.MEMBER))
        }
        repeat(50) { i ->
            val member = mockGroup.member(3700000L + i)
            assertEquals(MemberPermission.MEMBER, member.permission)
            assertEquals("member$i", member.nick)
            assertTrue(member.nameCard.isEmpty())
            assertEquals(MemberPermission.OWNER, mockGroup.botPermission)
        }

        val newOwner: MockNormalMember
        runAndReceiveEventBroadcast {
            newOwner = mockGroup.addMember0(simpleMemberInfo(84485417, "root", permission = MemberPermission.OWNER))
        }.let { events ->
            assertEquals(0, events.size)
        }
        assertEquals(MemberPermission.OWNER, newOwner.permission)
        assertEquals(MemberPermission.MEMBER, mockGroup.botPermission)
        assertSame(newOwner, mockGroup.owner)

        val newNewOwner = mockGroup.member(3700000L)
        runAndReceiveEventBroadcast {
            mockGroup.changeOwner(newNewOwner)
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<MemberPermissionChangeEvent>(events[0]) {
                assertSame(newNewOwner, member)
                assertSame(MemberPermission.OWNER, new)
                assertSame(MemberPermission.MEMBER, origin)
            }
            assertIsInstance<MemberPermissionChangeEvent>(events[1]) {
                assertSame(newOwner, member)
                assertSame(MemberPermission.OWNER, origin)
                assertSame(MemberPermission.MEMBER, new)
            }
        }
        assertEquals(MemberPermission.OWNER, newNewOwner.permission)
        assertEquals(MemberPermission.MEMBER, newOwner.permission)
        assertEquals(MemberPermission.MEMBER, mockGroup.botPermission)
        assertSame(newNewOwner, mockGroup.owner)
    }

    @Test
    internal fun testMockAvatarChange() = runBlocking<Unit> {
        assertEquals("http://q1.qlogo.cn/g?b=qq&nk=${bot.id}&s=640", bot.avatarUrl)
        runAndReceiveEventBroadcast {
            bot.avatarUrl = "http://localhost/test.png"
            assertEquals("http://localhost/test.png", bot.avatarUrl)
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<BotAvatarChangedEvent>(events[0])
        }
    }

    @Test
    internal fun testNewFriendRequest() = runBlocking<Unit> {
        runAndReceiveEventBroadcast {
            bot.broadcastNewFriendRequestEvent(
                1, "Hi", 0, "Hello!"
            ).reject()
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<NewFriendRequestEvent>(events[0]) {
                assertEquals(1, fromId)
                assertEquals("Hi", fromNick)
                assertEquals(0, fromGroupId)
                assertEquals("Hello!", message)
            }
            assertEquals(bot.friends.size, 0)
        }

        runAndReceiveEventBroadcast {
            bot.broadcastNewFriendRequestEvent(
                1, "Hi", 0, "Hello!"
            ).accept()
        }.let { events ->
            assertEquals(2, events.size, events.toString())
            assertIsInstance<NewFriendRequestEvent>(events[0]) {
                assertEquals(1, fromId)
                assertEquals("Hi", fromNick)
                assertEquals(0, fromGroupId)
                assertEquals("Hello!", message)
            }

            assertIsInstance<FriendAddEvent>(events[1]) {
                assertEquals(1, friend.id)
                assertEquals("Hi", friend.nick)
                assertSame(friend, bot.getFriend(friend.id))
            }

            assertEquals(1, bot.friends.size)
        }

    }

    @Test
    internal fun testMessageEventBroadcast() = runBlocking<Unit> {
        runAndReceiveEventBroadcast {
            bot.addGroup(5597122, "testing!")
                .addMember0(simpleMemberInfo(5971, "test", permission = MemberPermission.OWNER))
                .says("Hello World")

            bot.addFriend(9815, "tester").says("Msg By TestFriend")

            bot.addStranger(987166, "sudo").says("How are you")

            bot.group(5597122).sendMessage("Testing message")
            bot.friend(9815).sendMessage("Hi my friend")
            bot.stranger(987166).sendMessage("How are you")
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
    internal fun testMockGroupJoinRequest() = runBlocking<Unit> {
        val group = bot.addGroup(9875555515, "test")

        runAndReceiveEventBroadcast {
            group.broadcastNewMemberJoinRequestEvent(
                100000000, "demo", "msg"
            ).accept()
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<MemberJoinRequestEvent>(events[0]) {
                assertEquals(100000000, fromId)
                assertEquals("demo", fromNick)
                assertEquals("msg", message)
            }
            assertIsInstance<MemberJoinEvent>(events[1]) {
                assertEquals(100000000, member.id)
                assertEquals("demo", member.nick)
            }
        }

        val member = group.member(100000000)
        assertEquals(MemberPermission.MEMBER, member.permission)
    }

    @Test
    internal fun testMockBotJoinGroupRequest() = runBlocking<Unit> {
        val invitor = bot.addFriend(5710, "demo")
        runAndReceiveEventBroadcast {
            invitor.broadcastInviteBotJoinGroupRequestEvent(999999999, "test")
                .accept()
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<BotInvitedJoinGroupRequestEvent>(events[0]) {
                assertEquals(5710, invitorId)
                assertEquals("demo", invitorNick)
                assertEquals(999999999, groupId)
                assertEquals("test", groupName)
            }
            assertIsInstance<BotJoinGroupEvent>(events[1]) {
                assertNotSame(group.botAsMember, group.owner)
                assertEquals(MemberPermission.MEMBER, group.botPermission)
                assertEquals(999999999, group.id)
                assertEquals(MemberPermission.OWNER, group.owner.permission)
            }
        }
    }

    @Test
    internal fun testMessageRecallEventBroadcast() = runBlocking<Unit> {
        val group = bot.addGroup(8484846, "g")
        val admin = group.addMember0(simpleMemberInfo(945474, "admin", permission = MemberPermission.ADMINISTRATOR))
        val sender = group.addMember0(simpleMemberInfo(178711, "usr", permission = MemberPermission.MEMBER))

        runAndReceiveEventBroadcast {
            sender.says("Test").mockFireRecalled()
            sender.says("Admin recall").mockFireRecalled(admin)
            group.sendMessage("Hello world").mockFireRecalled(admin)
            sender.says("Hi").recall()
            admin.says("I'm admin").let { resp ->
                resp.recall()
                assertFails { resp.recall() }.let(::println)
            }
        }.dropMsgChat().let { events ->
            assertEquals(5, events.size)
            assertIsInstance<MessageRecallEvent.GroupRecall>(events[0]) {
                assertNull(operator)
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

        val root = group.addMember0(simpleMemberInfo(54986565, "root", permission = MemberPermission.OWNER))

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

    @Test
    internal fun testGroupAnnouncements() = runBlocking<Unit> {
        val group = bot.addGroup(8484541, "87")
        group.announcements.publish(
            MockOnlineAnnouncement(
                content = "Hello World",
                parameters = AnnouncementParameters.DEFAULT,
                senderId = 971121,
                allConfirmed = false,
                confirmedMembersCount = 0,
                publicationTime = 0
            )
        )
        val anc = group.announcements.asFlow().toList()
        assertEquals(1, anc.size)
        assertEquals("Hello World", anc[0].content)
        assertFalse(anc[0].fid.isEmpty())
        assertEquals(anc[0], group.announcements.get(anc[0].fid))
    }

    @Test
    internal fun testLeave() = runBlocking<Unit> {
        runAndReceiveEventBroadcast {
            bot.addGroup(1, "1").quit()
            bot.addFriend(2, "2").delete()
            bot.addStranger(3, "3").delete()
            bot.addGroup(4, "4")
                .addMember0(simpleMemberInfo(5, "5", permission = MemberPermission.MEMBER))
                .broadcastMemberLeave()
            bot.addGroup(6, "6")
                .addMember0(simpleMemberInfo(7, "7", permission = MemberPermission.OWNER))
                .broadcastKickBot()
        }.let { events ->
            assertEquals(5, events.size)
            assertIsInstance<BotLeaveEvent.Active>(events[0]) {
                assertEquals(1, group.id)
            }
            assertIsInstance<FriendDeleteEvent>(events[1]) {
                assertEquals(2, friend.id)
            }
            assertIsInstance<StrangerRelationChangeEvent.Deleted>(events[2]) {
                assertEquals(3, stranger.id)
            }
            assertIsInstance<MemberLeaveEvent>(events[3]) {
                assertEquals(4, group.id)
                assertEquals(5, member.id)
            }
            assertIsInstance<BotLeaveEvent.Kick>(events[4]) {
                assertEquals(6, group.id)
                assertEquals(7, operator.id)
            }
        }
    }

    @Test
    internal fun testNudge() = runBlocking<Unit> {
        val group = bot.addGroup(1, "1")
        val nudgeSender = group.addMember0(simpleMemberInfo(3, "3", permission = MemberPermission.MEMBER))
        val nudged = group.addMember0(simpleMemberInfo(4, "4", permission = MemberPermission.MEMBER))

        val myFriend = bot.addFriend(1, "514")
        val myStranger = bot.addStranger(2, "awef")

        runAndReceiveEventBroadcast {
            nudged.nudgedBy(nudgeSender)
            nudged.nudge().sendTo(group)
            myFriend.nudged(bot)
            myStranger.nudged(bot)
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
    internal fun testGroupFileV1() = runBlocking<Unit> {
        val fsroot = bot.addGroup(5417, "58aw").filesRoot
        fsroot.resolve("helloworld.txt").uploadAndSend(
            "HelloWorld".toByteArray().toExternalResource().toAutoCloseable()
        )
        assertEquals(1, fsroot.listFilesCollection().size)
        assertEquals(
            "HelloWorld",
            fsroot.resolve("helloworld.txt")
                .getDownloadInfo()!!
                .url.toUrl()
                .also { println("Mock file url: $it") }
                .readText()
        )
        fsroot.resolve("helloworld.txt").delete()
        assertEquals(0, fsroot.listFilesCollection().size)
    }

    @Test
    internal fun testQueryProfile() = runBlocking<Unit> {
        val service = bot.userProfileService
        val profile = buildUserProfile {
            nickname("Test0")
        }
        service.putUserProfile(1, profile)
        assertSame(profile, Mirai.queryProfile(bot, 1))
    }

    @Test
    internal fun testRoamingMessages() = runBlocking<Unit> {
        val mockFriend = bot.addFriend(1, "1")
        mockFriend says { append("Testing!") }
        mockFriend says { append("Test2!") }
        mockFriend.sendMessage("Pong!")

        mockFriend.roamingMessages.getAllMessages().toList().let { messages ->
            assertEquals(3, messages.size)
            assertEquals(messageChainOf(PlainText("Testing!")), messages[0])
            assertEquals(messageChainOf(PlainText("Test2!")), messages[1])
            assertEquals(messageChainOf(PlainText("Pong!")), messages[2])
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Utils">
    @AfterEach
    internal fun destroy() {
        bot.close()
    }

    internal inline fun <reified T> assertIsInstance(value: Any?, block: T.() -> Unit = {}) {
        if (value !is T) {
            fail { "Actual value $value (${value?.javaClass}) is not instanceof ${T::class.jvmName}" }
        }
        block(value)
    }

    internal suspend inline fun runAndReceiveEventBroadcast(
        action: () -> Unit
    ): List<Event> {

        contract {
            callsInPlace(action, InvocationKind.EXACTLY_ONCE)
        }

        val result = mutableListOf<Event>()
        val listener = GlobalEventChannel.subscribeAlways<Event> {
            result.add(this)
        }

        action()

        (bot as MockBotImpl).joinEventBroadcast()

        listener.cancel()
        return result
    }

    internal fun List<Event>.dropMessagePrePost() = filterNot {
        it is MessagePreSendEvent || it is MessagePostSendEvent<*>
    }

    internal fun List<Event>.dropMsgChat() = filterNot {
        it is MessageEvent || it is MessagePreSendEvent || it is MessagePostSendEvent<*>
    }

    internal inline fun String.toUrl(): URL = URL(this)

    internal inline fun <T> T.runAndAssertFails(block: T.() -> Unit) {
        assertFails { block() }
    }

    //</editor-fold>
}
