/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test.mock

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.announcement.AnnouncementParametersBuilder
import net.mamoe.mirai.contact.isBotMuted
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.mock.contact.announcement.MockOnlineAnnouncement
import net.mamoe.mirai.mock.test.MockBotTestBase
import net.mamoe.mirai.mock.userprofile.MockMemberInfoBuilder
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

        val member = group.getOrFail(100000000)
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
            assertEquals(0, events.size)
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
                .addMember(simpleMemberInfo(5, "5", permission = MemberPermission.MEMBER))
                .broadcastMemberLeave()
            bot.addGroup(6, "6")
                .addMember(simpleMemberInfo(7, "7", permission = MemberPermission.OWNER))
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

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
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
    internal fun testGroupFileUpload() = runTest {
        val files = bot.addGroup(111, "aaa").files
        val file = files.uploadNewFile("aaa", "ccc".toByteArray().toExternalResource().toAutoCloseable())
        assertEquals("ccc", file.getUrl()!!.toUrl().readText())
        runAndReceiveEventBroadcast {
            bot.getGroup(111)!!.addMember(simpleMemberInfo(222, "bbb", permission = MemberPermission.ADMINISTRATOR))
                .says(file.toMessage())
        }.let { events ->
            assertTrue(events[0].cast<GroupMessageEvent>().message.contains(FileMessage))
        }
    }

    @Test
    internal fun testAvatar() = runTest {
        assertNotEquals("", bot.addGroup(111, "aaa").avatarUrl.toUrl().readText())
    }

    @Test
    internal fun testBotLeaveGroup() = runTest {
        runAndReceiveEventBroadcast {
            bot.addGroup(1, "A").quit()
            bot.addGroup(2, "B")
                .addMember(MockMemberInfoBuilder.create {
                    uin(3).nick("W")
                    permission(MemberPermission.ADMINISTRATOR)
                }).broadcastKickBot()
            // TODO: BotLeaveEvent.Disband
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<BotLeaveEvent.Active>(events[0]) {
                assertEquals(1, group.id)
                assertEquals("A", group.name)
            }
            assertIsInstance<BotLeaveEvent.Kick>(events[1]) {
                assertEquals(2, group.id)
                assertEquals("B", group.name)
                assertEquals(3, operator.id)
                assertEquals("W", operator.nick)
            }
            assertNull(bot.getGroup(1))
            assertNull(bot.getGroup(2))
        }
    }

    @Test
    fun testBotGroupPermissionChangeEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addGroup(1, "")
                .appendMember(MockMemberInfoBuilder.create {
                    uin(1).nick("o")
                    permission(MemberPermission.OWNER)
                })
                .botAsMember permissionChangesTo MemberPermission.ADMINISTRATOR

            bot.addGroup(2, "")
                .appendMember(MockMemberInfoBuilder.create {
                    uin(1).nick("o")
                    permission(MemberPermission.OWNER)
                })
                .let {
                    it.changeOwner(it.botAsMember)
                }
        }.let { events ->
            assertEquals(3, events.size)
            assertIsInstance<BotGroupPermissionChangeEvent>(events[0]) {
                assertEquals(MemberPermission.ADMINISTRATOR, new)
                assertEquals(MemberPermission.MEMBER, origin)
            }
            assertIsInstance<BotGroupPermissionChangeEvent>(events[1]) {
                assertEquals(MemberPermission.OWNER, new)
                assertEquals(MemberPermission.MEMBER, origin)
            }
            assertIsInstance<MemberPermissionChangeEvent>(events[2]) {
                assertEquals(1, member.id)
                assertEquals("o", member.nick)
                assertEquals(MemberPermission.MEMBER, new)
                assertEquals(MemberPermission.OWNER, origin)
            }
        }
    }

    @Test
    fun testMuteEvent() = runTest {
        runAndReceiveEventBroadcast {
            val group = bot.addGroup(1, "")
                .appendMember(2, "")

            group.botAsMember.let {
                it.broadcastMute(it, 2)
                assertTrue { it.isMuted }
                it.broadcastMute(it, 0)
                assertFalse { it.isMuted }
                it.broadcastMute(it, 5)
                assertTrue { group.isBotMuted }
                assertTrue { it.isMuted }
            }

            group.getOrFail(2).let {
                it.broadcastMute(it, 2)
                assertTrue { it.isMuted }
                it.broadcastMute(it, 0)
                assertFalse { it.isMuted }
                it.broadcastMute(it, 5)
                assertTrue { it.isMuted }
            }
        }.let { events ->
            assertEquals(6, events.size)
            assertIsInstance<BotMuteEvent>(events[0])
            assertIsInstance<BotUnmuteEvent>(events[1])
            assertIsInstance<BotMuteEvent>(events[2])

            assertIsInstance<MemberMuteEvent>(events[3])
            assertIsInstance<MemberUnmuteEvent>(events[4])
            assertIsInstance<MemberMuteEvent>(events[5])

            delay(6000L)
            assertFalse { bot.getGroupOrFail(1).isBotMuted }
            assertFalse { bot.getGroupOrFail(1).getOrFail(2).isMuted }
        }
    }

    @Test
    fun testGroupNameChangeEvent() = runTest {
        runAndReceiveEventBroadcast {
            val g = bot.addGroup(1, "").appendMember(7, "A")
            g.controlPane.groupName = "OOOOO"
            g.name = "Test"
            g.controlPane.withActor(g.getOrFail(7)).groupName = "Hi"
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<GroupNameChangeEvent>(events[0]) {
                assertEquals("OOOOO", origin)
                assertEquals("Test", new)
                assertEquals(1, group.id)
                assertNull(operator)
            }
            assertIsInstance<GroupNameChangeEvent>(events[1]) {
                assertEquals("Test", origin)
                assertEquals("Hi", new)
                assertEquals(1, group.id)
                assertEquals(7, operator!!.id)
            }
        }
    }

    @Test
    fun testGroupMuteAllEvent() = runTest {
        runAndReceiveEventBroadcast {
            val g = bot.addGroup(1, "").appendMember(7, "A")
            g.controlPane.isMuteAll = true
            g.settings.isMuteAll = false
            g.controlPane.withActor(g.getOrFail(7)).isMuteAll = true
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<GroupMuteAllEvent>(events[0]) {
                assertEquals(true, origin)
                assertEquals(false, new)
                assertEquals(1, group.id)
                assertNull(operator)
            }
            assertIsInstance<GroupMuteAllEvent>(events[1]) {
                assertEquals(false, origin)
                assertEquals(true, new)
                assertEquals(1, group.id)
                assertEquals(7, operator!!.id)
            }
        }
    }

    @Test
    fun testGroupAllowAnonymousChatEvent() = runTest {
        runAndReceiveEventBroadcast {
            val g = bot.addGroup(1, "").appendMember(7, "A")
            g.controlPane.isAnonymousChatAllowed = true
            g.settings.isAnonymousChatEnabled = false
            g.controlPane.withActor(g.getOrFail(7)).isAnonymousChatAllowed = true
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<GroupAllowAnonymousChatEvent>(events[0]) {
                assertEquals(true, origin)
                assertEquals(false, new)
                assertEquals(1, group.id)
                assertNull(operator)
            }
            assertIsInstance<GroupAllowAnonymousChatEvent>(events[1]) {
                assertEquals(false, origin)
                assertEquals(true, new)
                assertEquals(1, group.id)
                assertEquals(7, operator!!.id)
            }
        }
    }

    @Test
    fun testGroupAllowConfessTalkEvent() = runTest {
        runAndReceiveEventBroadcast {
            val g = bot.addGroup(1, "").appendMember(7, "A")
            g.controlPane.isAllowConfessTalk = true
            g.controlPane.withActor(g.botAsMember).isAllowConfessTalk = false
            g.controlPane.withActor(g.getOrFail(7)).isAllowConfessTalk = true
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<GroupAllowConfessTalkEvent>(events[0]) {
                assertEquals(true, origin)
                assertEquals(false, new)
                assertEquals(1, group.id)
                assertTrue(isByBot)
            }
            assertIsInstance<GroupAllowConfessTalkEvent>(events[1]) {
                assertEquals(false, origin)
                assertEquals(true, new)
                assertEquals(1, group.id)
                assertFalse(isByBot)
            }
        }
    }

    @Test
    fun testGroupAllowMemberInviteEvent() = runTest {
        runAndReceiveEventBroadcast {
            val g = bot.addGroup(1, "").appendMember(7, "A")
            g.controlPane.isAllowMemberInvite = true
            g.settings.isAllowMemberInvite = false
            g.controlPane.withActor(g.getOrFail(7)).isAllowMemberInvite = true
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<GroupAllowMemberInviteEvent>(events[0]) {
                assertEquals(true, origin)
                assertEquals(false, new)
                assertEquals(1, group.id)
                assertNull(operator)
            }
            assertIsInstance<GroupAllowMemberInviteEvent>(events[1]) {
                assertEquals(false, origin)
                assertEquals(true, new)
                assertEquals(1, group.id)
                assertEquals(7, operator!!.id)
            }
        }
    }

    @Test
    fun testMemberCardChangeEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addGroup(1, "")
                .addMember(MockMemberInfoBuilder.create {
                    uin(2)
                    nameCard("Hi")
                }).nameCardChangesTo("Hello")
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<MemberCardChangeEvent>(events[0]) {
                assertEquals("Hi", origin)
                assertEquals("Hello", new)
                assertEquals(2, member.id)
                assertEquals(1, member.group.id)
            }
        }
    }

    @Test
    fun testMemberSpecialTitleChangeEvent() = runTest {
        runAndReceiveEventBroadcast {
            bot.addGroup(1, "").addMember(2, "") specialTitleChangesTo "Hello"
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<MemberSpecialTitleChangeEvent>(events[0]) {
                assertEquals("", origin)
                assertEquals("Hello", new)
                assertEquals(2, member.id)
                assertEquals(1, member.group.id)
            }
        }
    }

    @Test
    fun testHonorMember() = runTest {
        val group = bot.addGroup(1, "")
        val member1 = group.addMember(2, "")
        val member2 = group.addMember(3, "")
        assertEquals(emptyList(), group.active.queryHonorHistory(GroupHonorType.TALKATIVE).records)

        runAndReceiveEventBroadcast {
            group.active.changeHonorMember(member1, GroupHonorType.TALKATIVE)
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<MemberHonorChangeEvent.Achieve>(events[0]) {
                assertEquals(GroupHonorType.TALKATIVE, this.honorType)
                assertEquals(member1, this.member)
                assertEquals(group, this.group)
            }
        }
        assertEquals(member1, group.active.queryHonorHistory(GroupHonorType.TALKATIVE).current!!.member!!)
        assertEquals(emptyList(), group.active.queryHonorHistory(GroupHonorType.TALKATIVE).records)

        runAndReceiveEventBroadcast {
            group.active.changeHonorMember(member2, GroupHonorType.TALKATIVE)
        }.let { events ->
            assertEquals(3, events.size)
            assertIsInstance<GroupTalkativeChangeEvent>(events[0]) {
                assertEquals(member2, this.now)
                assertEquals(member1, this.previous)
                assertEquals(group, this.group)
            }
            assertIsInstance<MemberHonorChangeEvent.Lose>(events[1]) {
                assertEquals(GroupHonorType.TALKATIVE, this.honorType)
                assertEquals(member1, this.member)
                assertEquals(group, this.group)
            }
            assertIsInstance<MemberHonorChangeEvent.Achieve>(events[2]) {
                assertEquals(GroupHonorType.TALKATIVE, this.honorType)
                assertEquals(member2, this.member)
                assertEquals(group, this.group)
            }
        }

        assertEquals(member2, group.active.queryHonorHistory(GroupHonorType.TALKATIVE).current!!.member!!)
        // it.member must exist
        assertEquals(
            listOf(member1),
            group.active.queryHonorHistory(GroupHonorType.TALKATIVE).records.map { it.member!! })

        runAndReceiveEventBroadcast {
            group.active.changeHonorMember(member1, GroupHonorType.TALKATIVE)
        }.let { events ->
            assertEquals(3, events.size)
            assertIsInstance<GroupTalkativeChangeEvent>(events[0]) {
                assertEquals(member1, this.now)
                assertEquals(member2, this.previous)
                assertEquals(group, this.group)
            }
            assertIsInstance<MemberHonorChangeEvent.Lose>(events[1]) {
                assertEquals(GroupHonorType.TALKATIVE, this.honorType)
                assertEquals(member2, this.member)
                assertEquals(group, this.group)
            }
            assertIsInstance<MemberHonorChangeEvent.Achieve>(events[2]) {
                assertEquals(GroupHonorType.TALKATIVE, this.honorType)
                assertEquals(member1, this.member)
                assertEquals(group, this.group)
            }
        }
        assertEquals(member1, group.active.queryHonorHistory(GroupHonorType.TALKATIVE).current!!.member!!)
        assertEquals(
            listOf(member1, member2),
            group.active.queryHonorHistory(GroupHonorType.TALKATIVE).records.map { it.member!! })

        runAndReceiveEventBroadcast {
            group.active.changeHonorMember(member1, GroupHonorType.BRONZE)
        }.let { events ->
            assertEquals(1, events.size)
            assertIsInstance<MemberHonorChangeEvent.Achieve>(events[0]) {
                assertEquals(GroupHonorType.BRONZE, this.honorType)
                assertEquals(member1, this.member)
                assertEquals(group, this.group)
            }
        }
        assertEquals(member1, group.active.queryHonorHistory(GroupHonorType.BRONZE).current!!.member!!)
        assertEquals(emptyList(), group.active.queryHonorHistory(GroupHonorType.BRONZE).records)

        runAndReceiveEventBroadcast {
            group.active.changeHonorMember(member2, GroupHonorType.BRONZE)
        }.let { events ->
            assertEquals(2, events.size)
            assertIsInstance<MemberHonorChangeEvent.Lose>(events[0]) {
                assertEquals(GroupHonorType.BRONZE, this.honorType)
                assertEquals(member1, this.member)
                assertEquals(group, this.group)
            }
            assertIsInstance<MemberHonorChangeEvent.Achieve>(events[1]) {
                assertEquals(GroupHonorType.BRONZE, this.honorType)
                assertEquals(member2, this.member)
                assertEquals(group, this.group)
            }
        }
        assertEquals(member2, group.active.queryHonorHistory(GroupHonorType.BRONZE).current!!.member!!)
        assertEquals(listOf(member1), group.active.queryHonorHistory(GroupHonorType.BRONZE).records.map { it.member!! })
    }
}