/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.framework

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.network.KeyWithCreationTime
import net.mamoe.mirai.internal.network.KeyWithExpiry
import net.mamoe.mirai.internal.network.WLoginSigInfo
import net.mamoe.mirai.internal.network.WLoginSimpleInfo
import net.mamoe.mirai.internal.notice.processors.GroupExtensions
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.hexToBytes
import kotlin.test.BeforeTest

internal abstract class AbstractRealTimeActionTestUnit : AbstractCommonNHTest(), GroupExtensions {
    @BeforeTest
    internal fun prepareEnv() {
        bot.client.wLoginSigInfoField = WLoginSigInfo(
            uin = bot.id,
            encryptA1 = "01 23 33 AF EA".hexToBytes(),
            noPicSig = "55 47 20 23 54".hexToBytes(),
            simpleInfo = WLoginSimpleInfo(
                uin = bot.id,
                imgType = EMPTY_BYTE_ARRAY,
                imgFormat = EMPTY_BYTE_ARRAY,
                imgUrl = EMPTY_BYTE_ARRAY,
                mainDisplayName = EMPTY_BYTE_ARRAY,
            ),
            appPri = 0,
            a2ExpiryTime = 0,
            a2CreationTime = 849415181,
            loginBitmap = 1145141919810,
            tgt = "EA 5B CE FA 6C".hexToBytes(),
            tgtKey = "66 F5 A9 B8 FF".hexToBytes(),
            userStSig = KeyWithCreationTime(data = "3C FF FF FF 07".hexToBytes(), creationTime = 0),
            userStKey = "07 F5 A9 B8 0B".hexToBytes(),
            userStWebSig = KeyWithExpiry(data = "A1 5B CE FA 60".hexToBytes(), creationTime = 0, expireTime = 0),
            userA5 = KeyWithCreationTime(data = "66 CC FF AA AA".hexToBytes(), creationTime = 0),
            userA8 = KeyWithExpiry(data = "65 c1 B9 7A 1F".hexToBytes(), creationTime = 0, expireTime = 0),
            lsKey = KeyWithExpiry(data = "65 c1 B9 7A 1F".hexToBytes(), creationTime = 0, expireTime = 0),
            sKey = KeyWithExpiry(data = "D6 B1 9C 66 3A".hexToBytes(), creationTime = 0, expireTime = 0),
            userSig64 = KeyWithCreationTime(data = "D6 B1 9C 66 3A".hexToBytes(), creationTime = 0),
            openId = "D6 B1 9C 66 3A".hexToBytes(),
            openKey = KeyWithCreationTime(data = "B4 6E 5E 7A 3C".hexToBytes(), creationTime = 0),
            vKey = KeyWithExpiry(data = "A1 34 17 48 21".hexToBytes(), creationTime = 0, expireTime = 0),
            accessToken = KeyWithCreationTime(data = "12 35 87 14 A1".hexToBytes(), creationTime = 0),
            aqSig = KeyWithExpiry(data = "22 0C DC AC 30".hexToBytes(), creationTime = 0, expireTime = 0),
            superKey = "22 33 66 CC FF".hexToBytes(),
            sid = KeyWithExpiry(data = "11 45 14 19 19".hexToBytes(), creationTime = 0, expireTime = 0),
            psKeyMap = mutableMapOf(),
            pt4TokenMap = mutableMapOf(),
            d2 = KeyWithExpiry(data = "81 00 07 64 11".hexToBytes(), creationTime = 0, expireTime = 0),
            d2Key = "404 not found!!!!!!".toByteArray(),
            payToken = "What's this".toByteArray(),
            pf = "> 1 + 1 == 11\n< true".toByteArray(),
            pfKey = "Don't change anything if it runs".toByteArray(),
            da2 = "sudo rm -rf /".toByteArray(),
            wtSessionTicket = KeyWithCreationTime(data = "deluser root".toByteArray(), creationTime = 0),
            wtSessionTicketKey = "500 Server Internal Error".toByteArray(),
            deviceToken = "Winserver datacenter 2077".toByteArray(),
        )
        bot.client._bot = bot
        network.setStateOK(conn)
        removeOutgoingPacketEncoder()
    }
}
