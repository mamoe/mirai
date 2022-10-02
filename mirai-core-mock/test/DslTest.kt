/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.test

import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.mock.MockActions
import net.mamoe.mirai.mock.MockBotFactory
import net.mamoe.mirai.mock.userprofile.MockMemberInfoBuilder
import net.mamoe.mirai.mock.utils.NudgeDsl
import net.mamoe.mirai.mock.utils.broadcastMockEvents
import net.mamoe.mirai.mock.utils.mockUploadAsOnlineAudio
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import java.io.File


/*
 * This file only for showing MockDSL and how to use mock bot.
 * Not included in testing running
 */

@Suppress("unused")
internal suspend fun dslTest() {
    val bot = MockBotFactory.newMockBotBuilder().create()

    bot.addFriend(5, "OhMyFriend")

    bot.addGroup(1, "").apply {
        addMember(
            MockMemberInfoBuilder.create {
                uin(541)
                nameCard("Dmo")
                permission(MemberPermission.OWNER)
            }
        )
    }
    bot.addGroup(7, "")
        .appendMember(MockMemberInfoBuilder.create {  // Kotlin
            uin(571)
            nameCard("Hi")
            permission(MemberPermission.ADMINISTRATOR)
        })
        .appendMember(
            MockMemberInfoBuilder.invoke() // Java, MockMemberInfoBuilder.builder() in java
                .uin(1654441)
                .nameCard("60")
                .permission(MemberPermission.MEMBER)
                .specialTitle("ST")
                .build()
        )


    // 群成员 70 说了一句话
    bot.getGroupOrFail(50).getOrFail(70).says("0")

    // 群成员 1 发了一条语音
    bot.getGroupOrFail(1).getOrFail(1).says { // Kotlin
        +File("helloworld.amr").toExternalResource().toAutoCloseable().mockUploadAsOnlineAudio(bot)
    }
    /*
    Java:
    bot.getGroupOrFail(1).getOrFail(1).says(() -> {
        return bot.uploadOnlineAudio(
            ExternalResource.toExternalResource(new File("")).toAutoCloseable()
        );
    });
    */



    broadcastMockEvents { // Required for kotlin

        // 50 拍了拍 bot 的 sys32
        bot.getGroupOrFail(5).getOrFail(50).nudges(bot) {
            action("拍了拍")
            suffix("sys32")
        }
        MockActions.fireNudge( // Java
            bot.getGroupOrFail(5).getOrFail(50),
            bot,
            /*new*/ NudgeDsl().action("拍了拍").suffix("sys32")
        )

        // 1 拍了拍 bot 的 sys32
        bot.nudgedBy(bot.getGroupOrFail(1).getOrFail(1)) {
            action("拍了拍")
            suffix("sys32")
        }


        // 群成员 2 修改了群名片
        bot.getGroupOrFail(1).getOrFail(2) nameCardChangesTo "Test"
        MockActions.fireNameCardChanged( // Java
            bot.getGroupOrFail(1).getOrFail(2), "Test"
        )

        // 群成员 2 被群主修改了头衔
        bot.getGroupOrFail(1).getOrFail(2) specialTitleChangesTo "管埋员"
        MockActions.fireSpecialTitleChanged( // Java
            bot.getGroupOrFail(1).getOrFail(2), "管埋员"
        )

        // 群主修改了群成员 2 的权限为 Administrator
        bot.getGroupOrFail(1).getOrFail(2) permissionChangesTo MemberPermission.ADMINISTRATOR
        MockActions.firePermissionChanged( // Java
            bot.getGroupOrFail(1).getOrFail(2),
            MemberPermission.ADMINISTRATOR
        )

        // 群主撤回了一条群员消息
        bot.getGroupOrFail(1).owner.recallMessage( // Kotlin & Java
            bot.getGroupOrFail(1).getOrFail(1) says { append("SB") }
        )
    }

    // 新的入群申请
    bot.getGroupOrFail(50).broadcastNewMemberJoinRequestEvent(
        requester = 3,
        requesterName = "Him188moe",
        message = "Hi!",
    ).reject(message = "Hello!")

    // 新的好友申请
    bot.broadcastNewFriendRequestEvent(
        requester = 1,
        requesterNick = "Karlatemp",
        fromGroup = 0,
        message = "さくらが落ちる",
    ).accept()

    bot.broadcastNewFriendRequestEvent(9, "", 0, "").reject()
}
