package net.mamoe.mirai.api.http.data.common

import kotlinx.serialization.Serializable
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.QQ

@Serializable
abstract class ContactDTO : DTO {
    abstract val id: Long
}

@Serializable
data class QQDTO(
    override val id: Long,
    val nickName: String,
    val remark: String
) : ContactDTO() {
    // TODO: queryProfile.nickname & queryRemark.value not support now
    constructor(qq: QQ) : this(qq.id, "", "")
}


@Serializable
data class MemberDTO(
    override val id: Long,
    val memberName: String,
    val permission: MemberPermission,
    val group: GroupDTO
) : ContactDTO() {
    constructor(member: Member) : this (
        member.id, member.groupCard, member.permission,
        GroupDTO(member.group)
    )
}

@Serializable
data class GroupDTO(
    override val id: Long,
    val name: String,
    val permission: MemberPermission
) : ContactDTO() {
    constructor(group: Group) : this(group.id, group.name, group.botPermission)
}
