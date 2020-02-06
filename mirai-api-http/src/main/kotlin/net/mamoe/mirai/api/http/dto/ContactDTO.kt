package net.mamoe.mirai.api.http.dto

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
) : ContactDTO()

// TODO: queryProfile.nickname & queryRemark.value not support now
suspend fun QQDTO(qq: QQ): QQDTO = QQDTO(qq.id, "", "")

@Serializable
data class MemberDTO(
    override val id: Long,
    val memberName: String = "",
    val permission: MemberPermission,
    val group: GroupDTO
) : ContactDTO()

fun MemberDTO(member: Member, name: String = ""): MemberDTO = MemberDTO(member.id, name, member.permission, GroupDTO(member.group))

@Serializable
data class GroupDTO(
    override val id: Long,
    val name: String,
    val permission: MemberPermission
) : ContactDTO()

fun GroupDTO(group: Group): GroupDTO = GroupDTO(group.id, group.name, group.botPermission)