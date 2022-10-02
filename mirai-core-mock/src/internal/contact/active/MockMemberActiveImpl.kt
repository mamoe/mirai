/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.internal.contact.active

import net.mamoe.mirai.contact.active.MemberMedalInfo
import net.mamoe.mirai.contact.active.MemberMedalType
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.mock.contact.active.MockMemberActive

internal class MockMemberActiveImpl : MockMemberActive {
    override fun mockSetRank(value: Int) {
        rank = value
    }

    override fun mockSetPoint(value: Int) {
        point = value
    }

    override fun mockSetHonors(value: Set<GroupHonorType>) {
        honors = value
    }

    override fun mockSetTemperature(value: Int) {
        temperature = value
    }

    override fun mockSetMedal(info: MemberMedalInfo) {
        medal = info
    }

    override var rank: Int = 0
    override var point: Int = 0
    override var honors: Set<GroupHonorType> = setOf()
    override var temperature: Int = 0

    @Volatile
    var medal: MemberMedalInfo = MemberMedalInfo("", "", MemberMedalType.ACTIVE, setOf())

    override suspend fun queryMedal(): MemberMedalInfo = medal
}