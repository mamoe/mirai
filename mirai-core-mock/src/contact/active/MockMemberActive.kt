/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock.contact.active

import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.contact.active.MemberMedalInfo
import net.mamoe.mirai.data.GroupHonorType

public interface MockMemberActive : MemberActive {
    /**
     * @see rank
     */
    public fun mockSetRank(value: Int)

    /**
     * @see point
     */
    public fun mockSetPoint(value: Int)

    /**
     * @see honors
     */
    public fun mockSetHonors(value: Set<GroupHonorType>)

    /**
     * @see temperature
     */
    public fun mockSetTemperature(value: Int)

    /**
     * @see queryMedal
     */
    public fun mockSetMedal(info: MemberMedalInfo)
}
