/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.contact.info

import kotlinx.serialization.Serializable
import net.mamoe.mirai.data.GuildInfo
import net.mamoe.mirai.internal.network.Packet

@Serializable
internal data class GuildInfoImpl(
    override val name: String,
    override val id: Long,
    override val guildCode: Long,
    override val coverUrl: String,
    override val avatarUrl: String,
    override val guildProfile: String,
    override val maxMemberCount: Long,
    override val memberCount: Long,
    override val createTime: Long,
    override val maxRobotCount: Short,
    override val maxAdminCount: Short,
    override val ownerId: Long
) : GuildInfo, Packet, Packet.NoLog {

}