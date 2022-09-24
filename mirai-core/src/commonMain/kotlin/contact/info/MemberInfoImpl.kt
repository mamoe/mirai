/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.contact.info

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.protocol.data.jce.StTroopMemberInfo
import net.mamoe.mirai.internal.network.protocol.packet.login.wtlogin.orEmpty
import net.mamoe.mirai.utils.ConcurrentSet
import net.mamoe.mirai.utils.currentTimeSeconds

@Serializable
internal data class MemberInfoImpl(
    override val uin: Long,
    override var nick: String,
    override var permission: MemberPermission,
    override var remark: String = "",
    override val nameCard: String = "",
    override val specialTitle: String = "",
    override val muteTimestamp: Int = 0,
    override val anonymousId: String? = null,
    override val joinTimestamp: Int = currentTimeSeconds().toInt(),
    override var lastSpeakTimestamp: Int = 0,
    override val isOfficialBot: Boolean = false,
    override var rank: Int = 1,
    override var point: Int = 0,
    override val honors: MutableSet<GroupHonorType> = ConcurrentSet(),
    override var temperature: Int = 1
) : MemberInfo {
    constructor(
        client: QQAndroidClient,
        jceInfo: StTroopMemberInfo,
        groupOwnerId: Long,
    ) : this(
        uin = jceInfo.memberUin,
        nick = jceInfo.nick,
        permission = when {
            jceInfo.memberUin == groupOwnerId -> MemberPermission.OWNER
            jceInfo.dwFlag?.takeLowestOneBit() == 1L -> MemberPermission.ADMINISTRATOR
            else -> MemberPermission.MEMBER
        },
        remark = jceInfo.autoRemark.orEmpty(),
        nameCard = jceInfo.sName.orEmpty(),
        specialTitle = jceInfo.sSpecialTitle.orEmpty(),
        muteTimestamp = jceInfo.dwShutupTimestap?.toInt() ?: 0,
        anonymousId = null,
        joinTimestamp = jceInfo.dwJoinTime?.toInt() ?: 0,
        lastSpeakTimestamp = jceInfo.dwLastSpeakTime?.toInt() ?: 0,
        isOfficialBot = client.groupConfig.isOfficialRobot(jceInfo.memberUin),
        rank = jceInfo.dwMemberLevel?.toInt() ?: 1,
        point = jceInfo.dwPoint?.toInt() ?: 0,
        honors = ConcurrentSet<GroupHonorType>().apply {
            /**
             * vecGroupHonor 的 结构是
             * [
             *     (type, value),
             *     (type, value),
             *     (type, value),
             * ]
             *  构成的 ByteArray
             *  type = 8 时，表示群荣誉头衔标志
             *  type = 16 时，表示群荣誉活跃度
             */
            val bytes = jceInfo.vecGroupHonor.orEmpty()
            for (index in bytes.indices step 2) {
                val type = bytes[index]
                if (type.toInt() == 8) {
                    val value = bytes.getOrNull(index + 1) ?: break
                    try {
                        @Suppress("INVISIBLE_MEMBER")
                        add(GroupHonorType(value.toInt()))
                    } catch (_: Exception) {
                    }
                }
            }
        },
        temperature = jceInfo.vecGroupHonor?.let { bytes ->
            for (index in bytes.indices step 2) {
                val type = bytes[index]
                if (type.toInt() == 16) {
                    val value = bytes.getOrNull(index + 1) ?: break
                    return@let value.toInt()
                }
            }
            null
        } ?: 0
    )
}