/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import kotlinx.coroutines.flow.toList
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.announcement.AnnouncementParametersBuilder
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.mock.contact.announcement.MockOnlineAnnouncement
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.mock.utils.member
import net.mamoe.mirai.mock.utils.simpleMemberInfo
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.cast
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class MockGroupTest : MockBotTestBase() {
    @Test
    internal fun testMockGroupJoinRequest() = runTest {
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
    internal fun testMockBotJoinGroupRequest() = runTest {
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
    internal fun testGroupAnnouncements() = runTest {
        val group = bot.addGroup(8484541, "87")
        runAndReceiveEventBroadcast {
            group.announcements.publish(
                MockOnlineAnnouncement(
                    content = "dlroW olleH",
                    parameters = AnnouncementParametersBuilder().apply { this.sendToNewMember = true }.build(),
                    senderId = 9711221,
                    allConfirmed = false,
                    confirmedMembersCount = 0,
                    publicationTime = 0
                )
            )
            group.announcements.publish(
                MockOnlineAnnouncement(
                    content = "Hello World",
                    parameters = AnnouncementParametersBuilder().apply { this.sendToNewMember = true }.build(),
                    senderId = 971121,
                    allConfirmed = false,
                    confirmedMembersCount = 0,
                    publicationTime = 0
                )
            )
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<GroupEntranceAnnouncementChangeEvent>(events[0])
        }
        val anc = group.announcements.asFlow().toList()
        assertEquals(1, anc.size)
        assertEquals("Hello World", anc[0].content)
        assertFalse(anc[0].fid.isEmpty())
        assertEquals(anc[0], group.announcements.get(anc[0].fid))
    }

    @Test
    internal fun testLeave() = runTest {
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
    internal fun testGroupFileV1() = runTest {
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
    internal fun testMemberHonorChangeEvent() = runTest {
        runAndReceiveEventBroadcast {
            val group = bot.addGroup(111, "aa")
            val member1 = group.addMember0(simpleMemberInfo(222, "bb", permission = MemberPermission.MEMBER))
            val member2 = group.addMember0(simpleMemberInfo(333, "cc", permission = MemberPermission.MEMBER))
            group.honorMembers[GroupHonorType.ACTIVE] = member1
            group.changeHonorMember(member2, GroupHonorType.ACTIVE)
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<MemberHonorChangeEvent.Lose>(events[0])
            assertEquals(222, events[0].cast<MemberHonorChangeEvent.Lose>().member.id)
            assertEquals(GroupHonorType.ACTIVE, events[1].cast<MemberHonorChangeEvent.Achieve>().honorType)
            assertEquals(333, events[1].cast<MemberHonorChangeEvent.Achieve>().member.id)
            assertIsInstance<MemberHonorChangeEvent.Achieve>(events[1])
        }
    }

    @Test
    internal fun testGroupFileUpload() = runTest {
        val files = bot.addGroup(111, "aaa").files
        val file = files.uploadNewFile("aaa", "ccc".toByteArray().toExternalResource().toAutoCloseable())
        assertEquals("ccc", file.getUrl()!!.toUrl().readText())
        runAndReceiveEventBroadcast {
            bot.getGroup(111)!!.addMember0(simpleMemberInfo(222, "bbb", permission = MemberPermission.ADMINISTRATOR))
                .says(file.toMessage())
        }.let { events ->
            assertTrue(events[0].cast<GroupMessageEvent>().message.contains(FileMessage))
        }
    }

    @Test
    internal fun testAvatar() = runTest {
        assertNotEquals("", bot.addGroup(111, "aaa").avatarUrl.toUrl().readText())
    }
}